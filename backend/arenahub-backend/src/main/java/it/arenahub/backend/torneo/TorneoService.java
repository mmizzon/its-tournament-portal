package it.arenahub.backend.torneo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.arenahub.backend.torneo.TorneoDtos.TorneoRequest;
import it.arenahub.backend.torneo.TorneoDtos.TorneoResponse;

@Service
public class TorneoService {
    private final TorneoRepository repository;

    public TorneoService(TorneoRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<TorneoResponse> elenco() {
        return repository.findAll().stream().map(this::risposta).toList();
    }

    @Transactional(readOnly = true)
    public TorneoResponse dettaglio(Integer id) { return risposta(trova(id)); }

    @Transactional
    public TorneoResponse crea(TorneoRequest request) {
        valida(request);
        Torneo torneo = new Torneo();
        aggiornaDati(torneo, request);
        applicaModalita(torneo, request, true);
        return risposta(repository.save(torneo));
    }

    @Transactional
    public TorneoResponse modifica(Integer id, TorneoRequest request) {
        valida(request);
        Torneo torneo = trova(id);
        ModalitaTorneo modalitaAttuale = modalita(torneo);
        if (modalitaAttuale != request.modalita()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La modalità di un torneo esistente non può essere cambiata");
        }
        aggiornaDati(torneo, request);
        applicaModalita(torneo, request, false);
        return risposta(repository.save(torneo));
    }

    @Transactional
    public TorneoResponse annulla(Integer id) {
        Torneo torneo = trova(id);
        torneo.setCancellato(true);
        return risposta(repository.save(torneo));
    }

    private void aggiornaDati(Torneo torneo, TorneoRequest request) {
        torneo.setNome(request.nome().trim());
        torneo.setDescrizione(request.descrizione() == null ? null : request.descrizione().trim());
        torneo.setDataInizioIscrizioni(request.dataInizioIscrizioni());
        torneo.setDataFineIscrizioni(request.dataFineIscrizioni());
        torneo.setDataInizioTorneo(request.dataInizioTorneo());
        torneo.setDataFineTorneo(request.dataFineTorneo());
    }

    private void applicaModalita(Torneo torneo, TorneoRequest request, boolean nuova) {
        if (request.modalita() == ModalitaTorneo.INDIVIDUALE) {
            if (request.maxPartecipanti() == null || request.maxPartecipanti() <= 0
                    || request.maxSquadre() != null || request.componenti() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Per un torneo individuale specificare solo maxPartecipanti");
            }
            if (nuova) torneo.setIndividuale(new TorneoIndividuale(torneo, request.maxPartecipanti()));
            else torneo.getIndividuale().setMaxPartecipanti(request.maxPartecipanti());
        } else {
            if (request.maxSquadre() == null || request.maxSquadre() <= 0
                    || request.componenti() == null || request.componenti() <= 0
                    || request.maxPartecipanti() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Per un torneo a squadre specificare maxSquadre e componenti");
            }
            if (nuova) torneo.setSquadre(new TorneoSquadra(torneo, request.maxSquadre(), request.componenti()));
            else {
                torneo.getSquadre().setMaxSquadre(request.maxSquadre());
                torneo.getSquadre().setComponenti(request.componenti());
            }
        }
    }

    private void valida(TorneoRequest request) {
        if (request.dataInizioIscrizioni().isAfter(request.dataFineIscrizioni())
                || request.dataFineIscrizioni().isAfter(request.dataInizioTorneo())
                || request.dataInizioTorneo().isAfter(request.dataFineTorneo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le date devono rispettare: inizio iscrizioni <= fine iscrizioni <= inizio torneo <= fine torneo");
        }
    }

    private TorneoResponse risposta(Torneo torneo) {
        ModalitaTorneo modalita = modalita(torneo);
        Integer maxPartecipanti = modalita == ModalitaTorneo.INDIVIDUALE
                ? torneo.getIndividuale().getMaxPartecipanti() : null;
        Integer maxSquadre = modalita == ModalitaTorneo.SQUADRE
                ? torneo.getSquadre().getMaxSquadre() : null;
        Integer componenti = modalita == ModalitaTorneo.SQUADRE
                ? torneo.getSquadre().getComponenti() : null;
        StatoTorneo stato = stato(torneo);
        return new TorneoResponse(torneo.getId(), torneo.getNome(), torneo.getDescrizione(),
                torneo.getDataInizioIscrizioni(), torneo.getDataFineIscrizioni(),
                torneo.getDataInizioTorneo(), torneo.getDataFineTorneo(), torneo.isCancellato(),
                stato, modalita, maxPartecipanti, maxSquadre, componenti);
    }

    private StatoTorneo stato(Torneo torneo) {
        if (torneo.isCancellato()) return StatoTorneo.CANCELLATO;
        LocalDate oggi = LocalDate.now();
        if (oggi.isBefore(torneo.getDataInizioTorneo())) return StatoTorneo.APERTO;
        if (!oggi.isAfter(torneo.getDataFineTorneo())) return StatoTorneo.IN_CORSO;
        return StatoTorneo.CONCLUSO;
    }

    private ModalitaTorneo modalita(Torneo torneo) {
        if (torneo.getIndividuale() != null) return ModalitaTorneo.INDIVIDUALE;
        if (torneo.getSquadre() != null) return ModalitaTorneo.SQUADRE;
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Torneo senza modalità individuale o a squadre");
    }

    private Torneo trova(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneo non trovato"));
    }
}

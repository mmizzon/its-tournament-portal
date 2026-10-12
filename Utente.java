package it.its.tornei.dto;

import it.its.tornei.model.Iscrizione;

import java.time.LocalDateTime;

public record IscrittoResponse(
        Integer utenteId,
        String nome,
        String cognome,
        String email,
        LocalDateTime dataIscrizione
) {
    public static IscrittoResponse da(Iscrizione i) {
        return new IscrittoResponse(
                i.getUtente().getId(),
                i.getUtente().getNome(),
                i.getUtente().getCognome(),
                i.getUtente().getEmail(),
                i.getDataIscrizione());
    }
}

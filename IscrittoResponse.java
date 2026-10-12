package it.its.tornei;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TorneiApplicationTests {
    @Autowired
    private MockMvc mvc;

    @Test
    void flussoCompleto() throws Exception {
        MockHttpSession admin = login("admin@tornei.it", "Admin123!");

        MockHttpSession utente = registra("Luca", "Bianchi", "luca@test.it", "password123");
        mvc.perform(post("/api/admin/tornei").session(utente)
                        .contentType(MediaType.APPLICATION_JSON).content(torneoJson("Vietato", 0)))
                .andExpect(status().isForbidden());

        MvcResult creato = mvc.perform(post("/api/admin/tornei").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(torneoJson("Calcetto", 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stato").value("aperto"))
                .andExpect(jsonPath("$.iscrizioniAperte").value(true))
                .andReturn();
        String id = creato.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(post("/api/admin/tornei").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nome":"Sbagliato","dataInizioIscrizioni":"2030-01-10",
                                 "dataFineIscrizioni":"2030-01-01","dataInizioTorneo":"2030-02-01",
                                 "dataFineTorneo":"2030-02-02"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore").exists());

        mvc.perform(get("/api/tornei").param("stato", "aperto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Calcetto"));
        mvc.perform(post("/api/tornei/" + id + "/iscrizione"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/tornei/" + id + "/iscrizione").session(utente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iscritto").value(true))
                .andExpect(jsonPath("$.numeroIscritti").value(1));
        mvc.perform(post("/api/tornei/" + id + "/iscrizione").session(utente))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/profilo/tornei").session(utente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Calcetto"));
        mvc.perform(get("/api/admin/tornei/" + id + "/iscritti").session(admin))
                .andExpect(jsonPath("$[0].email").value("luca@test.it"));
        mvc.perform(delete("/api/tornei/" + id + "/iscrizione").session(utente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iscritto").value(false));

        mvc.perform(post("/api/admin/tornei/" + id + "/cancella").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stato").value("cancellato"));
        mvc.perform(post("/api/tornei/" + id + "/iscrizione").session(utente))
                .andExpect(status().isConflict());
    }

    @Test
    void registrazioneEmailDuplicataELoginErrato() throws Exception {
        registra("Anna", "Verdi", "anna@test.it", "password123");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Anna","cognome":"Verdi","email":"ANNA@test.it","password":"password123"}"""))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"anna@test.it","password":"sbagliata"}"""))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"","cognome":"X","email":"non-una-email","password":"123"}"""))
                .andExpect(status().isBadRequest());
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/auth/login").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk());
        return session;
    }

    private MockHttpSession registra(String nome, String cognome, String email, String password) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/auth/register").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"cognome\":\"" + cognome
                                + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
        return session;
    }

    private String torneoJson(String nome, int spostamento) {
        LocalDate oggi = LocalDate.now().plusDays(spostamento);
        return "{\"nome\":\"" + nome + "\",\"descrizione\":\"Test\","
                + "\"dataInizioIscrizioni\":\"" + oggi.minusDays(1) + "\","
                + "\"dataFineIscrizioni\":\"" + oggi.plusDays(5) + "\","
                + "\"dataInizioTorneo\":\"" + oggi.plusDays(10) + "\","
                + "\"dataFineTorneo\":\"" + oggi.plusDays(12) + "\"}";
    }
}

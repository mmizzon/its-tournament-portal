-- =====================================================
--  Dati di prova (facoltativo)
--  Eseguire DOPO 02_schema.sql, con il database selezionato.
--
--  Le date sono calcolate rispetto a OGGI, così ogni torneo
--  si trova sempre in uno stato diverso qualunque giorno
--  eseguiate lo script.
--
--  Gli utenti (admin e utente demo) li crea il backend al primo
--  avvio, perché la password va salvata come hash BCrypt.
-- =====================================================

INSERT INTO tornei (nome, descrizione,
                    data_inizio_iscrizioni, data_fine_iscrizioni,
                    data_inizio_torneo, data_fine_torneo, cancellato)
VALUES
-- APERTO con iscrizioni aperte
('Torneo di Calcetto d''Autunno',
 'Torneo di calcio a 5 aperto a tutti gli studenti. Squadre da 5 giocatori più 2 riserve.',
 DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY),
 DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), FALSE),

('Torneo di Ping Pong',
 'Singolare a eliminazione diretta. Racchette disponibili in palestra.',
 DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY),
 DATE_ADD(CURDATE(), INTERVAL 25 DAY), DATE_ADD(CURDATE(), INTERVAL 27 DAY), FALSE),

-- APERTO ma iscrizioni non ancora iniziate
('Torneo di Scacchi',
 'Torneo a sistema svizzero, 5 turni, cadenza 15+10.',
 DATE_ADD(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 21 DAY),
 DATE_ADD(CURDATE(), INTERVAL 28 DAY), DATE_ADD(CURDATE(), INTERVAL 29 DAY), FALSE),

-- IN CORSO
('Torneo di Basket 3x3',
 'Partite da 10 minuti o primo a 21 punti nel campetto esterno.',
 DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY),
 DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), FALSE),

-- CHIUSO
('Torneo di Pallavolo di Primavera',
 'Edizione primaverile del torneo di pallavolo mista.',
 DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_SUB(CURDATE(), INTERVAL 180 DAY),
 DATE_SUB(CURDATE(), INTERVAL 170 DAY), DATE_SUB(CURDATE(), INTERVAL 160 DAY), FALSE),

-- CANCELLATO
('Torneo di Padel',
 'Annullato per indisponibilità dei campi.',
 DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY),
 DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), TRUE);

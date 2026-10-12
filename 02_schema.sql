-- =====================================================
--  Schema del database "tornei_db"
--  Eseguire DOPO aver selezionato il database:  USE tornei_db;
--  Lo script si può rieseguire: cancella e ricrea tutto.
-- =====================================================

DROP VIEW IF EXISTS tornei_con_stato;
DROP TABLE IF EXISTS iscrizioni;
DROP TABLE IF EXISTS tornei;
DROP TABLE IF EXISTS utenti;

-- -----------------------------------------------------
-- UTENTI
-- La password è salvata solo come hash BCrypt, mai in chiaro.
-- L'admin è un utente normale con ruolo = 'ADMIN'.
-- -----------------------------------------------------
CREATE TABLE utenti (
  id                  INT AUTO_INCREMENT PRIMARY KEY,
  nome                VARCHAR(50)  NOT NULL,
  cognome             VARCHAR(50)  NOT NULL,
  email               VARCHAR(100) NOT NULL UNIQUE,
  password_hash       VARCHAR(255) NOT NULL,
  ruolo               ENUM('UTENTE','ADMIN') NOT NULL DEFAULT 'UTENTE',
  data_registrazione  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- TORNEI
-- Lo stato NON è salvato: si calcola dalle date (vedi vista sotto).
-- Viene salvato solo il flag "cancellato", deciso dall'admin.
-- -----------------------------------------------------
CREATE TABLE tornei (
  id                      INT AUTO_INCREMENT PRIMARY KEY,
  nome                    VARCHAR(100)  NOT NULL,
  descrizione             VARCHAR(2000),
  data_inizio_iscrizioni  DATE          NOT NULL,
  data_fine_iscrizioni    DATE          NOT NULL,
  data_inizio_torneo      DATE          NOT NULL,
  data_fine_torneo        DATE          NOT NULL,
  cancellato              BOOLEAN       NOT NULL DEFAULT FALSE,
  CONSTRAINT chk_iscrizioni     CHECK (data_inizio_iscrizioni <= data_fine_iscrizioni),
  CONSTRAINT chk_iscr_torneo    CHECK (data_fine_iscrizioni   <= data_inizio_torneo),
  CONSTRAINT chk_torneo         CHECK (data_inizio_torneo     <= data_fine_torneo)
);

-- -----------------------------------------------------
-- ISCRIZIONI (tabella ponte molti-a-molti utenti <-> tornei)
-- La chiave primaria composta impedisce di iscriversi due volte
-- allo stesso torneo.
-- -----------------------------------------------------
CREATE TABLE iscrizioni (
  utente_id        INT      NOT NULL,
  torneo_id        INT      NOT NULL,
  data_iscrizione  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (utente_id, torneo_id),
  CONSTRAINT fk_iscr_utente FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE CASCADE,
  CONSTRAINT fk_iscr_torneo FOREIGN KEY (torneo_id) REFERENCES tornei(id) ON DELETE CASCADE
);

CREATE INDEX idx_iscrizioni_torneo ON iscrizioni(torneo_id);

-- -----------------------------------------------------
-- VISTA: tornei con lo stato calcolato dalla data di oggi
--   cancellato            -> 'cancellato'
--   oggi > fine torneo    -> 'chiuso'
--   oggi >= inizio torneo -> 'in_corso'
--   altrimenti            -> 'aperto'
-- "iscrizioni_aperte" dice se OGGI ci si può iscrivere.
-- -----------------------------------------------------
CREATE VIEW tornei_con_stato AS
SELECT t.id,
       t.nome,
       t.descrizione,
       t.data_inizio_iscrizioni,
       t.data_fine_iscrizioni,
       t.data_inizio_torneo,
       t.data_fine_torneo,
       t.cancellato,
       CASE
         WHEN t.cancellato                     THEN 'cancellato'
         WHEN CURRENT_DATE > t.data_fine_torneo   THEN 'chiuso'
         WHEN CURRENT_DATE >= t.data_inizio_torneo THEN 'in_corso'
         ELSE 'aperto'
       END AS stato,
       CASE
         WHEN t.cancellato = FALSE
          AND CURRENT_DATE BETWEEN t.data_inizio_iscrizioni AND t.data_fine_iscrizioni
         THEN TRUE ELSE FALSE
       END AS iscrizioni_aperte,
       (SELECT COUNT(*) FROM iscrizioni i WHERE i.torneo_id = t.id) AS numero_iscritti
FROM tornei t;

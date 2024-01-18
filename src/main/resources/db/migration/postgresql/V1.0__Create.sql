-- docker compose exec postgres bash
-- psql --dbname=zulieferer --username=zulieferer [--file=/sql/V1.0__Create.sql]

CREATE TABLE IF NOT EXISTS zulieferer (
    id            uuid PRIMARY KEY USING INDEX TABLESPACE zuliefererspace,
    version       integer NOT NULL DEFAULT 0,
    name          varchar(40) NOT NULL CHECK (name ~ '[A-ZÄÖÜ][a-zäöüß]+'),
    email         varchar(40) NOT NULL UNIQUE USING INDEX TABLESPACE zuliefererspace,
    baecker_id    uuid NOT NULL,
    erzeugt       timestamp NOT NULL,
    aktualisiert  timestamp NOT NULL
) TABLESPACE zuliefererspace;

CREATE INDEX IF NOT EXISTS zulieferer_email_idx ON zulieferer(email) TABLESPACE zuliefererspace;
CREATE INDEX IF NOT EXISTS zulieferer_baecker_id_idx ON zulieferer(baecker_id) TABLESPACE zuliefererspace;

CREATE TABLE IF NOT EXISTS filiale (
    id        uuid PRIMARY KEY USING INDEX TABLESPACE zuliefererspace,
    name       char(40) NOT NULL CHECK (name ~ '[A-ZÄÖÜ][a-zäöüß]+'),
    standort  varchar(40) NOT NULL,
    zulieferer_id  uuid NOT NULL UNIQUE USING INDEX TABLESPACE zuliefererspace REFERENCES zulieferer
) TABLESPACE zuliefererspace;
CREATE INDEX IF NOT EXISTS filiale_standort_idx ON filiale(standort) TABLESPACE zuliefererspace;

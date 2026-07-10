-- Flere av tabellene manglet kolonner som finnes i kildetabellene i Oracle. Kun t_beregn_grl
-- og sa_sak_10 hadde fra før både oppdatert og db_splitt (se V1__init_schema.sql). Oppdaget
-- ved at replikering fra historisk-exodus feilet trinnvis for t_stonad (oppdatert), t_ef
-- (db_splitt), t_endring (db_splitt) og t_rolle (db_splitt) - t_vedtak er lagt til proaktivt
-- her siden den har nøyaktig samme opprinnelige mønster og med stor sannsynlighet ville
-- feilet likt.
alter table t_stonad
    add column oppdatert timestamp default current_timestamp,
    add column db_splitt char(2) default '  ';

alter table t_vedtak
    add column oppdatert timestamp default current_timestamp,
    add column db_splitt char(2) default '  ';

alter table t_ef
    add column db_splitt char(2) default '  ';

alter table t_endring
    add column db_splitt char(2) default '  ';

alter table t_rolle
    add column db_splitt char(2) default '  ';

-- exodus_status.iterator var satt til varchar(256), men cursor-verdien fra historisk-exodus-ef
-- kan være lengre enn dette.
alter table exodus_status
    alter column iterator type text;

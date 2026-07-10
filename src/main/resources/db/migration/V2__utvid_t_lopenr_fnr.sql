-- t_lopenr_fnr manglet oppdatert og db_splitt, som finnes i kildetabellen i Oracle
-- (tilsvarende mønster som t_beregn_grl/sa_sak_10). Oppdaget da replikering fra
-- historisk-exodus feilet med "column oppdatert does not exist".
alter table t_lopenr_fnr
    add column oppdatert timestamp default current_timestamp,
    add column db_splitt char(2) default '  ';

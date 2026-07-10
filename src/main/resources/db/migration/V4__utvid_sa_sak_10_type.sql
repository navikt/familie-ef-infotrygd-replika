alter table sa_sak_10
    alter column s10_undervalg type char(2);

-- Resten av sa_sak_10 sine kolonnetyper er verifisert mot faktisk Oracle-skjema (DESCRIBE av
-- kildetabellen). Flere kolonner var deklarert som char(n) i V1, men er egentlig VARCHAR2(n) i
-- Oracle. Postgres sin char(n) høyrepadder verdier med mellomrom, i motsetning til VARCHAR2, så
-- dette endrer den faktiske verdien (ikke bare visningen) for korte strenger og bør rettes opp.
alter table sa_sak_10
    alter column s10_nivaa type varchar(3),
    alter column s10_tellepunkt type varchar(3),
    alter column s10_eval_kode type varchar(4),
    alter column s10_fremlegg type varchar(3),
    alter column s10_behen_type type varchar(3),
    alter column s10_behen_enhet type varchar(4),
    alter column s10_reg_av_type type varchar(3),
    alter column s10_reg_av_enhet type varchar(4),
    alter column s10_diff_framlegg type varchar(3),
    alter column s10_innstillt_av_type type varchar(3),
    alter column s10_innstillt_av_enhet type varchar(4),
    alter column s10_vedtatt_av_type type varchar(3),
    alter column s10_vedtatt_av_enhet type varchar(4),
    alter column s10_prio_tab type varchar(6),
    alter column s10_aoe type varchar(3),
    alter column s10_reell_enhet type varchar(4),
    alter column tk_nr type varchar(4),
    alter column f_nr type varchar(11);
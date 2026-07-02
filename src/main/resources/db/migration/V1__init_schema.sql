-- Postgres-skjema for familie-ef-infotrygd-replika.
--
-- Tabellene er en replika av et utvalg av tabellene i Infotrygd (Oracle) som brukes av
-- enslig forsørger-domenet. Navn på tabeller/kolonner er beholdt (i lowercase, siden Postgres
-- foldes unquoted identifiers til lowercase) slik at dette er kompatibelt med replikeringen
-- fra kildesystemet og med de eksisterende spørringene i koden.
--
-- Oracle NUMBER er konvertert til numeric, CHAR/VARCHAR2 til char/varchar, og DATE/TIMESTAMP
-- er beholdt som date/timestamp.

create table t_lopenr_fnr (
    person_lopenr numeric not null primary key,
    personnr char(11) not null,
    opprettet timestamp not null default current_timestamp
);

create table t_stonad (
    stonad_id numeric not null primary key,
    person_lopenr numeric,
    kode_rutine char(2),
    dato_start date,
    kode_opphor char(2),
    dato_opphor date,
    oppdrag_id numeric,
    tidspunkt_opphort timestamp,
    tidspunkt_reg timestamp,
    brukerid char(8),
    opprettet timestamp not null default current_timestamp
);

create table t_vedtak (
    vedtak_id numeric not null primary key,
    person_lopenr numeric not null,
    kode_rutine char(2),
    dato_start date,
    tknr char(4),
    saksblokk char(1),
    saksnr numeric,
    type_sak char(2),
    kode_resultat char(2),
    dato_innv_fom date,
    dato_innv_tom date,
    dato_mottatt_sak date,
    kode_vedtaksnivaa char(3),
    type_beregning char(3),
    tknr_beh char(4),
    tidspunkt_reg timestamp,
    brukerid char(8),
    nokkel_dl1 char(30),
    alternativ_mottaker numeric(11),
    stonad_id numeric,
    kidnr varchar(25),
    faktnr varchar(33),
    opprettet timestamp not null default current_timestamp
);

create table t_endring (
    vedtak_id numeric(10, 0) not null,
    kode varchar(2) not null,
    opprettet timestamp default current_timestamp,
    oppdatert timestamp default current_timestamp,
    primary key (vedtak_id, kode)
);

create table t_rolle (
    vedtak_id numeric(10, 0) not null,
    type varchar(2) not null,
    tidspunkt_reg timestamp not null,
    fom date not null,
    tom date,
    person_lopenr_r numeric(10, 0) not null,
    brukerid varchar(8) not null,
    barn_type varchar(2),
    bor_sammen_med numeric(1, 0),
    trygdetid_faktisk numeric(4, 0),
    trygdetid_anvendt numeric(4, 0),
    trygdetid_unntak char(1),
    trygd_medlem_siden numeric(4, 0),
    utenlandsopphold char(1),
    bt_1_sum numeric(5, 0),
    bt_1_antall numeric(2, 0),
    bt_2_sum numeric(5, 0),
    bt_2_antall numeric(2, 0),
    bt_s_sum numeric(5, 0),
    bt_s_antall numeric(2, 0),
    opprettet timestamp default current_timestamp,
    oppdatert timestamp default current_timestamp,
    primary key (vedtak_id, type, tidspunkt_reg, person_lopenr_r)
);

create table t_ef (
    vedtak_id numeric(10, 0) not null,
    -- PK er egentlig (vedtak_id, tidspunkt_reg) i Oracle, siden det finnes historikk-rader
    -- for samme vedtak_id med ulik tidspunkt_reg.
    tidspunkt_reg timestamp not null default current_timestamp,
    samordning char(1),
    aktivitet char(1),
    overgang char(1),
    kode_overg varchar(6),
    barnt_utg numeric(5, 0),
    utdanning numeric(5, 0),
    tilsk_flytting numeric(5, 0),
    mangl_arbeid char(1),
    mangl_btil char(1),
    stonad_belop numeric(6, 0),
    innt_fradrag numeric(6, 0),
    netto_belop numeric(6, 0),
    brukerid varchar(8),
    ant_mnd numeric(2, 0),
    sam_fradrag numeric(6, 0),
    opprettet timestamp default current_timestamp,
    oppdatert timestamp default current_timestamp,
    primary key (vedtak_id, tidspunkt_reg)
);

create table t_beregn_grl (
    vedtak_id numeric not null,
    type_belop varchar(4) not null,
    tidspunkt_reg timestamp not null default current_timestamp,
    fom date not null,
    tom date,
    belop numeric(11, 2) not null,
    brukerid varchar(8) not null,
    opprettet timestamp default current_timestamp,
    oppdatert timestamp default current_timestamp,
    db_splitt char(2) default '  ',
    primary key (vedtak_id, type_belop, tidspunkt_reg)
);

create table sa_sak_10 (
    s01_personkey numeric(15),
    s05_saksblokk char(1),
    s10_saksnr char(2),
    s10_reg_dato numeric(8),
    s10_mottattdato numeric(8),
    s10_kapittelnr char(2),
    s10_valg char(2),
    s10_undervalg char(1),
    s10_dublett_feil char(1),
    s10_type char(2),
    s10_innstilling char(2),
    s10_resultat char(2),
    s10_nivaa char(3),
    s10_innstilldato numeric(8),
    s10_vedtaksdato numeric(8),
    s10_iverksattdato numeric(8),
    s10_grunnbl_dato numeric(8),
    s10_aarsakskode char(2),
    s10_tellepunkt char(3),
    s10_telletype char(1),
    s10_telledato numeric(8),
    s10_eval_kode char(4),
    s10_eval_tir char(1),
    s10_fremlegg char(3),
    s10_innstilling2 char(2),
    s10_innstilldato2 numeric(8),
    s10_annen_instans char(1),
    s10_behen_type char(3),
    s10_behen_enhet char(4),
    s10_reg_av_type char(3),
    s10_reg_av_enhet char(4),
    s10_diff_framlegg char(3),
    s10_innstillt_av_type char(3),
    s10_innstillt_av_enhet char(4),
    s10_vedtatt_av_type char(3),
    s10_vedtatt_av_enhet char(4),
    s10_prio_tab char(6),
    s10_aoe char(3),
    s10_es_system char(1),
    s10_es_gsak_oppdragsid numeric(10),
    s10_knyttet_til_sak char(2),
    s10_vedtakstype char(1),
    s10_reell_enhet char(4),
    s10_mod_endret char(1),
    tk_nr char(4),
    f_nr char(11),
    opprettet timestamp default current_timestamp,
    oppdatert timestamp default current_timestamp,
    endret_i_kilde timestamp default current_timestamp,
    kilde_is varchar(12) default ' ',
    region char(1) default ' ',
    db_splitt char(2) default ' ',
    id_sak numeric not null primary key
);

create index idx_t_lopenr_fnr_personnr on t_lopenr_fnr (personnr);
create index idx_t_stonad_person_lopenr on t_stonad (person_lopenr);
create index idx_t_vedtak_stonad_id on t_vedtak (stonad_id);
create index idx_t_vedtak_person_lopenr on t_vedtak (person_lopenr);
create index idx_t_rolle_person_lopenr_r on t_rolle (person_lopenr_r);
create index idx_sa_sak_10_f_nr on sa_sak_10 (f_nr);

-- Styringstabell for replikering fra historisk-exodus (on-prem) til denne GCP-postgres-basen.
--
-- iterator er en cursor levert av exodus sitt /api/hentUttrekk-endepunkt, og brukes
-- til å be om neste side med data for en gitt tabell. job_status brukes til å registrere at exodus
-- har re-eksportert tabellen fra Oracle (NY_BASELINE), som gjør at den lokale iteratoren er ugyldig
-- og at tabellen må trunkeres og replikeres på nytt fra bunnen av.
create table exodus_status (
    tabell varchar(64) not null primary key,
    iterator varchar(256),
    job_status varchar(32) not null default 'OK',
    antall_rader_hentet bigint not null default 0,
    sist_oppdatert timestamp not null default current_timestamp
);

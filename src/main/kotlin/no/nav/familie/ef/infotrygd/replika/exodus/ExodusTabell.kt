package no.nav.familie.ef.infotrygd.replika.exodus

/**
 * Alle tabellene som skal repliseres fra historisk-exodus, med den naturlige nøkkelen fra Oracle
 * som brukes til å upserte rader i Postgres. Kolonnenavn er alltid lowercase siden det er slik
 * Postgres folder unquoted identifiers (se V1__init_schema.sql).
 *
 * t_lopenr_fnr, t_stonad, t_vedtak og t_ef har egen primærnøkkel i skjemaet fra før. t_endring,
 * t_rolle, t_beregn_grl og sa_sak_10 har fått en tilsvarende sammensatt/enkel nøkkel lagt til i
 * V1__init_schema.sql for å støtte upsert her.
 */
enum class ExodusTabell(
    val tabellNavn: String,
    val primærnøkkel: List<String>,
) {
    T_LOPENR_FNR("t_lopenr_fnr", listOf("person_lopenr")),
    T_STONAD("t_stonad", listOf("stonad_id")),
    T_VEDTAK("t_vedtak", listOf("vedtak_id")),
    T_EF("t_ef", listOf("vedtak_id", "tidspunkt_reg")),
    T_ENDRING("t_endring", listOf("vedtak_id", "kode")),
    T_ROLLE("t_rolle", listOf("vedtak_id", "type", "tidspunkt_reg", "person_lopenr_r")),
    T_BEREGN_GRL("t_beregn_grl", listOf("vedtak_id", "type_belop", "tidspunkt_reg")),
    SA_SAK_10("sa_sak_10", listOf("id_sak")),
    ;

    companion object {
        fun fraTabellNavn(navn: String): ExodusTabell =
            entries.find { it.tabellNavn.equals(navn, ignoreCase = true) }
                ?: throw IllegalArgumentException("Ukjent exodus-tabell: $navn")
    }
}

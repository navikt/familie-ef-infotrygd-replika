package no.nav.familie.ef.infotrygd.replika.exodus

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * Generisk upsert av rader fra historisk-exodus, basert på den naturlige nøkkelen definert i
 * ExodusTabell. Radene kommer som Map<kolonnenavn, String?> siden hentUttrekk-responsen
 * serialiserer alle Oracle-verdier (numeric/date/timestamp/varchar) som String - se
 * HentUttrekkResponse.tilRader(). Postgres gjør ikke implisitt cast fra tekst til numeric/date/
 * timestamp, så hver verdi castes eksplisitt til kolonnens faktiske type (slått opp via
 * information_schema og cachet per tabell).
 */
@Repository
class ExodusUpsertRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    private val kolonnetyperPerTabell = ConcurrentHashMap<String, Map<String, String>>()

    fun upsert(
        tabell: ExodusTabell,
        rader: List<Map<String, String?>>,
    ) {
        if (rader.isEmpty()) return

        val kolonner = rader.first().keys.map { it.lowercase() }.distinct()
        val primærnøkkel = tabell.primærnøkkel
        val settKolonner = kolonner - primærnøkkel.toSet()
        val kolonnetyper = hentKolonnetyper(tabell.tabellNavn)

        val sql =
            buildString {
                append("INSERT INTO ${tabell.tabellNavn} (${kolonner.joinToString(", ")}) ")
                append("VALUES (${kolonner.joinToString(", ") { "CAST(:$it AS ${kolonnetyper[it] ?: "text"})" }}) ")
                append("ON CONFLICT (${primærnøkkel.joinToString(", ")}) ")
                if (settKolonner.isEmpty()) {
                    append("DO NOTHING")
                } else {
                    append("DO UPDATE SET ${settKolonner.joinToString(", ") { "$it = EXCLUDED.$it" }}")
                }
            }

        val parametre =
            rader
                .map { rad -> MapSqlParameterSource(rad.mapKeys { (kolonnenavn, _) -> kolonnenavn.lowercase() }) }
                .toTypedArray()

        jdbcTemplate.batchUpdate(sql, parametre)
    }

    /** Henter Postgres sitt interne typenavn (udt_name) per kolonne, f.eks. numeric/date/timestamp/bpchar/varchar. */
    private fun hentKolonnetyper(tabellNavn: String): Map<String, String> =
        kolonnetyperPerTabell.getOrPut(tabellNavn) {
            jdbcTemplate.jdbcTemplate
                .query(
                    "select column_name, udt_name from information_schema.columns where table_name = ?",
                    { rs, _ -> rs.getString("column_name") to rs.getString("udt_name") },
                    tabellNavn,
                ).toMap()
        }

    /** Brukes når exodus varsler NY_BASELINE - lokale data er ikke lenger gyldige. */
    fun truncate(tabell: ExodusTabell) {
        jdbcTemplate.jdbcTemplate.execute("TRUNCATE TABLE ${tabell.tabellNavn}")
    }
}

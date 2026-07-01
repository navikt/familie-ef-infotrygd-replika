package no.nav.familie.ef.infotrygd.replika.repository

import no.nav.familie.ef.infotrygd.replika.model.StønadType
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class InfotrygdRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun harAktivStønad(personIdenter: Set<String>) = harStønad(personIdenter, kunAktive = true)

    fun harStønad(
        personIdenter: Set<String>,
        kunAktive: Boolean = false,
        dagensDato: LocalDate = LocalDate.now(),
    ): List<Pair<String, StønadType>> {
        val values =
            MapSqlParameterSource()
                .addValue("personIdenter", personIdenter)
                .addValue("kodeRutiner", StønadType.values().map { it.kodeRutine })
        val filter: String =
            if (kunAktive) {
                values.addValue("dagensDato", dagensDato)
                " AND coalesce(S.DATO_OPPHOR,V.DATO_INNV_TOM) > :dagensDato "
            } else {
                ""
            }

        val result =
            jdbcTemplate.query(
                """
            SELECT L.PERSONNR, S.KODE_RUTINE 
              FROM T_LOPENR_FNR L
              JOIN T_STONAD S ON S.PERSON_LOPENR = L.PERSON_LOPENR
              JOIN T_VEDTAK V ON V.stonad_id = S.stonad_id
            WHERE L.PERSONNR IN (:personIdenter)
              AND S.KODE_RUTINE IN (:kodeRutiner)
              $filter
            GROUP BY L.personnr, S.KODE_RUTINE
        """,
                values,
            ) { resultSet, _ ->
                Pair(
                    resultSet.getString("PERSONNR"),
                    StønadType.fraKodeRutine(resultSet.getString("KODE_RUTINE")),
                )
            }
        return result.toList()
    }
}

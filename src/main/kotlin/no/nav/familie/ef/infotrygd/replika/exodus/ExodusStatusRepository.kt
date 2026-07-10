package no.nav.familie.ef.infotrygd.replika.exodus

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * Leser/skriver fremdriften (iterator + job_status) for hver tabell som repliseres fra
 * historisk-exodus. Se V1__init_schema.sql for skjema.
 */
@Repository
class ExodusStatusRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun finn(tabell: ExodusTabell): ExodusStatus? =
        jdbcTemplate
            .query(
                "SELECT * FROM exodus_status WHERE tabell = :tabell",
                MapSqlParameterSource("tabell", tabell.tabellNavn),
            ) { rs, _ ->
                ExodusStatus(
                    tabell = rs.getString("tabell"),
                    iterator = rs.getString("iterator"),
                    jobStatus = JobStatus.valueOf(rs.getString("job_status")),
                    antallRaderHentet = rs.getLong("antall_rader_hentet"),
                    sistOppdatert = rs.getTimestamp("sist_oppdatert").toLocalDateTime(),
                )
            }.firstOrNull()

    /**
     * Oppdaterer iterator og teller opp antall hentede rader. Setter job_status til PAGINERER
     * hvis det finnes flere sider å hente, ellers OK (tabellen er ajour).
     */
    fun oppdaterIterator(
        tabell: ExodusTabell,
        nyIterator: String?,
        antallNyeRader: Int,
        flereSider: Boolean,
    ) {
        val jobStatus = if (flereSider) JobStatus.PAGINERER else JobStatus.OK
        jdbcTemplate.update(
            """
            INSERT INTO exodus_status (tabell, iterator, job_status, antall_rader_hentet, sist_oppdatert)
            VALUES (:tabell, :iterator, :jobStatus, :antallNyeRader, current_timestamp)
            ON CONFLICT (tabell) DO UPDATE SET
                iterator = EXCLUDED.iterator,
                job_status = :jobStatus,
                antall_rader_hentet = exodus_status.antall_rader_hentet + :antallNyeRader,
                sist_oppdatert = current_timestamp
            """,
            MapSqlParameterSource()
                .addValue("tabell", tabell.tabellNavn)
                .addValue("iterator", nyIterator)
                .addValue("jobStatus", jobStatus.name)
                .addValue("antallNyeRader", antallNyeRader),
        )
    }

    /** Nullstiller iterator og markerer at tabellen må replikeres på nytt fra bunnen av. */
    fun settNyBaseline(tabell: ExodusTabell) {
        jdbcTemplate.update(
            """
            INSERT INTO exodus_status (tabell, iterator, job_status, antall_rader_hentet, sist_oppdatert)
            VALUES (:tabell, NULL, 'NY_BASELINE', 0, current_timestamp)
            ON CONFLICT (tabell) DO UPDATE SET
                iterator = NULL,
                job_status = 'NY_BASELINE',
                antall_rader_hentet = 0,
                sist_oppdatert = current_timestamp
            """,
            MapSqlParameterSource("tabell", tabell.tabellNavn),
        )
    }
}

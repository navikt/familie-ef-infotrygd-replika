package no.nav.familie.ef.infotrygd.replika.exodus

import java.time.LocalDateTime

enum class JobStatus {
    OK,
    PAGINERER,
    NY_BASELINE,
}

data class ExodusStatus(
    val tabell: String,
    val iterator: String?,
    val jobStatus: JobStatus,
    val antallRaderHentet: Long,
    val sistOppdatert: LocalDateTime,
)

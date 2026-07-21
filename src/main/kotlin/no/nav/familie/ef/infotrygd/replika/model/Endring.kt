package no.nav.familie.ef.infotrygd.replika.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Suppress("unused") // brukes av hibernate for å generere hvilke tabeller som brukes
@Entity
@Table(name = "T_ENDRING")
data class Endring(
    @Id
    @Column(name = "VEDTAK_ID")
    val id: Long,
    @Column(name = "KODE")
    val kode: String,
)

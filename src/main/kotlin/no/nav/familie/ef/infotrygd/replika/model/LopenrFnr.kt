package no.nav.familie.ef.infotrygd.replika.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Suppress("unused") // brukes av hibernate for å generere hvilke tabeller som brukes
@Entity
@Table(name = "T_LOPENR_FNR")
data class LopenrFnr(
    @Id
    @Column(name = "PERSON_LOPENR")
    val personKey: Long,
    @Column(name = "PERSONNR")
    val fnr: String,
)

package no.nav.familie.ef.infotrygd.replika.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Suppress("unused") // brukes av hibernate for å generere hvilke tabeller som brukes
@Entity
@Table(name = "T_BEREGN_GRL")
data class BeregningGrunnlag(
    @Id
    @Column(name = "VEDTAK_ID")
    val id: Long,
    @Column(name = "BELOP")
    val beløp: Long,
    @Column(name = "TYPE_BELOP")
    val type: String,
)

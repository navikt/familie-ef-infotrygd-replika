package no.nav.familie.ef.infotrygd.replika.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Suppress("unused") // brukes av hibernate for å generere hvilke tabeller som brukes
@Entity
@Table(name = "T_EF")
data class EF(
    @Id
    @Column(name = "VEDTAK_ID")
    val id: Long,
    @Column(name = "STONAD_BELOP")
    val stønadsbeløp: Long,
    @Column(name = "BARNT_UTG")
    val utgifterBarnetilsyn: Long,
    @Column(name = "INNT_FRADRAG")
    val inntektsfradrag: Long,
    @Column(name = "SAM_FRADRAG")
    val samordningsfradrag: Long,
    @Column(name = "netto_belop")
    val nettobeløp: String,
    @Column(name = "KODE_OVERG")
    val kodeOvergangsstønad: String,
    @Column(name = "AKTIVITET")
    val aktivitet: String,
)

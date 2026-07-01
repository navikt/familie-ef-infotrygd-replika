package no.nav.familie.ef.infotrygd.replika.perioder

import no.nav.familie.ef.infotrygd.replika.rest.api.Periode
import no.nav.familie.kontrakter.felles.ef.Datakilde
import java.time.LocalDate

data class InternPeriode(
    val personIdent: String,
    val inntektsreduksjon: Int,
    val samordningsfradrag: Int,
    val utgifterBarnetilsyn: Int,
    val månedsbeløp: Int,
    val engangsbeløp: Int,
    val stønadFom: LocalDate,
    val stønadTom: LocalDate,
    val opphørsdato: LocalDate?,
    val datakilde: Datakilde,
)

fun Periode.tilInternPeriode(): InternPeriode =
    InternPeriode(
        personIdent = this.personIdent,
        inntektsreduksjon = this.inntektsreduksjon,
        samordningsfradrag = this.samordningsfradrag,
        utgifterBarnetilsyn = this.utgifterBarnetilsyn,
        månedsbeløp = this.månedsbeløp,
        engangsbeløp = this.engangsbeløp,
        stønadFom = this.stønadFom,
        stønadTom = this.stønadTom,
        opphørsdato = this.opphørsdato,
        datakilde = Datakilde.INFOTRYGD,
    )

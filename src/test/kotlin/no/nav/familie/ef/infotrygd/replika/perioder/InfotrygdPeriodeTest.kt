package no.nav.familie.ef.infotrygd.replika.perioder

import no.nav.familie.ef.infotrygd.replika.perioder.InfotrygdPeriodeTestUtil.lagInfotrygdPeriode
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdEndringKode
import no.nav.familie.ef.infotrygd.replika.rest.api.Periode
import no.nav.familie.ef.infotrygd.replika.utils.InfotrygdPeriodeUtil.slåSammenInfotrygdperioder
import no.nav.familie.ef.infotrygd.replika.utils.isEqualOrAfter
import no.nav.familie.ef.infotrygd.replika.utils.isEqualOrBefore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class InfotrygdPeriodeTest {
    companion object {
        private val FOM = LocalDate.of(2021, 3, 1)
        private val TOM = LocalDate.of(2021, 5, 31)

        private val FØR_STARTDATO_FOM = LocalDate.of(2021, 1, 1)
        private val FØR_STARTDATO_TOM = LocalDate.of(2021, 1, 31)
        private val ETTER_SLUTDATO_FOM = LocalDate.of(2021, 7, 1)
        private val ETTER_SLUTTDATO_TOM = LocalDate.of(2021, 7, 31)
        private val MIDT_I_FOM = LocalDate.of(2021, 3, 1)
        private val MIDT_I_TOM = LocalDate.of(2021, 3, 31)
    }

    private val startperiode = lagPeriode(FOM, TOM)

    @Test
    internal fun `erInfotrygdPeriodeOverlappende - ikke overlappende`() {
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(FØR_STARTDATO_FOM, FØR_STARTDATO_TOM)))
            .withFailMessage("Perioden starter og slutter før startperioden")
            .isFalse

        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(ETTER_SLUTDATO_FOM, ETTER_SLUTTDATO_TOM)))
            .withFailMessage("Perioden starter og slutter etter startperioden")
            .isFalse
    }

    @Test
    internal fun `erInfotrygdPeriodeOverlappende - overlappende startdato starter før`() {
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(FØR_STARTDATO_FOM, MIDT_I_TOM)))
            .withFailMessage("Perioden starter før og slutter midt i")
            .isTrue
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(FØR_STARTDATO_FOM, TOM)))
            .withFailMessage("Perioden starter før og slutter samtidig")
            .isTrue
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(FØR_STARTDATO_FOM, ETTER_SLUTTDATO_TOM)))
            .withFailMessage("Perioden starter før og slutter etter")
            .isTrue
    }

    @Test
    internal fun `erInfotrygdPeriodeOverlappende - overlappende startdato starter samtidig`() {
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(FOM, MIDT_I_TOM)))
            .withFailMessage("Perioden starter samtidig og slutter midt i")
            .isTrue
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(FOM, TOM)))
            .withFailMessage("Perioden starter og slutter samtidig")
            .isTrue
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(FOM, ETTER_SLUTTDATO_TOM)))
            .withFailMessage("Perioden starter og slutter etter")
            .isTrue
    }

    @Test
    internal fun `erInfotrygdPeriodeOverlappende - overlappende startdato starter midt i`() {
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(MIDT_I_FOM, MIDT_I_TOM)))
            .withFailMessage("Perioden starter midt i og slutter midt i")
            .isTrue
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(MIDT_I_FOM, TOM)))
            .withFailMessage("Perioden starter midt i og slutter samtidig")
            .isTrue
        assertThat(startperiode.erPeriodeOverlappende(lagPeriode(MIDT_I_FOM, ETTER_SLUTTDATO_TOM)))
            .withFailMessage("Perioden starter midt i og slutter etter")
            .isTrue
    }

    @Test
    internal fun `skal filtrere vekk perioder som er annulert eller uaktuelle`() {
        val perioder =
            slåSammenInfotrygdperioder(
                listOf(
                    lagInfotrygdPeriode(
                        stønadFom = LocalDate.parse("2021-01-01"),
                        stønadTom = LocalDate.parse("2021-01-02"),
                        beløp = 1,
                        kode = InfotrygdEndringKode.ANNULERT,
                    ),
                    lagInfotrygdPeriode(
                        stønadFom = LocalDate.parse("2021-02-01"),
                        stønadTom = LocalDate.parse("2021-02-02"),
                        beløp = 2,
                    ),
                    lagInfotrygdPeriode(
                        stønadFom = LocalDate.parse("2021-03-01"),
                        stønadTom = LocalDate.parse("2021-03-02"),
                        beløp = 3,
                        kode = InfotrygdEndringKode.UAKTUELL,
                    ),
                ),
            )

        assertThat(perioder).hasSize(1)
        assertThat(perioder[0].månedsbeløp).isEqualTo(2)
    }

    private fun Periode.omslutter(periode: Periode) = periode.stønadFom.isBefore(stønadFom) && periode.stønadTom.isAfter(stønadTom)

    private fun Periode.erDatoInnenforPeriode(dato: LocalDate): Boolean = dato.isEqualOrBefore(stønadTom) && dato.isEqualOrAfter(stønadFom)

    private fun Periode.erPeriodeOverlappende(periode: Periode): Boolean =
        (erDatoInnenforPeriode(periode.stønadFom) || erDatoInnenforPeriode(periode.stønadTom)) ||
            omslutter(periode)

    private fun lagPeriode(
        fom: LocalDate,
        tom: LocalDate,
    ) = InfotrygdPeriodeTestUtil.lagInfotrygdPeriode(stønadFom = fom, stønadTom = tom)
}

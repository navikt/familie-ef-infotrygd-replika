package no.nav.familie.ef.infotrygd.replika.service

import io.mockk.every
import io.mockk.mockk
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ef.infotrygd.replika.model.StønadType.BARNETILSYN
import no.nav.familie.ef.infotrygd.replika.model.StønadType.OVERGANGSSTØNAD
import no.nav.familie.ef.infotrygd.replika.repository.PeriodeRepository
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdEndringKode
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSakstype
import no.nav.familie.ef.infotrygd.replika.rest.api.Periode
import no.nav.familie.ef.infotrygd.replika.rest.api.PeriodeRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PeriodeServiceTest {
    val periodeRepository = mockk<PeriodeRepository>()
    val periodeService: PeriodeService = PeriodeService(periodeRepository)

    private val request =
        PeriodeRequest(
            personIdenter = setOf(FoedselsNr("01015450572")),
            stønadstyper = setOf(BARNETILSYN, OVERGANGSSTØNAD),
        )

    @BeforeEach
    fun setUp() {
        every { periodeRepository.hentBarnForPerioder(any()) } returns emptyMap()
    }

    @Test
    fun `Skal legge til barn på barnetilsyn - andre skal være uendret`() {
        val uendretPeriode = lagPeriode()
        every { periodeRepository.hentPerioder(any()) } returns
            listOf(
                Pair(BARNETILSYN, lagPeriode(vedtakId = 35L)),
                Pair(OVERGANGSSTØNAD, uendretPeriode),
            )
        every { periodeRepository.hentBarnForPerioder(any()) } returns mapOf(35L to listOf("123"))

        val perioder = periodeService.hentPerioder(request = request)
        assertThat(perioder[OVERGANGSSTØNAD]!!.first().barnIdenter).isEmpty()
        assertThat(uendretPeriode).isEqualTo(perioder[OVERGANGSSTØNAD]!!.first())
        assertThat(perioder[BARNETILSYN]!!.first().barnIdenter.first()).isEqualTo("123")
    }

    @Test
    fun `Ingen barnetilsynbarn funnet - skal ikke feile hvis det ikke finnes barn`() {
        val uendretPeriode = lagPeriode()
        every { periodeRepository.hentPerioder(any()) } returns
            listOf(
                Pair(BARNETILSYN, lagPeriode(vedtakId = 35L)),
                Pair(OVERGANGSSTØNAD, uendretPeriode),
            )

        val perioder = periodeService.hentPerioder(request = request)
        val barnetilsynPerioderHentet = perioder[BARNETILSYN]!!.first()
        assertThat(perioder[OVERGANGSSTØNAD]!!.first().barnIdenter).isEmpty()
        assertThat(barnetilsynPerioderHentet.barnIdenter).isEmpty()
    }

    @Test
    fun `Ingen barnetilsynbarn funnet - skal bruke forrige periode sine barn hvis det finnes`() {
        val uendretPeriode = lagPeriode()
        every { periodeRepository.hentPerioder(any()) } returns
            listOf(
                Pair(
                    BARNETILSYN,
                    lagPeriode(vedtakId = 35L, "FI", stønadFom = LocalDate.MIN.plusDays(4), stønadTom = LocalDate.MAX),
                ),
                Pair(
                    BARNETILSYN,
                    lagPeriode(vedtakId = 34L, stønadFom = LocalDate.MIN, stønadTom = LocalDate.MIN.plusDays(3)),
                ),
                Pair(OVERGANGSSTØNAD, uendretPeriode),
            )
        every { periodeRepository.hentBarnForPerioder(any()) } returns mapOf(34L to listOf("123"))

        val perioder = periodeService.hentPerioder(request = request)
        val barnetilsynPerioderHentet = perioder[BARNETILSYN]!!.associateBy { it.vedtakId }
        assertThat(barnetilsynPerioderHentet[34L]!!.barnIdenter).isNotEmpty
        assertThat(barnetilsynPerioderHentet[35L]!!.barnIdenter).isNotEmpty
    }

    @Test
    fun `skal filtrere vekk perioder som mangler oppdragId med beløp`() {
        val perioderMedOppdragId =
            listOf(
                lagPeriode(oppdragId = 1, månedsbeløp = 0, engangsbeløp = 1),
                lagPeriode(oppdragId = 1, månedsbeløp = 1, engangsbeløp = 0),
                lagPeriode(oppdragId = 1, månedsbeløp = 0, engangsbeløp = 0),
            )
        val periodeUtenOppdragIdUtenBeløp = lagPeriode(oppdragId = null, månedsbeløp = 0, engangsbeløp = 0)
        val perioderUtenOppdragIdMedBeløp =
            listOf(
                lagPeriode(oppdragId = null, månedsbeløp = 0, engangsbeløp = 1),
                lagPeriode(oppdragId = null, månedsbeløp = 1, engangsbeløp = 0),
                lagPeriode(oppdragId = null, månedsbeløp = 1, engangsbeløp = 1),
            )

        val dbPerioder = perioderMedOppdragId + periodeUtenOppdragIdUtenBeløp + perioderUtenOppdragIdMedBeløp
        every { periodeRepository.hentPerioder(any()) } returns dbPerioder.map { Pair(OVERGANGSSTØNAD, it) }

        val perioder = periodeService.hentPerioder(request).values.flatten()

        assertThat(perioder).containsExactlyInAnyOrderElementsOf(perioderMedOppdragId + periodeUtenOppdragIdUtenBeløp)
    }

    private fun lagPeriode(
        vedtakId: Long = 1,
        vedtakKodeResultat: String = "I",
        stønadFom: LocalDate = LocalDate.MIN,
        stønadTom: LocalDate = LocalDate.MAX,
        månedsbeløp: Int = 0,
        engangsbeløp: Int = 0,
        oppdragId: Int? = 1,
    ) = Periode(
        personIdent = "123",
        sakstype = InfotrygdSakstype.SØKNAD,
        kode = InfotrygdEndringKode.FØRSTEGANGSVEDTAK,
        kodeOvergangsstønad = null,
        aktivitetstype = null,
        brukerId = "",
        stønadId = 0,
        vedtakId = vedtakId,
        vedtakstidspunkt = LocalDateTime.MIN,
        engangsbeløp = engangsbeløp,
        inntektsgrunnlag = 0,
        inntektsreduksjon = 0,
        samordningsfradrag = 0,
        utgifterBarnetilsyn = 0,
        månedsbeløp = månedsbeløp,
        startDato = LocalDate.MIN,
        stønadFom = stønadFom,
        stønadTom = stønadTom,
        opphørsdato = null,
        barnIdenter = emptyList(),
        vedtakKodeResultat = vedtakKodeResultat,
        oppdragId = oppdragId,
    )
}

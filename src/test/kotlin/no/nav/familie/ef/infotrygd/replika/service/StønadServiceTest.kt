package no.nav.familie.ef.infotrygd.replika.service

import no.nav.familie.ef.infotrygd.replika.model.StønadType
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSøkRequest
import no.nav.familie.ef.infotrygd.replika.utils.reverserFnr
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrasjonstest")
internal class StønadServiceTest {
    @Autowired lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var stønadService: StønadService

    private val startdato = LocalDate.now().minusDays(50)
    private val sluttdato = LocalDate.now().plusDays(50)

    @BeforeEach
    fun setup() {
        // Legger inn en med opphør bak i tid
        jdbcTemplate.update("INSERT INTO T_LOPENR_FNR (PERSON_LOPENR, PERSONNR) VALUES (1, '$IDENT')")
        leggInnStønad(1, StønadType.OVERGANGSSTØNAD, LocalDate.now().minusDays(100))
        leggInnVedtak(1, StønadType.OVERGANGSSTØNAD)

        // Legger inn en med opphør frem i tid
        jdbcTemplate.update("INSERT INTO T_LOPENR_FNR (PERSON_LOPENR, PERSONNR) VALUES (2, '$IDENT2')")
        leggInnStønad(2, StønadType.SKOLEPENGER, LocalDate.now().plusDays(100))
        leggInnVedtak(2, StønadType.SKOLEPENGER)

        // Legger inn en uten opphør
        jdbcTemplate.update("INSERT INTO T_LOPENR_FNR (PERSON_LOPENR, PERSONNR) VALUES (3, '$IDENT3')")
        leggInnStønad(3, StønadType.BARNETILSYN, null)
        leggInnVedtak(3, StønadType.BARNETILSYN)

        jdbcTemplate.update("INSERT INTO sa_sak_10 (f_nr, s10_kapittelnr, s10_valg) VALUES ('${IDENT4.reverserFnr()}', 'EF', 'OG')")
    }

    private fun leggInnStønad(
        id: Int,
        stønadType: StønadType,
        opphørsdato: LocalDate?,
    ) {
        jdbcTemplate.update(
            """INSERT INTO T_STONAD (STONAD_ID, OPPDRAG_ID, PERSON_LOPENR, KODE_RUTINE, DATO_START, DATO_OPPHOR)
                                         VALUES (?, ?, ?, ?, CURRENT_DATE, ?)""",
            id,
            id,
            id,
            stønadType.kodeRutine,
            opphørsdato,
        )
    }

    private fun leggInnVedtak(
        id: Int,
        stønadType: StønadType,
    ) {
        jdbcTemplate.update(
            """INSERT INTO T_VEDTAK (VEDTAK_ID, PERSON_LOPENR, STONAD_ID, KODE_RUTINE, DATO_INNV_FOM,
                             DATO_INNV_TOM) VALUES (?,?,?,?,?,?)""",
            id,
            id,
            id,
            stønadType.kodeRutine,
            startdato,
            sluttdato,
        )
    }

    @AfterEach
    fun tearDown() {
        listOf("T_LOPENR_FNR", "T_STONAD", "T_VEDTAK", "sa_sak_10").forEach {
            jdbcTemplate.update("TRUNCATE TABLE $it")
        }
    }

    @Test
    fun `opphør bak i tid - har vedtak, men ikke aktiv`() {
        val stønadType = StønadType.OVERGANGSSTØNAD
        val finnes = stønadService.finnesIInfotrygd(InfotrygdSøkRequest(setOf(IDENT)))

        assertThat(finnes.vedtak).hasSize(1)
        assertThat(finnes.saker).isEmpty()

        val vedtakstreff = finnes.vedtak.first()
        assertThat(vedtakstreff.stønadType).isEqualTo(stønadType)
        assertThat(vedtakstreff.harLøpendeVedtak).isFalse
    }

    @Test
    fun `opphør frem i tid - har aktiv vedtak`() {
        val stønadType = StønadType.SKOLEPENGER
        val finnes = stønadService.finnesIInfotrygd(InfotrygdSøkRequest(setOf(IDENT2)))

        assertThat(finnes.vedtak).hasSize(1)
        assertThat(finnes.saker).isEmpty()

        val vedtakstreff = finnes.vedtak.first()
        assertThat(vedtakstreff.stønadType).isEqualTo(stønadType)
        assertThat(vedtakstreff.harLøpendeVedtak).isTrue
    }

    @Test
    fun `opphør er null - har aktiv vedtak`() {
        val stønadType = StønadType.BARNETILSYN
        val finnes = stønadService.finnesIInfotrygd(InfotrygdSøkRequest(setOf(IDENT3)))

        assertThat(finnes.vedtak).hasSize(1)
        assertThat(finnes.saker).isEmpty()

        val vedtakstreff = finnes.vedtak.first()
        assertThat(vedtakstreff.stønadType).isEqualTo(stønadType)
        assertThat(vedtakstreff.harLøpendeVedtak).isTrue
    }

    @Test
    fun `ident4 har ikke noen vedtak, men har en sak`() {
        val finnes = stønadService.finnesIInfotrygd(InfotrygdSøkRequest(setOf(IDENT4)))

        assertThat(finnes.vedtak).isEmpty()
        assertThat(finnes.saker).hasSize(1)

        val sak = finnes.saker.first()
        assertThat(sak.personIdent).isEqualTo(IDENT4)
        assertThat(sak.stønadType).isEqualTo(StønadType.OVERGANGSSTØNAD)
    }

    companion object {
        const val IDENT = "01234567890"
        const val IDENT2 = "01234567891"
        const val IDENT3 = "01234567892"
        const val IDENT4 = "01234567893"
    }
}

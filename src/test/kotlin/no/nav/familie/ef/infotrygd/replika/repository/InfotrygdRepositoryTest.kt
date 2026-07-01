package no.nav.familie.ef.infotrygd.replika.repository

import no.nav.familie.ef.infotrygd.replika.model.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrasjonstest")
internal class InfotrygdRepositoryTest {
    @Autowired lateinit var infotrygdRepository: InfotrygdRepository

    @Autowired lateinit var jdbcTemplate: JdbcTemplate

    private val personIdent = "01234567890"
    private val personIdent2 = "01234567891"

    private val startdato = LocalDate.now().minusDays(50)
    private val sluttdato = LocalDate.now().plusDays(50)
    private val opphørsdato = LocalDate.now()

    private fun leggInnData(medOpphør: Boolean = false) {
        val opphørsdato = if (medOpphør) opphørsdato else null
        jdbcTemplate.update("INSERT INTO T_LOPENR_FNR (PERSON_LOPENR, PERSONNR) VALUES (1, ?)", personIdent)
        jdbcTemplate.update(
            """INSERT INTO T_VEDTAK (VEDTAK_ID, PERSON_LOPENR, STONAD_ID, KODE_RUTINE, DATO_INNV_FOM,
                         DATO_INNV_TOM) VALUES (1,1,1,'EO',?,?)""",
            startdato,
            sluttdato,
        )
        jdbcTemplate.update(
            """INSERT INTO T_STONAD (STONAD_ID, OPPDRAG_ID, PERSON_LOPENR, KODE_RUTINE, DATO_START, DATO_OPPHOR)
                                         VALUES (1, 1, 1, 'EO', CURRENT_DATE, ?)""",
            opphørsdato,
        )
    }

    @AfterEach
    fun tearDown() {
        listOf("T_LOPENR_FNR", "T_VEDTAK", "T_STONAD").forEach {
            jdbcTemplate.update("TRUNCATE TABLE $it")
        }
    }

    @Test
    fun `harStønad - overgangsstønad`() {
        leggInnData(false)
        val harStønad = infotrygdRepository.harStønad(setOf(personIdent), false)
        assertThat(harStønad).isEqualTo(listOf(Pair(personIdent, StønadType.OVERGANGSSTØNAD)))
    }

    @Test
    fun `harStønad - ingen treff`() {
        leggInnData(false)
        val harStønad = infotrygdRepository.harStønad(setOf(personIdent2), false)
        assertThat(harStønad).isEmpty()
    }

    // Dagens dato er (sluttdato - 5)
    @Test
    fun `harStønad - kun aktive - finner aktiv stønad når dagens dato er før sluttdato`() {
        leggInnData(false)
        val dagensDato = sluttdato.minusDays(5)
        val harStønad = infotrygdRepository.harStønad(setOf(personIdent), true, dagensDato)
        assertThat(harStønad).isEqualTo(listOf(Pair(personIdent, StønadType.OVERGANGSSTØNAD)))
    }

    // Dagens dato er (sluttdato + 5)
    @Test
    fun `harStønad - kun aktive - finner ikke aktiv stønad når dagens dato er etter sluttdato`() {
        leggInnData(false)
        val dagensDato = sluttdato.plusDays(5)
        val harStønad = infotrygdRepository.harStønad(setOf(personIdent), true, dagensDato)
        assertThat(harStønad).isEmpty()
    }

    // Dagens dato er (opphør - 5)
    @Test
    fun `harStønad - kun aktive, har opphør - finner aktiv stønad når dagens dato er før opphørsdato`() {
        leggInnData(true)
        val dagensDato = opphørsdato.minusDays(5)
        val harStønad = infotrygdRepository.harStønad(setOf(personIdent), true, dagensDato)
        assertThat(harStønad).isEqualTo(listOf(Pair(personIdent, StønadType.OVERGANGSSTØNAD)))
    }

    // Dagens dato er (opphør + 5)
    @Test
    fun `harStønad - kun aktive, har opphør - finner ingen aktiv stønad når dagens dato er etter opphørsdato`() {
        leggInnData(true)
        val dagensDato = opphørsdato.plusDays(5)
        val harStønad =
            infotrygdRepository.harStønad(
                setOf(personIdent),
                true,
                dagensDato,
            )
        assertThat(harStønad).isEmpty()
    }
}

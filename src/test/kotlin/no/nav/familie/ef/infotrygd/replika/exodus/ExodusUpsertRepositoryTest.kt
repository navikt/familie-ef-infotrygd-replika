package no.nav.familie.ef.infotrygd.replika.exodus

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrasjonstest")
class ExodusUpsertRepositoryTest {
    @Autowired
    private lateinit var upsertRepository: ExodusUpsertRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        listOf("t_lopenr_fnr", "t_ef").forEach {
            jdbcTemplate.update("TRUNCATE TABLE $it")
        }
    }

    @Test
    fun `upsert setter inn nye rader basert på enkel primærnøkkel`() {
        upsertRepository.upsert(
            ExodusTabell.T_LOPENR_FNR,
            listOf(mapOf("person_lopenr" to "1", "personnr" to "01234567890")),
        )

        val rad = jdbcTemplate.queryForMap("SELECT * FROM t_lopenr_fnr WHERE person_lopenr = 1")
        assertThat(rad["personnr"]).isEqualTo("01234567890")
    }

    @Test
    fun `upsert oppdaterer eksisterende rad ved konflikt på primærnøkkel`() {
        upsertRepository.upsert(
            ExodusTabell.T_LOPENR_FNR,
            listOf(mapOf("person_lopenr" to "1", "personnr" to "01234567890")),
        )
        upsertRepository.upsert(
            ExodusTabell.T_LOPENR_FNR,
            listOf(mapOf("person_lopenr" to "1", "personnr" to "09876543210")),
        )

        val antallRader = jdbcTemplate.queryForObject("SELECT count(*) FROM t_lopenr_fnr", Int::class.java)
        val rad = jdbcTemplate.queryForMap("SELECT * FROM t_lopenr_fnr WHERE person_lopenr = 1")
        assertThat(antallRader).isEqualTo(1)
        assertThat(rad["personnr"]).isEqualTo("09876543210")
    }

    @Test
    fun `upsert håndterer sammensatt primærnøkkel`() {
        upsertRepository.upsert(
            ExodusTabell.T_EF,
            listOf(
                mapOf(
                    "vedtak_id" to "1",
                    "tidspunkt_reg" to "2024-01-01T00:00:00",
                    "samordning" to "J",
                ),
            ),
        )

        val antallRader = jdbcTemplate.queryForObject("SELECT count(*) FROM t_ef", Int::class.java)
        assertThat(antallRader).isEqualTo(1)
    }

    @Test
    fun `upsert med tom liste gjør ingenting`() {
        upsertRepository.upsert(ExodusTabell.T_LOPENR_FNR, emptyList())

        val antallRader = jdbcTemplate.queryForObject("SELECT count(*) FROM t_lopenr_fnr", Int::class.java)
        assertThat(antallRader).isEqualTo(0)
    }

    @Test
    fun `truncate tømmer tabellen`() {
        upsertRepository.upsert(
            ExodusTabell.T_LOPENR_FNR,
            listOf(mapOf("person_lopenr" to "1", "personnr" to "01234567890")),
        )

        upsertRepository.truncate(ExodusTabell.T_LOPENR_FNR)

        val antallRader = jdbcTemplate.queryForObject("SELECT count(*) FROM t_lopenr_fnr", Int::class.java)
        assertThat(antallRader).isEqualTo(0)
    }
}

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
class ExodusStatusRepositoryTest {
    @Autowired
    private lateinit var statusRepository: ExodusStatusRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("TRUNCATE TABLE exodus_status")
    }

    @Test
    fun `finn returnerer null når tabellen aldri har blitt replikert`() {
        assertThat(statusRepository.finn(ExodusTabell.T_VEDTAK)).isNull()
    }

    @Test
    fun `oppdaterIterator setter inn ny status og teller opp antall rader`() {
        statusRepository.oppdaterIterator(ExodusTabell.T_VEDTAK, "iterator-1", 100)

        val status = statusRepository.finn(ExodusTabell.T_VEDTAK)
        assertThat(status).isNotNull
        assertThat(status!!.iterator).isEqualTo("iterator-1")
        assertThat(status.jobStatus).isEqualTo(JobStatus.OK)
        assertThat(status.antallRaderHentet).isEqualTo(100)
    }

    @Test
    fun `oppdaterIterator akkumulerer antall rader over flere kall`() {
        statusRepository.oppdaterIterator(ExodusTabell.T_VEDTAK, "iterator-1", 100)
        statusRepository.oppdaterIterator(ExodusTabell.T_VEDTAK, "iterator-2", 50)

        val status = statusRepository.finn(ExodusTabell.T_VEDTAK)
        assertThat(status!!.iterator).isEqualTo("iterator-2")
        assertThat(status.antallRaderHentet).isEqualTo(150)
    }

    @Test
    fun `settNyBaseline nullstiller iterator og setter status NY_BASELINE`() {
        statusRepository.oppdaterIterator(ExodusTabell.T_VEDTAK, "iterator-1", 100)

        statusRepository.settNyBaseline(ExodusTabell.T_VEDTAK)

        val status = statusRepository.finn(ExodusTabell.T_VEDTAK)
        assertThat(status!!.iterator).isNull()
        assertThat(status.jobStatus).isEqualTo(JobStatus.NY_BASELINE)
        assertThat(status.antallRaderHentet).isEqualTo(0)
    }

    @Test
    fun `en påfølgende vellykket oppdatering setter status tilbake til OK`() {
        statusRepository.settNyBaseline(ExodusTabell.T_VEDTAK)

        statusRepository.oppdaterIterator(ExodusTabell.T_VEDTAK, "iterator-1", 10)

        val status = statusRepository.finn(ExodusTabell.T_VEDTAK)
        assertThat(status!!.jobStatus).isEqualTo(JobStatus.OK)
    }
}

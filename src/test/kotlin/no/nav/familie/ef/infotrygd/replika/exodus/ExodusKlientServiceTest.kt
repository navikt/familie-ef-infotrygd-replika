package no.nav.familie.ef.infotrygd.replika.exodus

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExodusKlientServiceTest {
    private val exodusClient = mockk<ExodusClient>()
    private val statusRepository = mockk<ExodusStatusRepository>()
    private val skrivService = mockk<ExodusSkrivService>(relaxed = true)
    private val exodusProperties = ExodusProperties(baseUrl = "http://localhost", batchStorrelse = 2)

    private val exodusKlientService =
        ExodusKlientService(exodusClient, statusRepository, skrivService, exodusProperties)

    private val tomSchema = SchemaDto(kolonner = emptyList())
    private val vedtakIdSchema = SchemaDto(kolonner = listOf(KolonnebeskrivelseDto("vedtak_id")))

    @Test
    fun `bruker null iterator ved første kall for en tabell`() {
        every { statusRepository.finn(ExodusTabell.T_VEDTAK) } returns null
        every { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, null, 2) } returns
            HentUttrekkResponse(iterator = "iterator-0", schema = tomSchema, innhold = emptyList())

        exodusKlientService.replikerNesteSide(ExodusTabell.T_VEDTAK)

        verify { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, null, 2) }
    }

    @Test
    fun `gjenbruker lagret iterator ved senere kall`() {
        every { statusRepository.finn(ExodusTabell.T_VEDTAK) } returns
            ExodusStatus("t_vedtak", "iterator-1", JobStatus.OK, 10, java.time.LocalDateTime.now())
        every { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, "iterator-1", 2) } returns
            HentUttrekkResponse(iterator = "iterator-1", schema = tomSchema, innhold = emptyList())

        exodusKlientService.replikerNesteSide(ExodusTabell.T_VEDTAK)

        verify { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, "iterator-1", 2) }
    }

    @Test
    fun `lagrer siden og returnerer true når responsen er full (kan finnes flere sider)`() {
        every { statusRepository.finn(ExodusTabell.T_VEDTAK) } returns null
        val respons =
            HentUttrekkResponse(
                iterator = "iterator-2",
                schema = vedtakIdSchema,
                innhold = listOf(listOf("1"), listOf("2")),
            )
        every { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, null, 2) } returns respons

        val flereSider = exodusKlientService.replikerNesteSide(ExodusTabell.T_VEDTAK)

        assertThat(flereSider).isTrue
        verify {
            skrivService.lagreSide(
                ExodusTabell.T_VEDTAK,
                listOf(mapOf("vedtak_id" to "1"), mapOf("vedtak_id" to "2")),
                "iterator-2",
            )
        }
    }

    @Test
    fun `returnerer false når responsen har færre rader enn forespurt`() {
        every { statusRepository.finn(ExodusTabell.T_VEDTAK) } returns null
        val respons = HentUttrekkResponse(iterator = "iterator-1", schema = vedtakIdSchema, innhold = listOf(listOf("1")))
        every { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, null, 2) } returns respons

        val flereSider = exodusKlientService.replikerNesteSide(ExodusTabell.T_VEDTAK)

        assertThat(flereSider).isFalse
    }

    @Test
    fun `returnerer false ved tom liste`() {
        every { statusRepository.finn(ExodusTabell.T_VEDTAK) } returns null
        every { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, null, 2) } returns
            HentUttrekkResponse(iterator = "iterator-0", schema = tomSchema, innhold = emptyList())

        val flereSider = exodusKlientService.replikerNesteSide(ExodusTabell.T_VEDTAK)

        assertThat(flereSider).isFalse
    }

    @Test
    fun `NY_BASELINE fører til nullstilling og at scheduler bør prøve på nytt`() {
        every { statusRepository.finn(ExodusTabell.T_VEDTAK) } returns
            ExodusStatus("t_vedtak", "iterator-1", JobStatus.OK, 10, java.time.LocalDateTime.now())
        every { exodusClient.hentUttrekk(ExodusTabell.T_VEDTAK, "iterator-1", 2) } throws
            NyBaselineException(ExodusTabell.T_VEDTAK)

        val flereSider = exodusKlientService.replikerNesteSide(ExodusTabell.T_VEDTAK)

        assertThat(flereSider).isTrue
        verify { skrivService.nullstillTilNyBaseline(ExodusTabell.T_VEDTAK) }
    }
}

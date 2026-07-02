package no.nav.familie.ef.infotrygd.replika.exodus

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ExodusKlientSchedulerTest {
    private val exodusKlientService = mockk<ExodusKlientService>()
    private val lederVelger = mockk<LederVelger>()
    private val exodusProperties = ExodusProperties(baseUrl = "http://localhost", maksSiderPerKjoring = 3)

    private val scheduler = ExodusKlientScheduler(exodusKlientService, lederVelger, exodusProperties)

    @Test
    fun `gjør ingenting når poden ikke er leder`() {
        every { lederVelger.erLeder() } returns false

        scheduler.replikerAlleTabeller()

        verify(exactly = 0) { exodusKlientService.replikerNesteSide(any()) }
    }

    @Test
    fun `replikerer alle tabeller når poden er leder, til det ikke finnes flere sider`() {
        every { lederVelger.erLeder() } returns true
        ExodusTabell.entries.forEach { every { exodusKlientService.replikerNesteSide(it) } returns false }

        scheduler.replikerAlleTabeller()

        ExodusTabell.entries.forEach { verify(exactly = 1) { exodusKlientService.replikerNesteSide(it) } }
    }

    @Test
    fun `stopper etter maks antall sider selv om det fortsatt er flere sider`() {
        every { lederVelger.erLeder() } returns true
        every { exodusKlientService.replikerNesteSide(any()) } returns true

        scheduler.replikerAlleTabeller()

        verify(exactly = ExodusTabell.entries.size * 3) { exodusKlientService.replikerNesteSide(any()) }
    }

    @Test
    fun `feil i én tabell hindrer ikke replikering av de andre`() {
        every { lederVelger.erLeder() } returns true
        every { exodusKlientService.replikerNesteSide(ExodusTabell.T_LOPENR_FNR) } throws RuntimeException("boom")
        ExodusTabell.entries
            .filter { it != ExodusTabell.T_LOPENR_FNR }
            .forEach { every { exodusKlientService.replikerNesteSide(it) } returns false }

        scheduler.replikerAlleTabeller()

        ExodusTabell.entries
            .filter { it != ExodusTabell.T_LOPENR_FNR }
            .forEach { verify(exactly = 1) { exodusKlientService.replikerNesteSide(it) } }
    }
}

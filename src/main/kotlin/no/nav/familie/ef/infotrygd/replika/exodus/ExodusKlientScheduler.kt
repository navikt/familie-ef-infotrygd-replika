package no.nav.familie.ef.infotrygd.replika.exodus

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Kjører replikering av alle tabeller fra historisk-exodus periodisk. Med leaderElection: true i
 * nais-spec-en og flere pods er det kun poden som er leder som faktisk gjør noe - de andre er
 * en no-op her.
 */
@Component
class ExodusKlientScheduler(
    private val exodusKlientService: ExodusKlientService,
    private val lederVelger: LederVelger,
    private val exodusProperties: ExodusProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${exodus.scheduler-cron}")
    fun replikerAlleTabeller() {
        if (!exodusProperties.schedulerEnabled) {
            logger.debug("Scheduler for replikering er avskrudd (exodus.scheduler-enabled=false), gjør ingenting")
            return
        }
        if (!lederVelger.erLeder()) {
            return
        }
        logger.info("Starter scheduler-kjøring, replikerer ${ExodusTabell.entries.size} tabeller fra exodus")
        ExodusTabell.entries.forEach(::replikerTabellFullstendig)
        logger.info("Scheduler-kjøring ferdig")
    }

    /** Henter sider for én tabell helt til den er ajour, eller til sidetaket for én kjøring er nådd. */
    private fun replikerTabellFullstendig(tabell: ExodusTabell) {
        try {
            var flereSider = true
            var antallSider = 0
            while (flereSider && antallSider < exodusProperties.maksSiderPerKjoring) {
                flereSider = exodusKlientService.replikerNesteSide(tabell)
                antallSider++
            }
            if (flereSider) {
                logger.info(
                    "Tabell ${tabell.tabellNavn}: hentet $antallSider sider, men maksSiderPerKjoring " +
                        "(maksSiderPerKjoring=${exodusProperties.maksSiderPerKjoring}) ble nådd - fortsetter neste kjøring",
                )
            } else {
                logger.info("Tabell ${tabell.tabellNavn}: ajour etter $antallSider hentede sider")
            }
        } catch (e: Exception) {
            // Ingenting committes fra en feilet side - scheduleren prøver denne tabellen på nytt
            // ved neste kjøring, uten at det påvirker replikering av de andre tabellene.
            logger.error("Replikering av tabell ${tabell.tabellNavn} feilet, prøver igjen ved neste kjøring", e)
        }
    }
}

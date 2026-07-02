package no.nav.familie.ef.infotrygd.replika.exodus

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Orkestrerer replikering av én side av gangen for en tabell:
 * 1. Les iterator/job_status fra exodus_status (null første gang).
 * 2. POST /api/hentUttrekk med tabellnavn + iterator + batchstørrelse.
 * 3. Upsert radene + oppdater iterator i samme Postgres-transaksjon (ExodusSkrivService).
 * 4. Returner om det kan finnes flere sider (dvs. at forrige side var "full").
 *
 * Ved 409/NY_BASELINE trunkeres tabellen og iterator nullstilles, slik at neste forsøk starter
 * helt på nytt. Andre feil forplanter seg ut uten at noe committes, slik at scheduleren prøver
 * igjen ved neste kjøring.
 */
@Service
class ExodusKlientService(
    private val exodusClient: ExodusClient,
    private val statusRepository: ExodusStatusRepository,
    private val skrivService: ExodusSkrivService,
    private val exodusProperties: ExodusProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun replikerNesteSide(tabell: ExodusTabell): Boolean {
        val iterator = statusRepository.finn(tabell)?.iterator

        val respons =
            try {
                exodusClient.hentUttrekk(tabell, iterator, exodusProperties.batchStorrelse)
            } catch (e: NyBaselineException) {
                logger.warn(
                    "Mottok NY_BASELINE fra exodus for tabell ${tabell.tabellNavn}, " +
                        "trunkerer og nullstiller iterator - starter på nytt neste kjøring",
                    e,
                )
                skrivService.nullstillTilNyBaseline(tabell)
                return true
            }

        val rader = respons.tilRader()
        skrivService.lagreSide(tabell, rader, respons.iterator)

        return rader.size >= exodusProperties.batchStorrelse
    }
}

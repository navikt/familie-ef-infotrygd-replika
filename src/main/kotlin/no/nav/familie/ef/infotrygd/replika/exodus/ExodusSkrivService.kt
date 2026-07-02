package no.nav.familie.ef.infotrygd.replika.exodus

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Egen bean for de transaksjonelle skriveoperasjonene, slik at @Transactional faktisk fanges opp
 * av Spring sin proxy (kalles fra ExodusKlientService, som selv ikke er transaksjonell siden den
 * gjør et HTTP-kall mot exodus før skrivingen).
 */
@Service
class ExodusSkrivService(
    private val upsertRepository: ExodusUpsertRepository,
    private val statusRepository: ExodusStatusRepository,
) {
    /** Upsert av rader og oppdatering av iterator skjer i samme transaksjon. */
    @Transactional
    fun lagreSide(
        tabell: ExodusTabell,
        rader: List<Map<String, String?>>,
        nyIterator: String,
    ) {
        upsertRepository.upsert(tabell, rader)
        statusRepository.oppdaterIterator(tabell, nyIterator, rader.size)
    }

    /** Trunkering av tabellen og nullstilling av iterator skjer i samme transaksjon. */
    @Transactional
    fun nullstillTilNyBaseline(tabell: ExodusTabell) {
        upsertRepository.truncate(tabell)
        statusRepository.settNyBaseline(tabell)
    }
}

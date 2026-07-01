package no.nav.familie.ef.infotrygd.replika.service

import no.nav.familie.ef.infotrygd.replika.model.StønadType
import no.nav.familie.ef.infotrygd.replika.model.StønadType.BARNETILSYN
import no.nav.familie.ef.infotrygd.replika.repository.PeriodeRepository
import no.nav.familie.ef.infotrygd.replika.rest.api.Periode
import no.nav.familie.ef.infotrygd.replika.rest.api.PeriodeRequest
import no.nav.familie.ef.infotrygd.replika.utils.InfotrygdPeriodeUtil
import org.springframework.stereotype.Service

@Service
class PeriodeService(
    private val periodeRepository: PeriodeRepository,
) {
    fun hentPerioder(request: PeriodeRequest): Map<StønadType, List<Periode>> {
        val perioder =
            periodeRepository
                .hentPerioder(request)
                .filter { harOppdragIdEller0beløp(it.second) }
                .groupBy({ it.first }) { it.second }
                .toMutableMap()
        perioder[BARNETILSYN] = hentBarnetilsynPerioderMedBarn(perioder)
        return perioder.map { it.key to it.value.sortedByDescending { it.stønadFom } }.toMap()
    }

    /**
     * Skal filtrere vekk perioder som mangler oppdragId, og har beløp over 0kr
     * Først var det kommunisert at vi skulle filtrere vekk alle perioder som mangler oppdrag_id då disse ikke var iverksatte
     * Vi fant senere ut at de som manglet oppdragId og hadde beløp 0 var besluttet, men ikke sendt til oppdrag
     */
    private fun harOppdragIdEller0beløp(periode: Periode) =
        periode.oppdragId != null || (periode.engangsbeløp == 0 && periode.månedsbeløp == 0)

    fun hentSammenslåttePerioder(request: PeriodeRequest): Map<StønadType, List<Periode>> =
        hentPerioder(request)
            .map {
                it.key to slåSammenPerioder(it.value)
            }.toMap()

    private fun hentBarnetilsynPerioderMedBarn(perioderPerStønadstype: Map<StønadType, List<Periode>>): List<Periode> {
        val barnetilsynPerioder = perioderPerStønadstype.getOrDefault(BARNETILSYN, emptyList())
        val barnetilsynPeriodeBarnListe = periodeRepository.hentBarnForPerioder(barnetilsynPerioder)

        return barnetilsynPerioder
            .groupBy { it.stønadId }
            .values
            .flatMap { perioder ->
                perioder
                    .sortedBy { it.vedtakId }
                    .fold(emptyList()) { acc, periode ->
                        val barnIdenter = hentBarnIdenter(acc, periode, barnetilsynPeriodeBarnListe)
                        acc + periode.copy(barnIdenter = barnIdenter)
                    }
            }
    }

    private fun hentBarnIdenter(
        acc: List<Periode>,
        periode: Periode,
        barnetilsynPeriodeBarnListe: Map<Long, List<String>>,
    ): List<String> {
        val barnIdenter = barnetilsynPeriodeBarnListe[periode.vedtakId] ?: emptyList()
        if (barnIdenter.isNotEmpty()) return barnIdenter

        return brukBarnIdenterFraForrigeVedtak(acc, periode, barnIdenter)
    }

    /**
     * 49 vedtak finnes med kode_resultat = 'FI', som betyder at stønaden fortsatt er innvilget,
     * men der barnen har blitt fjernet. I de tilfellene bruker vi barnen fra forrige vedtak
     */
    private fun brukBarnIdenterFraForrigeVedtak(
        acc: List<Periode>,
        periode: Periode,
        barnIdenter: List<String>,
    ): List<String> {
        val last = acc.lastOrNull()
        return if (periode.erFortsattInnvilget() &&
            barnIdenter.isEmpty() &&
            last != null &&
            last.barnIdenter.isNotEmpty()
        ) {
            last.barnIdenter
        } else {
            emptyList()
        }
    }

    private fun slåSammenPerioder(perioder: List<Periode>): List<Periode> = InfotrygdPeriodeUtil.slåSammenInfotrygdperioder(perioder)
}

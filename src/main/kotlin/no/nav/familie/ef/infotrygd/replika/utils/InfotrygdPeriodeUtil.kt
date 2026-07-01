package no.nav.familie.ef.infotrygd.replika.utils

import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdEndringKode
import no.nav.familie.ef.infotrygd.replika.rest.api.Periode
import java.time.LocalDate

/**
 * Før vi slår sammen perioder fra infotrygd så må vi sette TOM-dato til opphøret sitt dato hvis opphørsdatoet er før TOM-dato
 * Vi filtrerer også ut ugyldige perioder, der TOM er før FOM
 *
 * Før vi slår sammen, sorterer vi de i en rekkefølge som de ble lagt inn i infotrygd,
 * sånn att en senere periode overskrever en tidligere periode
 */
object InfotrygdPeriodeUtil {
    fun filtrerOgSorterPerioderFraInfotrygd(perioderFraInfotrygd: List<Periode>): List<Periode> =
        perioderFraInfotrygd
            .toSet()
            .map { brukOpphørsdatoSomTomHvisDenFinnes(it) }
            .filter { it.stønadTom > it.stønadFom } // Skal infotrygd rydde bort disse? (inkl de der opphørdato er før startdato)
            .sortedWith(compareBy<Periode>({ it.stønadId }, { it.vedtakId }, { it.stønadFom }).reversed())

    private fun brukOpphørsdatoSomTomHvisDenFinnes(it: Periode): Periode {
        val opphørsdato = it.opphørsdato
        return if (opphørsdato != null && opphørsdato.isBefore(it.stønadTom)) {
            it.copy(stønadTom = trekkFraEnDagHvisFørsteIMåneden(opphørsdato))
        } else {
            it
        }
    }

    /**
     * Noen opphørsdatoer kommer som eks 01.02.yyyy, då skal vi bruke 31.01.yyyy
     */
    private fun trekkFraEnDagHvisFørsteIMåneden(opphørsdato: LocalDate) =
        if (opphørsdato.dayOfMonth == 1) opphørsdato.minusDays(1) else opphørsdato

    /**
     * Slår sammen perioder fra infotrygd, disse skal ikke slås sammen tvers ulike stønadId'er
     */
    fun slåSammenInfotrygdperioder(infotrygdperioder: List<Periode>): List<Periode> =
        filtrerOgSorterPerioderFraInfotrygd(infotrygdperioder)
            .filter { it.kode != InfotrygdEndringKode.ANNULERT && it.kode != InfotrygdEndringKode.UAKTUELL }
            .groupBy { it.stønadId }
            .values
            .flatMap(this::fjernPerioderSomErOverskrevet)
            .sortedByDescending { it.stønadFom }

    /* NB! forventer sorterte infotrygdperioder - nyest først.
     * NB 2 - dette er ikke en slå sammen, men en kutt periode som ikke gjelder (vi bruker den senest vedtatte versjonen av en periode),
     */
    private fun fjernPerioderSomErOverskrevet(perioder: List<Periode>): MutableList<Periode> {
        val list = mutableListOf<Periode>()

        for (periode in perioder) {
            val minStønadFom = list.minByOrNull { it.stønadFom }
            if (minStønadFom != null && periode.stønadFom.isEqualOrAfter(minStønadFom.stønadFom)) {
                // Fordi vi sorterer vil den "eldste" ikke bety noe - vi tar den ikke med videre
                continue
            } else if (minStønadFom != null && minStønadFom.erPeriodeOverlappende(periode)) {
                // Vi kutter den tidligere perioden - den nye perioden overtar herfra
                list.add(periode.copy(stønadTom = minStønadFom.stønadFom.minusDays(1)))
            } else {
                list.add(periode)
            }
        }
        return list
    }

    private fun Periode.erDatoInnenforPeriode(dato: LocalDate): Boolean = dato.isEqualOrBefore(stønadTom) && dato.isEqualOrAfter(stønadFom)

    fun Periode.erPeriodeOverlappende(periode: Periode): Boolean =
        (erDatoInnenforPeriode(periode.stønadFom) || erDatoInnenforPeriode(periode.stønadTom)) ||
            omslutter(periode)

    private fun Periode.omslutter(periode: Periode) = periode.stønadFom.isBefore(stønadFom) && periode.stønadTom.isAfter(stønadTom)
}

fun LocalDate.isEqualOrBefore(other: LocalDate) = this == other || this.isBefore(other)

fun LocalDate.isEqualOrAfter(other: LocalDate) = this == other || this.isAfter(other)

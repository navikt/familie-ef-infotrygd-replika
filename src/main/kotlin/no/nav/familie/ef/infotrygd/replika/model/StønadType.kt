package no.nav.familie.ef.infotrygd.replika.model

/**
 * @param kodeRutine Verdi fra kode_rutine fra v_vedtak
 * @param saS10Valg Verdi fra s10_valg fra sa_sak_10
 */
enum class StønadType(
    val kodeRutine: String,
    val saS10Valg: String,
) {
    OVERGANGSSTØNAD("EO", "OG"),
    BARNETILSYN("EB", "BT"),
    SKOLEPENGER("EU", "UT"),
    ;

    companion object {
        private val kodeRutineMap = values().map { it.kodeRutine to it }.toMap()
        private val s10ValgMap = values().map { it.saS10Valg to it }.toMap()

        fun fraKodeRutine(kodeRutine: String): StønadType =
            kodeRutineMap[kodeRutine] ?: error("Fant ikke StønadType fra kodeRutine=$kodeRutine")

        fun fraS10Valg(s10Valg: String): StønadType = s10ValgMap[s10Valg] ?: error("Fant ikke StønadType fra s10Valg=$s10Valg")
    }
}

package no.nav.familie.ef.infotrygd.replika.rest.api

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ef.infotrygd.replika.model.StønadType
import java.time.LocalDate
import java.time.LocalDateTime

data class PeriodeRequest(
    val personIdenter: Set<FoedselsNr>,
    val stønadstyper: Set<StønadType>,
)

data class PeriodeResponse(
    val overgangsstønad: List<Periode>,
    val barnetilsyn: List<Periode>,
    val skolepenger: List<Periode>,
)

/**
 * @param stønadBeløp er det samme som engangsbeløp
 * @param beløp mappet fra netto_beløp, som er det samme som månedsbeløp
 * @param kodeOvergangsstønad er i prinsipp det vi kaller periodetype i ny løsning
 */
data class Periode(
    val personIdent: String,
    val sakstype: InfotrygdSakstype,
    val kode: InfotrygdEndringKode,
    val kodeOvergangsstønad: InfotrygdOvergangsstønadKode?,
    val aktivitetstype: InfotrygdAktivitetstype?,
    val brukerId: String,
    val stønadId: Long,
    val vedtakId: Long,
    val vedtakstidspunkt: LocalDateTime,
    val engangsbeløp: Int,
    val inntektsgrunnlag: Int,
    val inntektsreduksjon: Int,
    val samordningsfradrag: Int,
    val utgifterBarnetilsyn: Int,
    val månedsbeløp: Int,
    val startDato: LocalDate,
    val stønadFom: LocalDate,
    val stønadTom: LocalDate,
    val opphørsdato: LocalDate?,
    val barnIdenter: List<String> = emptyList(),
    val vedtakKodeResultat: String,
    val oppdragId: Int?,
) {
    fun erFortsattInnvilget() = vedtakKodeResultat == "FI"
}

/**
 * AN      	Annullert
 * Disse finnes tvers de ulike søknadene i Infotrygd, men det er kun noen få som finnes blant våre stønader
 * AS      	Avgang småbarnstillegg
 * B       	Barnetillegg
 * BB      	Barn 18 år
 * E       	Endring i beregningsgrunnlag
 * F       	Førstegangsvedtak
 * G       	G-regulering
 * H       	Barn etterlatte.3 måneder
 * I       	Institusjonsopphold
 * IN      	Ny inntektsgr.
 * KB      	Barn 3 år
 * NB      	Nytt barn
 * NY      	Ny
 * O       	Opphørt
 * S       	Satsendring
 * TS      	Tilgang småbarnstillegg
 * U       	Uføregrad
 * UA      	Uaktuell
 * P       	Passivt
 * AV      	Avslag
 * OO      	Overf ny løsning
 */
enum class InfotrygdEndringKode(
    val infotrygdKode: String,
    val beskrivelse: String,
) {
    ANNULERT("AN", "Annullert"),
    ENDRING_BEREGNINGSGRUNNLAG("E", "Endring i beregningsgrunnlag"),
    FØRSTEGANGSVEDTAK("F", "Førstegangsvedtak"),
    G_REGULERING("G", "G-regulering"),
    NY("NY", "Ny"),
    OPPHØRT("O", "Opphørt"),
    SATSENDRING("S", "Satsendring"),
    UAKTUELL("UA", "Uaktuell"),
    OVERTFØRT_NY_LØSNING("OO", "Overf ny løsning"),
    ;

    companion object {
        private val kodeMap = values().associateBy(InfotrygdEndringKode::infotrygdKode)

        fun fraInfotrygdKode(kode: String): InfotrygdEndringKode = kodeMap[kode] ?: error("Fant ikke endringKode for $kode")
    }
}

enum class InfotrygdSakstype(
    val infotrygdKode: String,
    val beskrivelse: String,
) {
    KLAGE("K", "Klage"),
    MASKINELL_G_OMREGNING("MG", "Maskinell G-omregning"),
    REVURDERING("R", "Revurdering"),
    GRUNNBELØP_OMREGNING("GO", "Grunnbeløp omregning"),
    KONVERTERING("KO", "Konvertering"),
    MASKINELL_SATSOMREGNING("MS", "Maskinell satsomregning"),
    ANKE("A", "Anke"),
    SØKNAD("S", "Søknad"),
    SØKNAD_ØKNING_ENDRING("SØ", "Søknad om økning/endring"),
    ;

    companion object {
        private val kodeMap = values().associateBy(InfotrygdSakstype::infotrygdKode)

        fun fraInfotrygdKode(kode: String): InfotrygdSakstype = kodeMap[kode] ?: error("Fant ikke vedtakKode for $kode")
    }
}

enum class InfotrygdOvergangsstønadKode(
    val infotrygdKode: String,
    val beskrivelse: String,
) {
    BARN_UNDER_1_3_ÅR("1", "Barn under 1 år / 3 år (gamle tilfeller)"),
    YRKESRETTET_AKTIVITET_BARN_FYLT_1_3_ÅR(
        "2",
        "Er i yrkesrettet aktivitet - barn har fylt 1 år / 3 år (gamle tilfeller)",
    ),
    UNNTAK_FRA_KRAV_TIL_YRKESRETTET_AKTIVITET(
        "3",
        "Unntak fra krav til yrkesr. aktivitet når barn har fylt 1 år / år (gamle tilfeller)",
    ),
    UTVIDELSE_NØDVENDIG_UTDANNING("4", "Utvidelse på grunn av nødvendig utdanning jf 15-6. 3. ledd"),
    PÅVENTE_SKOLESTART_ARBEID_TILSYNSPLASS("5", "I påvente av skolestart/arbeid/tilsynsplass 15-6. 4. ledd"),
    YRKESRETTET_AKTIVITET("6", "Er i yrkesrettet aktivitet - i omstillingstid"),
    FORBIGÅENDE_SYKDOM("7", "Forbig. sykdom hos forsørger eller barnet 15-6. 6. ledd"),
    SÆRLIG_TILSSYNSKREVENDE_BARN("8", "Har særlig tilssynskrevende barn"),
    ETABLERER_EGEN_VIRKSOMHET("9", "Etablerer egen virksomhet"),
    FORTSATT_INNVILGET_TROSS_VARSEL_OM_OPPHØR_PGA_SAMBOER("10", "Fortsatt innvilget tro"),
    ;

    companion object {
        private val kodeMap = values().associateBy(InfotrygdOvergangsstønadKode::infotrygdKode)

        fun fraInfotrygdKode(kode: String): InfotrygdOvergangsstønadKode =
            kodeMap[kode] ?: error("Fant ikke overgangsstønadskode for $kode")
    }
}

enum class InfotrygdAktivitetstype(
    val infotrygdKode: String,
    val beskrivelse: String,
) {
    I_ARBEID("A", "I arbeid"),
    I_UTDANNING("U", "I utdanning"),
    TILMELDT_SOM_REELL_ARBEIDSSØKER("S", "Tilmeldt som reell arbeidssøker"),
    KURS("K", "Kurs o.l."),
    BRUKERKONTAKT("B", "Brukerkontakt"),
    IKKE_I_AKTIVITET("N", "Ikke i aktivitet"),
    ;

    companion object {
        private val kodeMap = values().associateBy(InfotrygdAktivitetstype::infotrygdKode)

        fun fraInfotrygdKode(kode: String): InfotrygdAktivitetstype = kodeMap[kode] ?: error("Fant ikke aktivitetstype for $kode")
    }
}

package no.nav.familie.ef.infotrygd.replika.exodus

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

/**
 * HTTP-kontrakt mot historisk-exodus, verifisert mot kildekoden i navikt/historisk-exodus
 * (UttrekkController/Uttrekk.kt). Radene i svaret fra /api/hentUttrekk kommer posisjonelt
 * (innhold: List<List<String?>>) sammen med et skjema som beskriver kolonnenavn og -rekkefølge,
 * IKKE som Map<String,Any?> - se [HentUttrekkResponse.tilRader].
 *
 * Alle verdier serialiseres som String av exodus (BigDecimal/LocalDate/LocalDateTime.toString()),
 * så innsetting i Postgres må caste eksplisitt til riktig kolonnetype - se ExodusUpsertRepository.
 */
data class HentUttrekkRequest(
    val tabellnavn: String,
    /** Null ved første kall for en tabell. */
    val iterator: String?,
    val antallRader: Long,
)

data class HentUttrekkResponse(
    /** Cursor som skal brukes i neste kall. Alltid satt - uendret dersom det ikke finnes mer data. */
    val iterator: String = "",
    val schema: SchemaDto = SchemaDto(emptyList()),
    @JsonSetter(contentNulls = Nulls.SET)
    val innhold: List<List<String?>> = emptyList(),
)

data class SchemaDto(
    val kolonner: List<KolonnebeskrivelseDto>,
)

data class KolonnebeskrivelseDto(
    val navn: String,
)

/** Slår sammen schema.kolonner og innhold til rader nøkkelt på (lowercase) kolonnenavn. */
fun HentUttrekkResponse.tilRader(): List<Map<String, String?>> {
    val kolonnenavn = schema.kolonner.map { it.navn.lowercase() }
    return innhold.map { rad -> kolonnenavn.zip(rad).toMap() }
}

data class TellRaderRequest(
    val tabellnavn: String,
)

data class TellRaderResponse(
    val antall: Long,
)

/**
 * Kastes når exodus svarer 409 CONFLICT med feilkode NY_BASELINE - det betyr at tabellen er
 * re-eksportert fra Oracle, og at den lokale iteratoren ikke lenger er gyldig.
 */
class NyBaselineException(
    val tabell: ExodusTabell,
) : RuntimeException("Exodus returnerte NY_BASELINE for tabell ${tabell.tabellNavn}")

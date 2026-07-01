package no.nav.familie.ef.infotrygd.replika.utils

fun String.reverserFnr() = reverse(this)

/**
 * Dette er formatet på fnr i noen kolonner i sa_ tabellene
 */
private val regex = """(\d\d)(\d\d)(\d\d)(\d{5})""".toRegex()

private fun reverse(fnr: String): String {
    require(regex.matches(fnr)) { "Ikke et gyldig (reversert?) fødselsnummer: $fnr" }

    val (a, b, c, pnr) = regex.find(fnr)!!.destructured
    return "$c$b$a$pnr"
}

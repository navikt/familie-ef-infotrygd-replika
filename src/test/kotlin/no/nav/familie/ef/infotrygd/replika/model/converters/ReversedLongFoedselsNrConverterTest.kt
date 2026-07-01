package no.nav.familie.ef.infotrygd.replika.model.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReversedLongFoedselsNrConverterTest {
    private val converter = ReversedLongFoedselNrConverter()

    @Test
    fun convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo(0)
        assertThat(
            converter.convertToDatabaseColumn("01015450572"),
        ).isEqualTo(54010150572L) // TestData.foedselsNr(foedselsdato = LocalDate.of(1854, 1, 1))
    }

    @Test
    fun convertToEntityAttribute() {
        val full = 54010150572L
        val short = 10100382L

        println()

        assertThat(
            converter.convertToEntityAttribute(full),
        ).isEqualTo("01015450572") // TestData.foedselsNr(foedselsdato = LocalDate.of(1854, 1, 1))
        assertThat(converter.convertToEntityAttribute(short)).isEqualTo("01010000382") // TestData.foedselsNr(LocalDate.of(1900, 1, 1))
        assertThat(converter.convertToEntityAttribute(0)).isNull()
    }
}

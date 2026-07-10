package no.nav.familie.ef.infotrygd.replika.exodus

import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HentUttrekkResponseDeserialiseringTest {
    @Test
    fun `deserialiserer respons med null-verdier i innhold`() {
        val json =
            """
            {
                "iterator": "abc",
                "schema": {"kolonner": [{"navn": "A"}, {"navn": "B"}]},
                "innhold": [["1", null]]
            }
            """.trimIndent()

        val respons = jsonMapper.readValue(json, HentUttrekkResponse::class.java)

        assertThat(respons.innhold[0][0]).isEqualTo("1")
        assertThat(respons.innhold[0][1]).isNull()
    }

    @Test
    fun `deserialiserer respons der null-verdi er forste element eller hele raden er null`() {
        val json =
            """
            {
                "iterator": "abc",
                "schema": {"kolonner": [{"navn": "A"}, {"navn": "B"}, {"navn": "C"}]},
                "innhold": [[null, "2", "3"], [null, null, null]]
            }
            """.trimIndent()

        val respons = jsonMapper.readValue(json, HentUttrekkResponse::class.java)

        assertThat(respons.innhold[0]).containsExactly(null, "2", "3")
        assertThat(respons.innhold[1]).containsExactly(null, null, null)
    }
}

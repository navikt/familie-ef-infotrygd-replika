package no.nav.familie.ef.infotrygd.replika.rest.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InfotrygdSakUndervalgTest {
    @Test
    fun skalMappeVerdi() {
        assertThat(InfotrygdSakUndervalg.fraInfotrygdKode("AK")).isEqualTo(InfotrygdSakUndervalg.AK)
        assertThat(InfotrygdSakUndervalg.fraInfotrygdKode("  ")).isNull()
    }
}

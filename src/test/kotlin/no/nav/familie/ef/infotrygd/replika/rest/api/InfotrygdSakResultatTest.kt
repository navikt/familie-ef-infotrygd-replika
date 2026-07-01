package no.nav.familie.ef.infotrygd.replika.rest.api

import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSakResultat.Companion.fraInfotrygdKode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InfotrygdSakResultatTest {
    @Test
    fun skalMappeVerdierFraResultat() {
        assertThat(fraInfotrygdKode("  ")).isEqualTo(InfotrygdSakResultat.ÅPEN_SAK)
        assertThat(fraInfotrygdKode("AN ")).isEqualTo(InfotrygdSakResultat.ANNULLERING)
    }
}

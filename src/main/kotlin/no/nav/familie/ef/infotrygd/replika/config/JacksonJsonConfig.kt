package no.nav.familie.ef.infotrygd.replika.config

import no.nav.familie.kontrakter.felles.jsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

@Configuration
class JacksonJsonConfig {
    @Bean
    fun objectMapper(): JsonMapper = jsonMapper
}

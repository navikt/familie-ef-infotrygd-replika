package no.nav.familie.ef.infotrygd.replika

import no.nav.familie.ef.infotrygd.replika.exodus.ExodusProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ExodusProperties::class)
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
package no.nav.familie.ef.infotrygd.replika.rest.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ping", produces = [MediaType.TEXT_PLAIN_VALUE])
class PingController {
    @GetMapping
    fun ping() = "pong"
}

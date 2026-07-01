package no.nav.familie.ef.infotrygd.replika.rest.controller

import io.micrometer.core.annotation.Timed
import no.nav.familie.ef.infotrygd.replika.repository.SakRepository
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSakResponse
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSøkRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/saker")
@Timed(value = "infotrygd_sak_enslig_forsoerger_controller", percentiles = [0.5, 0.95])
class SakController(
    private val sakRepository: SakRepository,
) {
    @PostMapping(path = ["/finn"])
    fun finnSaker(
        @RequestBody request: InfotrygdSøkRequest,
    ): ResponseEntity<Any> {
        if (request.personIdenter.isEmpty()) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(InfotrygdSakResponse(sakRepository.finnSaker(request.personIdenter)))
    }
}

package no.nav.familie.ef.infotrygd.replika.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.ExampleObject
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSøkRequest
import no.nav.familie.ef.infotrygd.replika.service.StønadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stonad")
@Timed(value = "infotrygd_historikk_enslig_forsoerger_controller", percentiles = [0.5, 0.95])
class StønadController(
    private val stønadService: StønadService,
) {
    @Operation(summary = "Søker etter oppgitte fødselssnummere med stønadstype")
    @PostMapping(path = ["/eksisterer"])
    @Parameters(
        Parameter(
            examples = [
                ExampleObject(
                    name = "request",
                    value = "{\n  \"personIdenter\": [\n\"12345612345\"\n]}",
                ),
            ],
        ),
    )
    fun eksisterer(
        @RequestBody request: InfotrygdSøkRequest,
    ): ResponseEntity<Any> {
        if (request.personIdenter.isEmpty()) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(stønadService.finnesIInfotrygd(request))
    }
}

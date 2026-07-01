package no.nav.familie.ef.infotrygd.replika.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.ExampleObject
import no.nav.familie.ef.infotrygd.replika.model.StønadType
import no.nav.familie.ef.infotrygd.replika.repository.PeriodeRepository
import no.nav.familie.ef.infotrygd.replika.rest.api.Periode
import no.nav.familie.ef.infotrygd.replika.rest.api.PeriodeRequest
import no.nav.familie.ef.infotrygd.replika.rest.api.PeriodeResponse
import no.nav.familie.ef.infotrygd.replika.service.PeriodeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/perioder")
@Timed(value = "infotrygd_historikk_enslig_forsoerger_controller", percentiles = [0.5, 0.95])
class PeriodeController(
    private val periodeRepository: PeriodeRepository,
    private val periodeService: PeriodeService,
) {
    @Operation(summary = "Henter perioder")
    @PostMapping
    @Parameters(
        Parameter(
            examples = [
                ExampleObject(
                    name = "request",
                    value =
                        "{\n  \"identer\": [\n\"<fnr>\"\n],\n" +
                            " \"stønadstyper\": [\n\"OVERGANGSSTØNAD\"\n] \n}",
                ),
            ],
        ),
    )
    fun hentPerioder(
        @RequestBody request: PeriodeRequest,
    ): ResponseEntity<Any> {
        if (request.personIdenter.isEmpty()) {
            return ResponseEntity.badRequest().build()
        }
        val perioder = periodeService.hentPerioder(request)
        return ResponseEntity.ok(lagPeriodeResponse(perioder))
    }

    @Operation(summary = "Henter sammenslåtte perioder")
    @PostMapping("/sammenslatte")
    @Parameters(
        Parameter(
            examples = [
                ExampleObject(
                    name = "request",
                    value =
                        "{\n  \"identer\": [\n\"<fnr>\"\n],\n" +
                            " \"stønadstyper\": [\n\"OVERGANGSSTØNAD\"\n] \n}",
                ),
            ],
        ),
    )
    fun hentSammenslåttePerioder(
        @RequestBody request: PeriodeRequest,
    ): ResponseEntity<Any> {
        if (request.personIdenter.isEmpty()) {
            return ResponseEntity.badRequest().build()
        }
        val perioder = periodeService.hentSammenslåttePerioder(request)
        return ResponseEntity.ok(lagPeriodeResponse(perioder))
    }

    @GetMapping(path = ["/migreringspersoner"])
    fun hentMigreringspersoner(
        @RequestParam antall: Int,
    ): ResponseEntity<Any> {
        val personerForMigrering = periodeRepository.hentPersonerForMigrering(antall)
        return ResponseEntity.ok(personerForMigrering)
    }

    private fun lagPeriodeResponse(perioder: Map<StønadType, List<Periode>>): PeriodeResponse =
        PeriodeResponse(
            overgangsstønad = perioder.getOrDefault(StønadType.OVERGANGSSTØNAD, emptyList()),
            barnetilsyn = perioder.getOrDefault(StønadType.BARNETILSYN, emptyList()),
            skolepenger = perioder.getOrDefault(StønadType.SKOLEPENGER, emptyList()),
        )
}

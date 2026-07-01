package no.nav.familie.ef.infotrygd.replika.rest.controller

import no.nav.familie.ef.infotrygd.replika.integration.TableIntegrator
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Transactional
class TableController(
    private val tableIntegrator: TableIntegrator,
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping(path = ["/tables"])
    fun get(): Map<String, List<String>> = tableIntegrator.tables

    @GetMapping(path = ["/tables2"])
    fun get2(): Map<String, Any?> =
        tableIntegrator.tables.keys.associateWith {
            try {
                jdbcTemplate.queryForObject("select count(*) from $it", emptyMap<String, Any>(), Int::class.java)
            } catch (e: Exception) {
                logger.error("Check mot tabeller feiler", e)
                e.message
            }
        }
}

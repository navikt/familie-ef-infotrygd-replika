package no.nav.familie.ef.infotrygd.replika.exodus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body

/**
 * Leader election basert på NAIS' elector-sidecar (https://github.com/nais/elector), som starter
 * automatisk når `leaderElection: true` settes i nais-spec-en. NAIS setter da miljøvariabelen
 * ELECTOR_GET_URL, som svarer med navnet på poden som for øyeblikket er leder.
 *
 * Er variabelen ikke satt (f.eks. lokalt eller uten leaderElection aktivert), regnes denne
 * instansen alltid som leder.
 */
@Component
class LederVelger(
    @Value("\${ELECTOR_GET_URL:}") private val electorUrl: String,
    @Value("\${HOSTNAME:}") private val eigenPodNavn: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    fun erLeder(): Boolean {
        if (electorUrl.isBlank()) {
            return true
        }
        return try {
            val leder = restClient.get().uri(electorUrl).retrieve().body<ElectorRespons>()
            leder?.name == eigenPodNavn
        } catch (e: RestClientException) {
            logger.warn("Klarte ikke å avgjøre leder via elector, antar at denne poden ikke er leder", e)
            false
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ElectorRespons(
        val name: String,
    )
}

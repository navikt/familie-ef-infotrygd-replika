package no.nav.familie.ef.infotrygd.replika.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.web.client.RestClient

/**
 * Klient og autentisering for utgående maskin-til-maskin-kall mot historisk-exodus (on-prem),
 * som er den eneste eksterne tjenesten denne applikasjonen selv kaller ut til.
 *
 * Autentisering skjer med Azure AD client credentials (samme mønster som andre nais-til-nais
 * on-prem-kall), konfigurert som en vanlig Spring Security OAuth2-client-registrering
 * ("exodus") i application.yml - se spring.security.oauth2.client.registration.exodus.
 *
 * Vi bruker AuthorizedClientServiceOAuth2AuthorizedClientManager (ikke den servlet-request-baserte
 * varianten) fordi kallene skjer fra en @Scheduled-jobb uten noen innkommende HTTP-request å
 * henge autorisasjonen på.
 */
@Configuration
class ExodusClientConfig {
    @Bean
    fun exodusAuthorizedClientManager(clientRegistrationRepository: ClientRegistrationRepository): OAuth2AuthorizedClientManager {
        val authorizedClientService = InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()

        return AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        ).apply {
            setAuthorizedClientProvider(authorizedClientProvider)
        }
    }

    @Bean
    fun exodusRestClient(
        @Value("\${exodus.base-url}") baseUrl: String,
        exodusAuthorizedClientManager: OAuth2AuthorizedClientManager,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor { request, body, execution ->
                val authorizeRequest =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId("exodus")
                        .principal("familie-ef-infotrygd-replika")
                        .build()
                val authorizedClient =
                    exodusAuthorizedClientManager.authorize(authorizeRequest)
                        ?: throw OAuth2AuthorizationException(
                            OAuth2Error("missing_token"),
                            "Klarte ikke å hente access token for kall mot historisk-exodus",
                        )
                request.headers.setBearerAuth(authorizedClient.accessToken.tokenValue)
                execution.execute(request, body)
            }.build()
}

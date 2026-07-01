package no.nav.familie.ef.infotrygd.replika.security

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

/**
 * Denne applikasjonen kalles kun maskin-til-maskin (application-to-application), det finnes altså
 * ingen innlogget saksbehandler. Azure AD-appen som kaller oss må ha fått tildelt app-rollen
 * "access_as_application" for at kallet skal godkjennes, se .nais/dev.yaml (azure.application.claims).
 */
@Component
class AzureJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val ROLES_CLAIM = "roles"
        private const val AZP_CLAIM = "azp"
        const val ACCESS_AS_APPLICATION_AUTHORITY = "ROLE_ACCESS_AS_APPLICATION"
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val roller = jwt.getClaimAsStringList(ROLES_CLAIM) ?: emptyList()
        val clientId = jwt.getClaimAsString(AZP_CLAIM)

        val authorities =
            if (roller.contains("access_as_application")) {
                listOf(SimpleGrantedAuthority(ACCESS_AS_APPLICATION_AUTHORITY))
            } else {
                logger.warn("Klient $clientId mangler påkrevd rolle access_as_application. Roller i token: ${roller.joinToString(", ")}")
                emptyList()
            }

        return JwtAuthenticationToken(jwt, authorities)
    }
}

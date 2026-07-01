package no.nav.familie.ef.infotrygd.replika.config

import no.nav.familie.ef.infotrygd.replika.Profiles
import no.nav.familie.ef.infotrygd.replika.security.AzureJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Sikkerheten er basert på Spring Security sin oauth2ResourceServer (JWT), i motsetning til
 * det gamle no.nav.security:token-validation-spring-biblioteket som ble brukt i familie-ef-infotrygd.
 * Azure AD-issueren konfigureres via spring.security.oauth2.resourceserver.jwt.issuer-uri/audiences
 * i application.yml (se AZURE_OPENID_CONFIG_ISSUER/AZURE_APP_CLIENT_ID fra nais).
 *
 * Applikasjonen kalles kun maskin-til-maskin, se AzureJwtAuthenticationConverter for hvordan
 * app-rollen "access_as_application" håndheves.
 */
@Configuration
@EnableWebSecurity
@Profile("!" + Profiles.LOCAL_MOCK)
class SecurityConfig(
    private val jwtAuthenticationConverter: AzureJwtAuthenticationConverter,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/internal/**",
                        "/api/ping",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/tables",
                        "/tables2",
                    ).permitAll()
                    .anyRequest()
                    .hasAuthority(AzureJwtAuthenticationConverter.ACCESS_AS_APPLICATION_AUTHORITY)
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { it.jwtAuthenticationConverter(jwtAuthenticationConverter) }
            }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }

        return http.build()
    }
}

package no.nav.familie.ef.infotrygd.replika.rest.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdFinnesResponse
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSøkRequest
import no.nav.familie.ef.infotrygd.replika.service.StønadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

@WebMvcTest(StønadController::class, excludeAutoConfiguration = [OAuth2ResourceServerWebSecurityAutoConfiguration::class])
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integrasjonstest")
class StønadControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var stønadService: StønadService

    @Test
    fun `request for HarStønad uten match`() {
        val request = InfotrygdSøkRequest(setOf("12345612345"))
        every { stønadService.finnesIInfotrygd(request) } returns InfotrygdFinnesResponse(emptyList(), emptyList())

        val result =
            mockMvc
                .post("/api/stonad/eksisterer") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn()

        val response = objectMapper.readValue<InfotrygdFinnesResponse>(result.response.contentAsString)
        assertThat(response.vedtak).isEmpty()
        assertThat(response.saker).isEmpty()
    }

    @Test
    fun `request for HarStønad uten fnr skal kaste bad request`() {
        val request = InfotrygdSøkRequest(emptySet())

        mockMvc
            .post("/api/stonad/eksisterer") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn()
            .response
            .let { assertThat(it.status).isEqualTo(400) }
    }
}

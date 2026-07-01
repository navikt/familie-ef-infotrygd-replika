package no.nav.familie.ef.infotrygd.replika

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integrasjonstest")
class MainTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun contextLoads() {
    }

    @Test
    fun `health-endepunktet svarer 200 uten autentisering`() {
        mockMvc.get("/internal/health").andExpect { status { isOk() } }
    }

    @Test
    fun `swagger-ui skal være tilgjengelig`() {
        mockMvc.get("/swagger-ui/index.html").andExpect { status { isOk() } }
    }

    @Test
    fun `api-docs skal inneholde openapi-spesifikasjon`() {
        mockMvc
            .get("/v3/api-docs")
            .andExpect {
                status { isOk() }
                content { contentType(org.springframework.http.MediaType.APPLICATION_JSON) }
            }
    }
}

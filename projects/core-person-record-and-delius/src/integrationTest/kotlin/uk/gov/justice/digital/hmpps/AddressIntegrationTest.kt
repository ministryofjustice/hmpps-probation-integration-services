package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AddressIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) {
    @Test
    fun `correctly returns address by id`() {
        mockMvc.get("/address/${PersonGenerator.FULL_PERSON_ADDRESSES[0].id}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(
                        """
                        {
                          "id": ${PersonGenerator.FULL_PERSON_ADDRESSES[0].id},
                          "fullAddress": "1 Main Street, London, PC1 1TS",
                          "addressNumber": "1",
                          "streetName": "Main Street",
                          "district": "London",
                          "county": "   ",
                          "postcode": "PC1 1TS",
                          "uprn": 123456789,
                          "telephoneNumber": "01234 567890",
                          "noFixedAbode": false,
                          "status": {
                            "code": "M",
                            "description": "Main Address"
                          },
                          "type": {
                            "code": "A01C",
                            "description": "Rental accommodation - private rental"
                          },
                          "typeVerified": true,
                          "notes": "Some notes about this address",
                          "startDateTime": ${objectMapper.writeValueAsString(PersonGenerator.FULL_PERSON_ADDRESSES[0].startDate)},
                          "startDate": "${PersonGenerator.FULL_PERSON_ADDRESSES[0].startDate!!.toLocalDate()}"
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT,
                    )
                }
            }
    }

    @Test
    fun `returns 404 if address not found`() {
        mockMvc
            .get("/address/-123") { withToken() }
            .andExpect {
                status { isNotFound() }
                content {
                    json(
                        """{"status":404,"message":"Address with id of -123 not found"}""".trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }
}

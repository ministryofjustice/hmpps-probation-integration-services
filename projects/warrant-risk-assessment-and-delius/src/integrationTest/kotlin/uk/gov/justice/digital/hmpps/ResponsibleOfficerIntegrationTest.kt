package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
internal class ResponsibleOfficerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `returns 404 when CRN does not exist`() {
        mockMvc.get("/responsible-officer/NOTFOUND") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns responsible officer details`() {
        val crn = PersonGenerator.DEFAULT.crn
        mockMvc.get("/responsible-officer/$crn") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "name": {
                            "forename": "Billy",
                            "middleName": "The",
                            "surname": "Kid"
                          },
                          "telephoneNumber": "07707123456",
                          "probationArea": {
                            "code": "B01",
                            "description": "probationAreaDescription"
                          },
                          "replyAddress": {
                            "id": 1000010,
                            "status": "Postal",
                            "officeDescription": "Jail Centre Plus",
                            "buildingName": null,
                            "buildingNumber": "281",
                            "streetName": "Postal Default Street",
                            "townCity": "Postinton",
                            "district": "Postrict",
                            "county": "County Post",
                            "postcode": "NE30 3ZZ"
                          }
                        }
                        """, JsonCompareMode.STRICT
                    )
                }
            }
    }
}

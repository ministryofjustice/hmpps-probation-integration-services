package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import uk.gov.justice.digital.hmpps.model.ResponsibleOfficerDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
internal class ResponsibleOfficerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `returns 404 when CRN does not exist`() {
        mockMvc.get("/sign-and-send/NOTFOUND/${ResponsibleOfficerGenerator.DEFAULT_USER.username}") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns 404 when submitting username does not exist`() {
        val crn = PersonGenerator.DEFAULT.crn

        mockMvc.get("/sign-and-send/$crn/nonexistent") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns responsible officer details`() {
        val crn = PersonGenerator.DEFAULT.crn
        val username = ResponsibleOfficerGenerator.DEFAULT_USER.username

        val response = mockMvc.get("/sign-and-send/$crn/$username") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ResponsibleOfficerDetails>()

        assertThat(response).isEqualTo(
            ResponsibleOfficerDetails(
                name = Name("Billy", "The", "Kid"),
                emailAddress = null,
                telephoneNumber = "07707123456",
                probationArea = CodeAndDescription("B01", "probationAreaDescription"),
                replyAddresses = listOf(
                    OfficeAddress(
                        id = OfficeLocationGenerator.DEFAULT_OFFICE_LOCATION_ID,
                        status = "Default",
                        officeDescription = "Jail Centre Plus",
                        buildingName = null,
                        buildingNumber = "281",
                        streetName = "Postal Default Street",
                        townCity = "Postinton",
                        district = "Postrict",
                        county = "County Post",
                        postcode = "NE30 3ZZ",
                    )
                ),
                userDetails = Name("Billy", null, "Kid"),
            )
        )
    }

    @Test
    fun `returns responsible officer details when managed by prison offender manager`() {
        val crn = PersonGenerator.PRISON_MANAGED.crn
        val username = ResponsibleOfficerGenerator.DEFAULT_USER.username

        val response = mockMvc.get("/sign-and-send/$crn/$username") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ResponsibleOfficerDetails>()

        assertThat(response).isEqualTo(
            ResponsibleOfficerDetails(
                name = Name("Prison", null, "Officer"),
                emailAddress = null,
                telephoneNumber = null,
                probationArea = CodeAndDescription("N01", "N01 Probation Area"),
                replyAddresses = emptyList(),
                userDetails = Name("Billy", null, "Kid"),
            )
        )
    }

    @Test
    fun `returns responsible officer with empty addresses when preferred address not found`() {
        val crn = PersonGenerator.NO_PREFERRED_ADDRESS.crn
        val username = ResponsibleOfficerGenerator.DEFAULT_USER.username

        val response = mockMvc.get("/sign-and-send/$crn/$username") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ResponsibleOfficerDetails>()

        assertThat(response).isEqualTo(
            ResponsibleOfficerDetails(
                name = Name("No", null, "Address"),
                emailAddress = null,
                telephoneNumber = null,
                probationArea = CodeAndDescription("N01", "N01 Probation Area"),
                replyAddresses = emptyList(),
                userDetails = Name("Billy", null, "Kid"),
            )
        )
    }
}
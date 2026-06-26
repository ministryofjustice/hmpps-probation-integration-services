package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalContactGenerator
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import kotlin.text.get

@AutoConfigureMockMvc
@SpringBootTest
internal class BasicDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `returns 404 when CRN does not exist`() {
        mockMvc.get("/basic-details/NOTFOUND") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns full basic details for a person`() {
        val person = PersonGenerator.DEFAULT
        val response = mockMvc.get("/basic-details/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<BasicDetails>()

        assertThat(response.title).isEqualTo("Mr")
        assertThat(response.name).isEqualTo(Name(forename = "Billy", middleName = "The", surname = "Kid"))
        assertThat(response.dateOfBirth).isEqualTo(person.dateOfBirth)
        assertThat(response.nationalInsuranceNumber).isEqualTo("XX000000X")
        assertThat(response.telephoneNumber).isEqualTo("01912525252")
        assertThat(response.mobileNumber).isEqualTo("07707123456")
        assertThat(response.emailAddress).isEqualTo("test@test.com")
        assertThat(response.lastHomeVisitDate).isEqualTo(ContactGenerator.LAST_HOME_VISIT.date)

        assertThat(response.addresses).containsExactlyInAnyOrder(
            AddressDetail(
                id = AddressGenerator.MAIN_ADDRESS.id,
                status = "Main",
                buildingName = "Main Building",
                buildingNumber = "2789",
                streetName = "Main Street",
                townCity = "Maintown",
                district = "MainDistrict",
                county = "Maincounty",
                postcode = "MA30 3IN",
            ),
            AddressDetail(
                id = AddressGenerator.POSTAL_ADDRESS.id,
                status = "Postal",
                buildingName = null,
                buildingNumber = "281",
                streetName = "Postal Default Street",
                townCity = "Postinton",
                district = "Postrict",
                county = "County Post",
                postcode = "NE30 3ZZ",
            ),
        )

        assertThat(response.employers).containsExactlyInAnyOrder(
            Employer(
                employerName = Name(forename = "Billy", middleName = "The", surname = "Kid"),
                employerAddress = EmployerAddress(
                    id = PersonalContactGenerator.EMPLOYER_ADDRESS.id,
                    status = "Main",
                    buildingName = "Employer Building",
                    buildingNumber = "1",
                    streetName = "Employer Street",
                    townCity = "Town City",
                    district = "District",
                    county = "County",
                    postcode = "NE30 3ZZ",
                ),
                telephoneNumber = "01912111111",
                mobileNumber = null,
            ),
        )
    }

    @Test
    fun `end-dated addresses are excluded`() {
        val response = mockMvc.get("/basic-details/${PersonGenerator.DEFAULT.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<BasicDetails>()

        val addressIds = response.addresses.map { it.id }
        assertThat(addressIds).doesNotContain(AddressGenerator.END_DATED_ADDRESS.id)
    }

    @Test
    fun `end-dated employers are excluded`() {
        val response = mockMvc.get("/basic-details/${PersonGenerator.DEFAULT.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<BasicDetails>()

        val employerNames = response.employers.map { it.employerName.surname }
        assertThat(employerNames).doesNotContain("Employer")
    }

    @Test
    fun `returns most recent home visit date only`() {
        val response = mockMvc.get("/basic-details/${PersonGenerator.DEFAULT.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<BasicDetails>()

        assertThat(response.lastHomeVisitDate).isEqualTo(ContactGenerator.LAST_HOME_VISIT.date)
        assertThat(response.lastHomeVisitDate).isNotEqualTo(ContactGenerator.OLDER_HOME_VISIT.date)
    }

    @Test
    fun `returns minimal details when optional fields are absent`() {
        val person = PersonGenerator.NO_OPTIONAL_FIELDS
        val response = mockMvc.get("/basic-details/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<BasicDetails>()

        assertThat(response.title).isNull()
        assertThat(response.nationalInsuranceNumber).isNull()
        assertThat(response.telephoneNumber).isNull()
        assertThat(response.mobileNumber).isNull()
        assertThat(response.emailAddress).isNull()
        assertThat(response.lastHomeVisitDate).isNull()
        assertThat(response.addresses).isEmpty()
        assertThat(response.employers).isEmpty()
        assertThat(response.name.middleName).isNull()
    }
}

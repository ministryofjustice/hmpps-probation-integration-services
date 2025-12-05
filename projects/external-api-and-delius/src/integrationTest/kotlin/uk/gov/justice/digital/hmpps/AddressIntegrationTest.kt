package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.model.AddressWrapper
import uk.gov.justice.digital.hmpps.model.CaseAddress
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

internal class AddressIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `can retrieve address for a case`() {
        val response = mockMvc.get("/case/${PersonGenerator.DEFAULT.crn}/addresses") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<AddressWrapper>()

        assertThat(response.contactDetails.addresses).contains(
            CaseAddress(
                false,
                CodedValue(RD_ADDRESS_STATUS.code, RD_ADDRESS_STATUS.description),
                "buildingName",
                "addressNumber",
                "streetName",
                "town",
                "district",
                "county",
                "postcode",
                LocalDate.now(),
                null,
                "Some notes about the address"
            )
        )
    }
}
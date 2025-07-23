package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON_2
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ETHNICITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_FEMALE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_MALE
import uk.gov.justice.digital.hmpps.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.model.ProbationReferenceData
import uk.gov.justice.digital.hmpps.model.RefData
import uk.gov.justice.digital.hmpps.model.SupervisionResponse
import uk.gov.justice.digital.hmpps.service.PhoneTypes
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

internal class ReferenceDataIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `returns reference data for ethnicity, gender, register_types and address status`() {
        val response = mockMvc.perform(get("/reference-data").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ProbationReferenceData>()
        Assertions.assertEquals(
            response.probationReferenceData["GENDER"],
            listOf(
                RefData(RD_FEMALE.code, RD_FEMALE.description),
                RefData(RD_MALE.code, RD_MALE.description)
            )
        )
        Assertions.assertEquals(
            response.probationReferenceData["PHONE_TYPE"],
            listOf(
                RefData(PhoneTypes.TELEPHONE.name, PhoneTypes.TELEPHONE.description),
                RefData(PhoneTypes.MOBILE.name, PhoneTypes.MOBILE.description)
            )
        )
        Assertions.assertEquals(
            response.probationReferenceData["ETHNICITY"],
            listOf(
                RefData(RD_ETHNICITY.code, RD_ETHNICITY.description),
            )
        )
        Assertions.assertEquals(
            response.probationReferenceData["ADDRESS_TYPE"],
            listOf(
                RefData(RD_ADDRESS_STATUS.code, RD_ADDRESS_STATUS.description),
            )
        )
    }
}

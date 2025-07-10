package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
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

internal class SupervisionIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `returns supervisions`() {
        val start = LocalDate.now()
        val review = LocalDate.now().plusMonths(6)
        val response = mockMvc
            .perform(get("/case/${PERSON.crn}/supervisions").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<SupervisionResponse>()
        Assertions.assertEquals(response.mappaDetail?.startDate, start)
        Assertions.assertEquals(response.mappaDetail?.reviewDate, review)
        Assertions.assertEquals(response.supervisions[0].sentence?.lengthUnits?.name, "Months")
        Assertions.assertEquals(response.supervisions[1].sentence?.lengthUnits, null)
    }

    @Test
    fun `returns crn for nomsId`() {

        val detailResponse = mockMvc
            .perform(get("/identifier-converter/noms-to-crn/${PERSON.nomsId}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<PersonIdentifier>()
        Assertions.assertEquals(detailResponse.crn, PERSON.crn)
    }

    @Test
    fun `returns 404 for nomsId not found`() {
        mockMvc.perform(get("/identifier-converter/noms-to-crn/A0001DZ").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `returns reference data for ethnicity, gender and register_types`() {
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
    }
}

package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON_2
import uk.gov.justice.digital.hmpps.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.model.SupervisionResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

internal class SupervisionIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `returns supervisions`() {
        val start = LocalDate.now()
        val review = LocalDate.now().plusMonths(6)
        val response = mockMvc
            .get("/case/${PERSON.crn}/supervisions") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SupervisionResponse>()
        Assertions.assertEquals(start, response.mappaDetail?.startDate)
        Assertions.assertEquals(review, response.mappaDetail?.reviewDate)
        Assertions.assertEquals("Months", response.supervisions[0].sentence?.lengthUnits?.name)
        Assertions.assertEquals(null, response.supervisions[1].sentence?.lengthUnits)
    }

    @Test
    fun `returns crn for nomsId`() {

        val detailResponse = mockMvc
            .get("/identifier-converter/noms-to-crn/${PERSON.nomsId}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<PersonIdentifier>()
        Assertions.assertEquals(detailResponse.crn, PERSON.crn)
    }

    @Test
    fun `returns 404 for nomsId not found`() {
        mockMvc.get("/identifier-converter/noms-to-crn/A0001DZ") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns no mappa level if invalid mappa level`() {
        val response = mockMvc
            .get("/case/${PERSON_2.crn}/supervisions") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SupervisionResponse>()
        Assertions.assertEquals(null, response.mappaDetail?.level)
    }
}

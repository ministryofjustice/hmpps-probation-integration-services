package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sevice.model.NomsUpdates
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class NomsNumberIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var custodyRepository: CustodyRepository

    @Test
    @Order(1)
    fun `API call retuns not found in delius`() {
        val crn = "ZZZ"

        val result = mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<NomsUpdates>()

        Assertions.assertThat(result.personMatches.first().matchReason.message)
            .isEqualTo("Custody record not found in Delius without a booking reference")
    }

    @Test
    @Order(2)
    fun `API call retuns Noms number already in delius`() {
        val crn = PersonGenerator.PERSON_WITH_NOMS.crn

        val result = mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<NomsUpdates>()

        Assertions.assertThat(result.personMatches.first().matchReason.message)
            .isEqualTo("This person already has a noms number in Delius")
    }

    @Test
    @Order(3)
    fun `API call retuns single match via prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NO_NOMS.crn

        val result = mockMvc
            .perform(post("/person/populate-noms-number?trialOnly=false").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<NomsUpdates>()

        Assertions.assertThat(result.personMatches.first().matchReason.message)
            .isEqualTo("Found a single match in prison search api")
    }

    @Test
    @Order(4)
    fun `API call retuns a single match from multiple matches found in prison search api does not update delius`() {
        val crn = PersonGenerator.PERSON_WITH_MULTI_MATCH.crn

        val result = mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<NomsUpdates>()

        Assertions.assertThat(result.personMatches.first().matchReason.message)
            .isEqualTo("Found a single match in prison search api and matching criteria.")
        val person =
            personRepository.findByNomsNumberAndSoftDeletedIsFalse(result.personMatches.first().matchDetail!!.nomsNumber)
        Assertions.assertThat(person).isNull()
    }

    @Test
    @Order(5)
    fun `API call retuns a single match from multiple matches found in prison search api updated person in delius`() {
        val crn = PersonGenerator.PERSON_WITH_MULTI_MATCH.crn
        val custodyId = personRepository.findByCrn(crn).first().custody.id

        val result = mockMvc
            .perform(post("/person/populate-noms-number?trialOnly=false").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<NomsUpdates>()

        Assertions.assertThat(result.personMatches.first().matchReason.message)
            .isEqualTo("Found a single match in prison search api and matching criteria.")
        val person =
            personRepository.findByNomsNumberAndSoftDeletedIsFalse(result.personMatches.first().matchDetail!!.nomsNumber)

        val custody = custodyRepository.findByIdOrNull(custodyId)
        Assertions.assertThat(person?.crn).isEqualTo(crn)
        Assertions.assertThat(custody?.bookingRef).isEqualTo("13831A")
    }

    @Test
    @Order(6)
    fun `API call cant determine a match from multiple matches found in prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NO_MATCH.crn

        val result = mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<NomsUpdates>()

        Assertions.assertThat(result.personMatches.first().matchReason.message)
            .isEqualTo("Unable to find a unique match using matching criteria.")
    }

    @Test
    @Order(7)
    fun `API call retuns single match but noms number already in delius via prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS.crn

        val result = mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<NomsUpdates>()

        Assertions.assertThat(result.personMatches.first().matchReason.message)
            .isEqualTo("Person was matched to noms number but another person exists in delius with this noms number")
    }
}

package uk.gov.justice.digital.hmpps

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.api.model.MatchRequest
import uk.gov.justice.digital.hmpps.api.model.MatchResponse
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.GENDER_FEMALE
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.OffenderAlias
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.api.model.MatchedBy

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class MatchIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var personRepository: PersonRepository

    @Autowired
    internal lateinit var entityManager: EntityManager

    @Autowired
    internal lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `search by name and date of birth including aliases`() {
        val people = personRepository.saveAll(
            listOf(
                PersonGenerator.generate(
                    "P723456",
                    forename = "Robert",
                    surname = "Smith",
                    dateOfBirth = LocalDate.of(1980, 1, 1),
                    currentDisposal = true,
                ),
                PersonGenerator.generate(
                    "P723457",
                    forename = "June",
                    surname = "Smith",
                    dateOfBirth = LocalDate.of(1979, 3, 12),
                    gender = GENDER_FEMALE,
                    currentDisposal = true,
                ),
                PersonGenerator.generate(
                    "P723458",
                    forename = "May",
                    surname = "Brown",
                    dateOfBirth = LocalDate.of(1981, 10, 9),
                    gender = GENDER_FEMALE,
                    currentDisposal = true,
                )
            )
        )

        transactionTemplate.execute {
            people.forEach { person ->
                entityManager.persist(PersonGenerator.generatePersonManager(person))
                entityManager.persist(
                    SentenceGenerator.generateEvent(
                        person,
                        referralDate = LocalDate.now().minusDays(14)
                    )
                )
            }
            val may = people.single { it.crn == "P723458" }
            entityManager.persist(
                OffenderAlias(
                    IdGenerator.getAndIncrement(),
                    may.id,
                    LocalDate.of(1980, 1, 1),
                    "Marge",
                    null,
                    false,
                    "Smith",
                    null,
                    GENDER_FEMALE,
                )
            )
        }

        val r1 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(
                        MatchRequest(
                            firstName = "Robert",
                            surname = "Smith",
                            dateOfBirth = LocalDate.of(1980, 1, 1)
                        )
                    )
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r1.matches).hasSize(1)
        assertThat(r1.matches.first().offender.otherIds.crn).isEqualTo("P723456")
        assertThat(r1.matchedBy).isEqualTo(MatchedBy.ALL_SUPPLIED)

        val r2 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(MatchRequest(surname = "Smith"))
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r2.matches).hasSize(2)
        assertThat(r2.matches.map { it.offender.otherIds.crn }).containsExactlyInAnyOrder("P723456", "P723457")
        assertThat(r2.matchedBy).isEqualTo(MatchedBy.ALL_SUPPLIED)

        val r3 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(
                        MatchRequest(
                            surname = "Smith",
                            dateOfBirth = LocalDate.of(1980, 1, 1),
                        )
                    )
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r3.matches).hasSize(1)
        assertThat(r3.matches.map { it.offender.otherIds.crn }).containsExactlyInAnyOrder("P723456")
        assertThat(r3.matchedBy).isEqualTo(MatchedBy.ALL_SUPPLIED)

        val r4 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(
                        MatchRequest(
                            firstName = "Marge",
                            surname = "Smith",
                            dateOfBirth = LocalDate.of(1980, 1, 1),
                        )
                    )
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r4.matches).hasSize(1)
        assertThat(r4.matches.map { it.offender.otherIds.crn }).containsExactlyInAnyOrder("P723458")
        assertThat(r4.matchedBy).isEqualTo(MatchedBy.ALL_SUPPLIED_ALIAS)

        val r5 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(
                        MatchRequest(
                            pncNumber = "09/516048H",
                            surname = "Smith",
                            firstName = "Robert",
                            dateOfBirth = LocalDate.of(1980, 1, 1)
                        )
                    )
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r5.matches).hasSize(1)
        assertThat(r5.matches.map { it.offender.otherIds.crn }).containsExactlyInAnyOrder("P723456")
        assertThat(r5.matchedBy).isEqualTo(MatchedBy.NAME)

        val r6 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(
                        MatchRequest(
                            surname = "Smith",
                            firstName = "James",
                            dateOfBirth = LocalDate.of(1980, 1, 1)
                        )
                    )
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r6.matches).hasSize(1)
        assertThat(r6.matches.map { it.offender.otherIds.crn }).containsExactlyInAnyOrder("P723456")
        assertThat(r6.matchedBy).isEqualTo(MatchedBy.PARTIAL_NAME)

        val r7 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(MatchRequest(surname = "Smith", dateOfBirth = LocalDate.of(1980, 1, 2)))
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r7.matches).hasSize(1)
        assertThat(r7.matches.map { it.offender.otherIds.crn }).containsExactlyInAnyOrder("P723456")
        assertThat(r7.matchedBy).isEqualTo(MatchedBy.PARTIAL_NAME_DOB_LENIENT)

        val r8 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(MatchRequest(surname = "Smith", dateOfBirth = LocalDate.of(1980, 2, 1)))
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r8.matches).hasSize(1)
        assertThat(r8.matches.map { it.offender.otherIds.crn }).containsExactlyInAnyOrder("P723456")
        assertThat(r8.matchedBy).isEqualTo(MatchedBy.PARTIAL_NAME_DOB_LENIENT)
    }

    @Test
    fun `search by identifiers`() {
        personRepository.saveAll(
            listOf(
                PersonGenerator.generate(
                    "P223456",
                    nomsNumber = "S3477CH",
                    pncNumber = "1999/9158411A",
                    dateOfBirth = LocalDate.of(1999, 1, 1)
                ),
                PersonGenerator.generate(
                    "P223457",
                    nomsNumber = "S3478CH",
                    pncNumber = "1973/5052670T",
                    dateOfBirth = LocalDate.of(1973, 1, 1)
                ),
                PersonGenerator.generate(
                    "P223458",
                    nomsNumber = "S3479CH",
                    pncNumber = "1976/7812661D",
                    dateOfBirth = LocalDate.of(1976, 1, 1)
                )
            )
        )

        val r1 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(MatchRequest(surname = "Any", pncNumber = "1973/5052670T")).withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r1.matches).hasSize(1)
        assertThat(r1.matches.first().offender.otherIds.crn).isEqualTo("P223457")
        assertThat(r1.matchedBy).isEqualTo(MatchedBy.EXTERNAL_KEY)

        val r2 = mockMvc
            .perform(
                post("/probation-cases/match")
                    .withJson(MatchRequest(surname = "Any", pncNumber = "99/9158411A")).withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MatchResponse>()

        assertThat(r2.matches).hasSize(1)
        assertThat(r2.matches.first().offender.otherIds.crn).isEqualTo("P223456")
        assertThat(r2.matchedBy).isEqualTo(MatchedBy.EXTERNAL_KEY)
    }
}
package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.AI_PREVIOUS_CRN
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_DISABILITY_CONDITION
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_DISABILITY_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_FEMALE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_MALE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_NATIONALITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_RELIGION
import uk.gov.justice.digital.hmpps.integration.delius.entity.AdditionalIdentifier
import uk.gov.justice.digital.hmpps.integration.delius.entity.Disability
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAlias
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

internal class SearchIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `search without params responds with client error`() {
        mockMvc
            .perform(
                post("/search/probation-cases")
                    .withJson(SearchRequest())
                    .withToken()
            )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `search by name and date of birth including aliases`() {
        val people = personRepository.saveAll(
            listOf(
                PersonGenerator.generate(
                    "P123456",
                    forename = "Robert",
                    surname = "Smith",
                    dateOfBirth = LocalDate.of(1980, 1, 1)
                ),
                PersonGenerator.generate(
                    "P123457",
                    forename = "June",
                    surname = "Smith",
                    dateOfBirth = LocalDate.of(1979, 3, 12),
                    gender = RD_FEMALE
                ),
                PersonGenerator.generate(
                    "P123458",
                    forename = "May",
                    surname = "Brown",
                    dateOfBirth = LocalDate.of(1981, 10, 9),
                    gender = RD_FEMALE
                )
            )
        )

        transactionTemplate.execute {
            val may = people.single { it.crn == "P123458" }
            entityManager.persist(
                PersonAlias(
                    may, "Marge", "Smith", LocalDate.of(1980, 1, 1), RD_FEMALE, null, null,
                    id = IdGenerator.getAndIncrement()
                )
            )
        }

        val r1 = mockMvc
            .perform(
                post("/search/probation-cases")
                    .withJson(
                        SearchRequest(
                            firstName = "Robert",
                            surname = "Smith",
                            dateOfBirth = LocalDate.of(1980, 1, 1)
                        )
                    )
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r1).hasSize(1)
        assertThat(r1.first().otherIds.crn).isEqualTo("P123456")

        val r2 = mockMvc
            .perform(
                post("/search/probation-cases")
                    .withJson(SearchRequest(surname = "Smith"))
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r2).hasSize(2)
        assertThat(r2.map { it.otherIds.crn }).containsExactlyInAnyOrder("P123456", "P123457")

        val r3 = mockMvc
            .perform(
                post("/search/probation-cases")
                    .withJson(
                        SearchRequest(
                            surname = "Smith",
                            dateOfBirth = LocalDate.of(1980, 1, 1),
                            includeAliases = true
                        )
                    )
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r3).hasSize(2)
        assertThat(r3.map { it.otherIds.crn }).containsExactlyInAnyOrder("P123456", "P123458")

        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }

    @Test
    fun `search by identifiers`() {
        val people = personRepository.saveAll(
            listOf(
                PersonGenerator.generate(
                    "P223456",
                    nomsId = "S3477CH",
                    pnc = "1999/9158411A",
                    dateOfBirth = LocalDate.of(1999, 1, 1)
                ),
                PersonGenerator.generate(
                    "P223457",
                    nomsId = "S3478CH",
                    pnc = "1973/5052670T",
                    dateOfBirth = LocalDate.of(1973, 1, 1)
                ),
                PersonGenerator.generate(
                    "P223458",
                    nomsId = "S3479CH",
                    pnc = "1976/7812661D",
                    dateOfBirth = LocalDate.of(1976, 1, 1)
                )
            )
        )

        transactionTemplate.execute {
            val person = people.single { it.crn == "P223458" }
            entityManager.persist(
                AdditionalIdentifier(
                    person,
                    "N223458",
                    AI_PREVIOUS_CRN,
                    id = IdGenerator.getAndIncrement(),
                )
            )
        }

        val r1 = mockMvc
            .perform(post("/search/probation-cases").withJson(SearchRequest(pncNumber = "1973/5052670T")).withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r1).hasSize(1)
        assertThat(r1.first().otherIds.crn).isEqualTo("P223457")

        val r2 = mockMvc
            .perform(post("/search/probation-cases").withJson(SearchRequest(nomsNumber = "S3477CH")).withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r2).hasSize(1)
        assertThat(r2.first().otherIds.crn).isEqualTo("P223456")

        val r3 = mockMvc
            .perform(post("/search/probation-cases").withJson(SearchRequest(crn = "P223458")).withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r3).hasSize(1)
        assertThat(r3.first().otherIds.nomsNumber).isEqualTo("S3479CH")

        val r4 = mockMvc
            .perform(post("/search/probation-cases").withJson(SearchRequest(crn = "N223458")).withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r4).hasSize(1)
        assertThat(r4.first().otherIds.nomsNumber).isEqualTo("S3479CH")

        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }

    @Test
    fun `can map entire object tree`() {
        val person = PersonGenerator.generate(
            "G123456",
            "G1234HJ",
            "Bobby",
            "Brown",
            LocalDate.of(1998, 2, 23),
            "1998/2031267B",
            secondName = "James",
            telephoneNumber = "0191 233 7563",
            mobileNumber = "07378851612",
            emailAddress = "bobby@gmail.com",
            currentDisposal = false,
        )
        transactionTemplate.execute {
            entityManager.persist(person)
            entityManager.persist(
                PersonAlias(
                    person,
                    "Jimmy",
                    "Jameson",
                    LocalDate.of(2000, 10, 13),
                    RD_FEMALE,
                    null,
                    null,
                    id = IdGenerator.getAndIncrement()
                )
            )
            entityManager.persist(
                Disability(
                    person,
                    RD_DISABILITY_TYPE,
                    RD_DISABILITY_CONDITION,
                    LocalDate.of(2010, 10, 13),
                    notes = "Some notes about the disability",
                    id = IdGenerator.getAndIncrement()
                )
            )
        }

        val r1 = mockMvc
            .perform(post("/search/probation-cases").withJson(SearchRequest(crn = "G123456")).withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<ProbationCaseDetail>>()

        assertThat(r1).hasSize(1)
        assertThat(r1.first()).isEqualTo(
            ProbationCaseDetail(
                OtherIds("G123456", "1998/2031267B", "G1234HJ"),
                "Bobby",
                "Brown",
                LocalDate.of(1998, 2, 23),
                RD_MALE.description,
                listOf("James"),
                CaseProfile(
                    nationality = RD_NATIONALITY.description,
                    religion = RD_RELIGION.description,
                    disabilities = listOf(
                        CaseDisability(
                            CodedValue(RD_DISABILITY_TYPE.code, RD_DISABILITY_TYPE.description),
                            CodedValue(RD_DISABILITY_CONDITION.code, RD_DISABILITY_CONDITION.description),
                            LocalDate.of(2010, 10, 13),
                            notes = "Some notes about the disability",
                        )
                    ),
                ),
                ContactDetails(
                    listOf(PhoneNumber("0191 233 7563", "TELEPHONE"), PhoneNumber("07378851612", "MOBILE")),
                    listOf("bobby@gmail.com")
                ),
                listOf(CaseAlias("Jimmy", "Jameson", LocalDate.of(2000, 10, 13), RD_FEMALE.description)),
                false
            )
        )

        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }
}
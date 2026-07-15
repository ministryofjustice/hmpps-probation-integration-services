package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EVENT
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integration.delius.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integration.delius.entity.Staff
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseDetailsIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `can retrieve basic case details`() {
        val person = PersonGenerator.DEFAULT
        val crn = person.crn
        val eventNumber = EVENT.number
        val detailResponse = mockMvc.get("/case-details/$crn/$eventNumber") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(detailResponse, equalTo(getDetailResponse()))
    }

    @ParameterizedTest
    @MethodSource("limitedAccess")
    fun `Response includes lao info when case is Restricted Or Excluded`(person: Person, lad: LimitedAccessDetail) {
        val response = mockMvc.get("/case-details/${person.crn}/1") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(response.limitedAccess, equalTo(lad))
    }

    @ParameterizedTest
    @MethodSource("limitedAccess")
    fun `LAO info for a Restricted Or Excluded`(person: Person, lad: LimitedAccessDetail) {
        val response = mockMvc.get("/case/${person.crn}/access-limitations") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<LimitedAccessDetail>()

        assertThat(response, equalTo(lad))
    }

    @Test
    fun `release details are correctly displayed`() {
        val person = PersonGenerator.WITH_RELEASE_DATE
        val crn = person.crn
        val eventNumber = 1
        val detailResponse = mockMvc.get("/case-details/$crn/$eventNumber") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseDetails>()

        MatcherAssert.assertThat(
            detailResponse, equalTo(
                CaseDetails(
                    null,
                    Name(person.forename, person.surname, person.secondName),
                    person.dateOfBirth,
                    person.gender.description,
                    Appearance(
                        SentenceGenerator.RELEASED_COURT_APPEARANCE.date.toLocalDate(),
                        Court(DataGenerator.COURT.name)
                    ),
                    SentenceSummary(
                        SentenceGenerator.RELEASE_DATE.date
                    ),
                    ResponsibleProvider(DEFAULT_PROVIDER.code, DEFAULT_PROVIDER.description),
                    null,
                    person.dynamicRsrScore,
                    null,
                )
            )
        )
    }

    @Test
    fun `returns managed case crns for an officer code`() {
        val expectedCrns = createManagedCases("M123456", 2)

        val response = mockMvc.get("/staff/M123456/managed-cases") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CrnPage>()

        assertThat(response.number, equalTo(0))
        assertThat(response.size, equalTo(10))
        assertThat(response.totalElements, equalTo(2))
        assertThat(response.totalPages, equalTo(1))
        assertThat(response.content, containsInAnyOrder(*expectedCrns.toTypedArray()))
    }

    @Test
    fun `returns requested managed cases page`() {
        createManagedCases("M234567", 3)

        val response = mockMvc.get("/staff/M234567/managed-cases?page=1&size=2") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CrnPage>()

        assertThat(response.number, equalTo(1))
        assertThat(response.size, equalTo(2))
        assertThat(response.totalElements, equalTo(3))
        assertThat(response.totalPages, equalTo(2))
        assertThat(response.content.size, equalTo(1))
    }

    @Test
    fun `returns empty managed cases page when officer has no people`() {
        transactionTemplate.execute { createOfficer("M345678") }

        val response = mockMvc.get("/staff/M345678/managed-cases?size=1") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CrnPage>()

        assertThat(response.number, equalTo(0))
        assertThat(response.size, equalTo(1))
        assertThat(response.totalElements, equalTo(0))
        assertThat(response.totalPages, equalTo(0))
        assertThat(response.content, empty())
    }

    @Test
    fun `returns not found when officer code does not exist`() {
        val response = mockMvc.get("/staff/M000000/managed-cases") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.status, equalTo(404))
        assertThat(response.message, equalTo("Staff with officerCode of M000000 not found"))
    }

    @Test
    fun `officer code matching is case insensitive`() {
        val expectedCrns = createManagedCases("M456789", 2)

        val response = mockMvc.get("/staff/m456789/managed-cases") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CrnPage>()

        assertThat(response.content, containsInAnyOrder(*expectedCrns.toTypedArray()))
    }

    private fun getDetailResponse(): CaseDetails {
        return CaseDetails(
            PersonGenerator.DEFAULT.nomsId,
            Name(
                PersonGenerator.DEFAULT.forename,
                PersonGenerator.DEFAULT.surname,
                PersonGenerator.DEFAULT.secondName,
            ),
            PersonGenerator.DEFAULT.dateOfBirth,
            PersonGenerator.DEFAULT.gender.description,
            Appearance(
                EVENT.courtAppearances.first().date.toLocalDate(),
                Court(DataGenerator.COURT.name)
            ),
            null,
            ResponsibleProvider(DEFAULT_PROVIDER.code, DEFAULT_PROVIDER.description),
            3,
            PersonGenerator.DEFAULT.dynamicRsrScore,
            null,
        )
    }

    companion object {
        @JvmStatic
        fun limitedAccess() = listOf(
            Arguments.of(
                PersonGenerator.EXCLUSION,
                LimitedAccessDetail(
                    excludedFrom = listOf(LimitedAccess.ExcludedFrom("Unknown")),
                    exclusionMessage = "There is an exclusion on this person",
                    restrictedTo = emptyList(),
                    restrictionMessage = null,
                )
            ),
            Arguments.of(
                PersonGenerator.RESTRICTION,
                LimitedAccessDetail(
                    excludedFrom = emptyList(),
                    exclusionMessage = null,
                    restrictedTo = listOf(
                        LimitedAccess.RestrictedTo("Unknown"),
                        LimitedAccess.RestrictedTo("john.smith@moj.gov.uk")
                    ),
                    restrictionMessage = "There is a restriction on this person",
                )
            ),
        )
    }

    private fun createManagedCases(officerCode: String, count: Int): List<String> {
        val provider = DataGenerator.DEFAULT_PROVIDER
        val team = DataGenerator.DEFAULT_TEAM
        val crns = (1..count).map { "M${IdGenerator.getAndIncrement().toString().takeLast(6).padStart(6, '0')}" }
        transactionTemplate.execute {
            val staff = createOfficer(officerCode)
            crns.forEach { crn ->
                val person = PersonGenerator.generate(crn)
                entityManager.persist(person)
                val manager = PersonManager(person, provider, team, staff, true, IdGenerator.getAndIncrement())
                entityManager.persist(manager)
                entityManager.persist(ResponsibleOfficer(person, manager, null, id = IdGenerator.getAndIncrement()))
            }
        }
        return crns
    }

    private fun createOfficer(officerCode: String): Staff {
        val staff = Staff(officerCode, "Case", "Officer", null, IdGenerator.getAndIncrement())
        entityManager.persist(staff)
        return staff
    }
}

private data class CrnPage(
    val content: List<String>,
    val number: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.NO_EVENTS_PERSON
import uk.gov.justice.digital.hmpps.data.generator.SupervisionPackageGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class SupervisionPackageIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `returns unauthorized when no token is supplied`() {
        mockMvc.get("/arns/${NO_EVENTS_PERSON.crn}/supervision-package")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns not found when CRN does not exist`() {
        mockMvc.get("/arns/Z123456/supervision-package") {
            withToken()
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.message") { value("Person with crn of Z123456 not found") }
        }
    }

    @Test
    fun `returns an empty list for a person without supervision packages`() {
        mockMvc.get("/arns/${NO_EVENTS_PERSON.crn}/supervision-package") {
            withToken()
        }.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @ParameterizedTest(name = "{0} has latest change at {1}")
    @CsvSource(
        "S123451,",
        "S123452,",
        "S123453,2026-01-02T10:00:00Z",
        "S123454,2026-01-02T10:00:00Z",
        "S123455,2026-01-03T10:00:00Z",
        "S123456,2026-01-03T10:00:00Z"
    )
    fun `returns creation date and latest type change regardless of insertion order`(
        crn: String,
        expectedChangedDate: String?
    ) {
        val packages = listOf(
            SupervisionPackageGenerator.SINGLE_PACKAGE,
            SupervisionPackageGenerator.UNCHANGED_TYPE,
            SupervisionPackageGenerator.CHANGED_TYPE,
            SupervisionPackageGenerator.UNCHANGED_AFTER_CHANGE,
            SupervisionPackageGenerator.RETURNED_TO_ORIGINAL_TYPE,
            SupervisionPackageGenerator.UNCHANGED_AFTER_RETURN
        ).single { it.first().person.crn == crn }

        mockMvc.get("/arns/$crn/supervision-package") {
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].crn") { value(crn) }
            jsonPath("$[0].eventId") { value(packages.first().disposal.event.id) }
            jsonPath("$[0].createdDate") { value("2026-01-01T10:00:00Z") }
            jsonPath("$[0].supervisionPackageChangedDate") {
                if (expectedChangedDate == null) doesNotExist() else value(expectedChangedDate)
            }
        }
    }

    @Test
    fun `groups packages by disposal and excludes packages belonging to another person`() {
        val person = SupervisionPackageGenerator.MULTIPLE_DISPOSALS_PERSON
        val firstEvent = SupervisionPackageGenerator.FIRST_EVENT
        val secondEvent = SupervisionPackageGenerator.SECOND_EVENT

        mockMvc.get("/arns/${person.crn}/supervision-package") {
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[*].crn") { value(containsInAnyOrder(person.crn, person.crn)) }
            jsonPath("$[*].eventId") {
                value(containsInAnyOrder(firstEvent.id.toInt(), secondEvent.id.toInt()))
            }
            jsonPath("$[*].createdDate") {
                value(containsInAnyOrder("2026-01-01T10:00:00Z", "2026-01-01T10:00:00Z"))
            }
            jsonPath("$[*].supervisionPackageChangedDate") {
                value(containsInAnyOrder("2026-01-02T10:00:00Z", "2026-01-02T10:00:00Z"))
            }
        }
    }
}

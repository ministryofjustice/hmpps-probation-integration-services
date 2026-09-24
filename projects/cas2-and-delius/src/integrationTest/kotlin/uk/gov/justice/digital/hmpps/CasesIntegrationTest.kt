package uk.gov.justice.digital.hmpps

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.CodeDescription
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.SexualOffenceRegistration
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.jsonPath
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CasesIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate,
) {
    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/cases/${nextCrn()}/sexual-offence-registrations")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns sexual offence registrations for case`() {
        val crn = nextCrn()
        val person = Person(id = IdGenerator.getAndIncrement(), crn = crn)
        val firstType = RegistrationGenerator.generateType(code = "RSC", description = "Registered Sex Offender")
        val firstCategory = RegistrationGenerator.generateCategory(code = "CAT1", description = "Category 1")
        val secondType =
            RegistrationGenerator.generateType(code = "SOPS", description = "Sex Offender Prevention Scheme")
        val secondCategory = RegistrationGenerator.generateCategory(code = "CAT2", description = "Category 2")
        val ignoredType = RegistrationGenerator.generateType(code = "MAPP", description = "MAPPA")
        val ignoredCategory = RegistrationGenerator.generateCategory(code = "CAT3", description = "Category 3")
        val otherPerson = Person(id = IdGenerator.getAndIncrement(), crn = nextCrn())
        val otherType = RegistrationGenerator.generateType(code = "SHPO", description = "Sexual Harm Prevention Order")
        val otherCategory = RegistrationGenerator.generateCategory(code = "CAT4", description = "Category 4")
        val activeRegistration = RegistrationGenerator.generate(
            person = person,
            type = firstType,
            category = firstCategory,
            registrationDate = LocalDate.of(2024, 1, 1),
            nextReviewDate = LocalDate.of(2025, 1, 1),
        )
        val deregisteredRegistration = RegistrationGenerator.generate(
            person = person,
            type = secondType,
            category = secondCategory,
            registrationDate = LocalDate.of(2024, 6, 1),
            nextReviewDate = null,
        )
        val ignoredRegistration = RegistrationGenerator.generate(
            person = person,
            type = ignoredType,
            category = ignoredCategory,
            registrationDate = LocalDate.of(2024, 3, 1),
        )
        val otherPersonRegistration = RegistrationGenerator.generate(
            person = otherPerson,
            type = otherType,
            category = otherCategory,
            registrationDate = LocalDate.of(2024, 8, 1),
        )
        val deregistration = RegistrationGenerator.generateDeregistration(
            registration = deregisteredRegistration,
            endDate = LocalDate.of(2024, 9, 1),
        )

        val expected = listOf(
            SexualOffenceRegistration(
                type = CodeDescription(code = "RSC", description = "Registered Sex Offender"),
                category = CodeDescription(code = "CAT1", description = "Category 1"),
                date = LocalDate.of(2024, 1, 1),
                nextReviewDate = LocalDate.of(2025, 1, 1),
                endDate = null,
            ),
            SexualOffenceRegistration(
                type = CodeDescription(code = "SOPS", description = "Sex Offender Prevention Scheme"),
                category = CodeDescription(code = "CAT2", description = "Category 2"),
                date = LocalDate.of(2024, 6, 1),
                nextReviewDate = null,
                endDate = LocalDate.of(2024, 9, 1),
            ),
        )

        persist(
            person,
            firstType,
            firstCategory,
            secondType,
            secondCategory,
            ignoredType,
            ignoredCategory,
            otherPerson,
            otherType,
            otherCategory,
            activeRegistration,
            deregisteredRegistration,
            ignoredRegistration,
            otherPersonRegistration,
            deregistration,
        )

        val response = mockMvc.get("/cases/$crn/sexual-offence-registrations") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SexualOffenceRegistrations>()

        assertThat(response.crn).isEqualTo(crn)
        assertThat(response.sexualOffenceRegistrations).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    fun `returns empty list when case has no sexual offence registrations`() {
        val person = Person(id = IdGenerator.getAndIncrement(), crn = nextCrn())
        persist(person)

        val response = mockMvc.get("/cases/${person.crn}/sexual-offence-registrations") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SexualOffenceRegistrations>()

        assertThat(response).isEqualTo(
            SexualOffenceRegistrations(
                crn = person.crn,
                sexualOffenceRegistrations = emptyList(),
            )
        )
    }

    @Test
    fun `returns not found when case does not exist`() {
        val crn = nextCrn()

        mockMvc.get("/cases/$crn/sexual-offence-registrations") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status", 404)
                jsonPath("$.message", "Person with crn of $crn not found")
            }
    }

    @Test
    fun `returns latest deregistration date when registration has multiple deregistrations`() {
        val crn = nextCrn()
        val person = Person(id = IdGenerator.getAndIncrement(), crn = crn)
        val type = RegistrationGenerator.generateType(code = "RSC", description = "Registered Sex Offender")
        val category = RegistrationGenerator.generateCategory(code = "CAT1", description = "Category 1")
        val registration = RegistrationGenerator.generate(
            person = person,
            type = type,
            category = category,
            registrationDate = LocalDate.of(2024, 1, 1),
            nextReviewDate = LocalDate.of(2025, 1, 1),
        )
        val firstDeregistration = RegistrationGenerator.generateDeregistration(
            registration = registration,
            endDate = LocalDate.of(2024, 6, 1),
        )
        val secondDeregistration = RegistrationGenerator.generateDeregistration(
            registration = registration,
            endDate = LocalDate.of(2024, 9, 1),
        )
        val thirdDeregistration = RegistrationGenerator.generateDeregistration(
            registration = registration,
            endDate = LocalDate.of(2024, 7, 15),
        )

        val expected = SexualOffenceRegistration(
            type = CodeDescription(code = "RSC", description = "Registered Sex Offender"),
            category = CodeDescription(code = "CAT1", description = "Category 1"),
            date = LocalDate.of(2024, 1, 1),
            nextReviewDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2024, 9, 1), // Should be the latest date
        )

        persist(
            person,
            type,
            category,
            registration,
            firstDeregistration,
            secondDeregistration,
            thirdDeregistration,
        )

        val response = mockMvc.get("/cases/$crn/sexual-offence-registrations") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SexualOffenceRegistrations>()

        assertThat(response.crn).isEqualTo(crn)
        assertThat(response.sexualOffenceRegistrations).containsExactly(expected)
    }

    private fun persist(vararg entities: Any) {
        transactionTemplate.execute {
            entities.forEach(entityManager::persist)
            entityManager.flush()
        }
    }

    private fun nextCrn(): String = "Z${IdGenerator.getAndIncrement().toString().takeLast(6).padStart(6, '0')}"
}


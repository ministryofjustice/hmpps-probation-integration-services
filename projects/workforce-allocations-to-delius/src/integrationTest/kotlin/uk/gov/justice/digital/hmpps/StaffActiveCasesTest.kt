package uk.gov.justice.digital.hmpps

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StaffActiveCasesTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val entityManager: EntityManager,
    transactionManager: PlatformTransactionManager,
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    @Test
    fun `successful post response`() {
        val staff = StaffGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        mockMvc.post("/staff/${staff.code}/active-cases") {
            withToken()
            json = listOf(person.crn)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(staff.code) }
                jsonPath("$.name.forename") { value(staff.forename) }
                jsonPath("$.name.surname") { value(staff.surname) }
                jsonPath("$.grade") { value("PSO") }
                jsonPath("$.cases[0].crn") { value(person.crn) }
                jsonPath("$.cases[0].name.forename") { value(person.forename) }
                jsonPath("$.cases[0].name.surname") { value(person.surname) }
                jsonPath("$.cases[0].type") { value("CUSTODY") }
                jsonPath("$.cases[0].initialAllocationDate") { value("2022-06-24") }
            }
    }

    @Test
    fun `successful get response`() {
        val staff = StaffGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        mockMvc.get("/staff/${staff.code}/active-cases") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(staff.code) }
                jsonPath("$.name.forename") { value(staff.forename) }
                jsonPath("$.name.surname") { value(staff.surname) }
                jsonPath("$.grade") { value("PSO") }
                jsonPath("$.cases[0].crn") { value(person.crn) }
                jsonPath("$.cases[0].name.forename") { value(person.forename) }
                jsonPath("$.cases[0].name.surname") { value(person.surname) }
                jsonPath("$.cases[0].type") { value("CUSTODY") }
                jsonPath("$.cases[0].initialAllocationDate") { value("2022-06-24") }
            }
    }

    @Test
    fun `post response can be empty`() {
        val staff = StaffGenerator.DEFAULT
        mockMvc.post("/staff/${staff.code}/active-cases") {
            withToken()
            json = emptyList<String>()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(staff.code) }
                jsonPath("$.cases") { isEmpty() }
            }
    }

    @Test
    fun `get response ignores invalid caseload rows`() {
        persistCaseload(
            CaseloadGenerator.generate(
                person = personReference(PersonGenerator.DEFAULT.id),
                staff = staffReference(StaffGenerator.STAFF_WITH_USER.id),
                team = teamReference(TeamGenerator.ALLOCATION_TEAM.id),
                roleCode = "AP"
            )
        )
        persistCaseload(
            CaseloadGenerator.generate(
                person = personReference(PersonGenerator.DEFAULT.id),
                staff = staffReference(StaffGenerator.STAFF_WITH_USER.id),
                team = teamReference(TeamGenerator.ALLOCATION_TEAM.id),
                trustProviderFlag = true
            )
        )

        mockMvc.get("/staff/${StaffGenerator.STAFF_WITH_USER.code}/active-cases") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(StaffGenerator.STAFF_WITH_USER.code) }
                jsonPath("$.cases") { isEmpty() }
            }
    }

    @Test
    fun `get response de duplicates duplicate caseload crns`() {
        val staff = StaffGenerator.DEFAULT
        persistCaseload(
            CaseloadGenerator.generate(
                person = personReference(PersonGenerator.DEFAULT.id),
                staff = staffReference(staff.id),
                team = teamReference(TeamGenerator.DEFAULT.id)
            )
        )

        mockMvc.get("/staff/${staff.code}/active-cases") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.cases.length()") { value(1) }
                jsonPath("$.cases[0].crn") { value(PersonGenerator.DEFAULT.crn) }
            }
    }

    @Test
    fun `post response maps license case type`() {
        val staff = StaffGenerator.DEFAULT
        val person = persistSentenceCase(
            crn = "L123456",
            sentenceType = "SC",
            sentenceDescription = "Suspended Sentence"
        )

        mockMvc.post("/staff/${staff.code}/active-cases") {
            withToken()
            json = listOf(person.crn)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.cases[0].crn") { value(person.crn) }
                jsonPath("$.cases[0].type") { value("LICENSE") }
                jsonPath("$.cases[0].initialAllocationDate") { doesNotExist() }
            }
    }

    @Test
    fun `post response maps community case type`() {
        val staff = StaffGenerator.DEFAULT
        val person = persistSentenceCase(
            crn = "C123457",
            sentenceType = "SP",
            sentenceDescription = "Community Sentence"
        )

        mockMvc.post("/staff/${staff.code}/active-cases") {
            withToken()
            json = listOf(person.crn)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.cases[0].crn") { value(person.crn) }
                jsonPath("$.cases[0].type") { value("COMMUNITY") }
                jsonPath("$.cases[0].initialAllocationDate") { doesNotExist() }
            }
    }

    @Test
    fun `post response maps unknown case type`() {
        val staff = StaffGenerator.DEFAULT
        val person = persistSentenceCase(
            crn = "U123458",
            sentenceType = "ZZ",
            sentenceDescription = "Other Sentence"
        )

        mockMvc.post("/staff/${staff.code}/active-cases") {
            withToken()
            json = listOf(person.crn)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.cases[0].crn") { value(person.crn) }
                jsonPath("$.cases[0].type") { value("UNKNOWN") }
                jsonPath("$.cases[0].initialAllocationDate") { doesNotExist() }
            }
    }

    @Test
    fun `staff not found`() {
        mockMvc.get("/staff/UNKNOWN/active-cases") {
            withToken()
        }
            .andExpect {
                status { isNotFound() }
            }
    }

    private fun persistSentenceCase(crn: String, sentenceType: String, sentenceDescription: String): Person {
        val person = PersonGenerator.generate(crn)
        val event = EventGenerator.generate(person = person, eventNumber = IdGenerator.id().toString())
        val disposalType = DisposalType(IdGenerator.id(), sentenceType, sentenceDescription)
        val disposal = DisposalGenerator.generate(event = event, type = disposalType)

        transactionTemplate.executeWithoutResult {
            entityManager.persist(person)
            entityManager.persist(event)
            entityManager.persist(disposalType)
            entityManager.persist(disposal)
            entityManager.flush()
        }

        return person
    }

    private fun persistCaseload(caseload: Caseload) {
        transactionTemplate.executeWithoutResult {
            entityManager.persist(caseload)
            entityManager.flush()
        }
    }

    private fun personReference(id: Long) = entityManager.getReference(Person::class.java, id)

    private fun staffReference(id: Long) = entityManager.getReference(Staff::class.java, id)

    private fun teamReference(id: Long) = entityManager.getReference(Team::class.java, id)
}

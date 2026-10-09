package uk.gov.justice.digital.hmpps

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
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
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TeamActiveCasesTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val entityManager: EntityManager,
    transactionManager: PlatformTransactionManager,
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    @Test
    fun `successful get response`() {
        val team = TeamGenerator.ALLOCATION_TEAM
        val staffOne = StaffGenerator.STAFF_WITH_USER
        val staffTwo = StaffGenerator.SPO_STAFF
        val personOne = PersonGenerator.DEFAULT
        val personTwo = persistSentenceCase()

        persistCaseload(
            CaseloadGenerator.generate(
                person = personReference(personOne.id),
                staff = staffReference(staffOne.id),
                team = teamReference(team.id)
            )
        )
        persistCaseload(
            CaseloadGenerator.generate(
                person = personReference(personTwo.id),
                staff = staffReference(staffTwo.id),
                team = teamReference(team.id)
            )
        )

        mockMvc.get("/team/${team.code}/active-cases") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(team.code) }
                jsonPath("$.description") { value(team.description) }
                jsonPath("$.staff.length()") { value(2) }
                jsonPath("$.staff[0].code") { value(staffOne.code) }
                jsonPath("$.staff[0].cases.length()") { value(1) }
                jsonPath("$.staff[0].cases[0].crn") { value(personOne.crn) }
                jsonPath("$.staff[0].cases[0].name.forename") { value(personOne.forename) }
                jsonPath("$.staff[0].cases[0].type") { value("CUSTODY") }
                jsonPath("$.staff[0].cases[0].initialAllocationDate") { value("2022-06-24") }
                jsonPath("$.staff[1].code") { value(staffTwo.code) }
                jsonPath("$.staff[1].cases.length()") { value(1) }
                jsonPath("$.staff[1].cases[0].crn") { value(personTwo.crn) }
                jsonPath("$.staff[1].cases[0].type") { value("LICENSE") }
                jsonPath("$.staff[1].cases[0].initialAllocationDate") { doesNotExist() }
            }
    }

    private fun persistSentenceCase(): Person {
        val person = PersonGenerator.generate("L123456")
        val event = EventGenerator.generate(person = person, eventNumber = IdGenerator.id().toString())
        val disposalType = DisposalType(IdGenerator.id(), "SC", "Suspended Sentence")
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



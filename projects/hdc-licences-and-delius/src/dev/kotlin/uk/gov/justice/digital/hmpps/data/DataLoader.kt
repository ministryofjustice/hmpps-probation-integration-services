package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.Borough
import uk.gov.justice.digital.hmpps.entity.CommunityManagerEntity
import uk.gov.justice.digital.hmpps.entity.District
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ProbationArea
import uk.gov.justice.digital.hmpps.entity.StaffEntity
import uk.gov.justice.digital.hmpps.entity.Team
import uk.gov.justice.digital.hmpps.entity.User
import uk.gov.justice.digital.hmpps.set
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        val probationArea = ProbationArea(id = id(), code = "TST", description = "Test")
        val borough = Borough(id = id(), code = "PDU", description = "Probation Delivery Unit", probationArea = probationArea)
            .also { probationArea.set(ProbationArea::boroughs, setOf(it)) }
        val district = District(id = id(), code = "LAU", description = "Local Admin Unit", borough = borough)
            .also { borough.set(Borough::districts, setOf(it)) }
        val team1 = Team(id = id(), code = "TEAM01", description = "Team 1", district = district, probationArea = probationArea)
        val team2 = Team(id = id(), code = "TEAM02", description = "Team 2", district = district, probationArea = probationArea)
        val staff = StaffEntity(id = id(), code = "STAFF01", forename = "Test", surname = "Staff", teams = listOf(team1, team2))
        val user = User(id = id(), username = "test.user", staff = staff)
            .also { staff.set(StaffEntity::user, it) }
        val person = Person(id = id(), nomsNumber = "PERSON1")
        val previousManager = CommunityManagerEntity(id = id(), person = person, staff = staff, team = team1, active = false)
        val currentManager = CommunityManagerEntity(id = id(), person = person, staff = staff, team = team2, active = true)
            .also { staff.set(StaffEntity::communityManagers, setOf(it)) }
            .also { person.set(Person::communityManagers, listOf(it)) }

        listOf(probationArea, borough, district, team1, team2, staff, user, person, previousManager, currentManager)
            .forEach(entityManager::persist)
    }

    private fun id() = IdGenerator.getAndIncrement()
}

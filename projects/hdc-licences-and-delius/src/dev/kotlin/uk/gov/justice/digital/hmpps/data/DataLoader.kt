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
import uk.gov.justice.digital.hmpps.entity.*
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
        val borough =
            Borough(id = id(), code = "PDU", description = "Probation Delivery Unit", probationArea = probationArea)
                .also { probationArea.set(ProbationArea::boroughs, setOf(it)) }
        val district = District(id = id(), code = "LAU", description = "Local Admin Unit", borough = borough)
            .also { borough.set(Borough::districts, setOf(it)) }
        val team1 =
            Team(id = id(), code = "TEAM01", description = "Team 1", district = district, probationArea = probationArea)
        val team2 =
            Team(id = id(), code = "TEAM02", description = "Team 2", district = district, probationArea = probationArea)
        val staff =
            StaffEntity(id = id(), code = "STAFF01", forename = "Test", surname = "Staff", teams = listOf(team1, team2))
        val staff1 =
            StaffEntity(
                id = id(),
                code = "STAFF0U",
                forename = "Test1",
                forename2 = "Forename1",
                surname = "Staff1",
                teams = listOf(team1)
            )
        val user = User(id = id(), username = "test.user", staff = staff)
            .also { staff.set(StaffEntity::user, it) }
        val person1 = Person(id = id(), crn = "X000001", nomsNumber = "PERSON1")
        val person2 = Person(id = id(), crn = "X000002", nomsNumber = "PERSON2")
        val person3 = Person(id = id(), crn = "X000003", nomsNumber = "PERSON3", softDeleted = true)
        val previousManager =
            CommunityManagerEntity(
                id = id(),
                person = person1,
                staff = staff,
                team = team1,
                probationArea = probationArea,
                active = false
            )
        val currentManager =
            CommunityManagerEntity(
                id = id(),
                person = person1,
                staff = staff,
                team = team2,
                probationArea = probationArea,
                active = true
            )
                .also { staff.set(StaffEntity::communityManagers, setOf(it)) }
                .also { person1.set(Person::communityManagers, listOf(it)) }
        val communityManager1 =
            CommunityManagerEntity(
                id = id(),
                person = person2,
                staff = staff1,
                team = team1,
                probationArea = probationArea
            )
        val communityManager2 = CommunityManagerEntity(
            id = id(),
            person = person2,
            team = team1,
            staff = staff,
            probationArea = probationArea,
            active = false
        )
        val communityManager3 =
            CommunityManagerEntity(
                id = id(),
                person = person3,
                staff = staff1,
                team = team1,
                probationArea = probationArea
            )

        listOf(
            probationArea,
            borough,
            district,
            team1,
            team2,
            staff,
            staff1,
            user,
            person1,
            person2,
            person3,
            previousManager,
            currentManager,
            communityManager1,
            communityManager2,
            communityManager3
        )
            .forEach(entityManager::persist)
    }

    private fun id() = IdGenerator.getAndIncrement()
}

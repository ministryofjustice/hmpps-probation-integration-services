package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactTypeRepository: ContactTypeRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        referenceDataRepository.save(ApprovedPremisesGenerator.DEFAULT.code)
        referenceDataRepository.save(ApprovedPremisesGenerator.NO_STAFF.code)
        referenceDataRepository.save(StaffGenerator.STAFF_GRADE)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.DEFAULT)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.NO_STAFF)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM_WITH_NO_STAFF)
        teamRepository.save(TeamGenerator.NON_APPROVED_PREMISES_TEAM)
        teamRepository.save(TeamGenerator.UNALLOCATED)
        staffRepository.save(StaffGenerator.generate("Key-worker", teams = listOf(TeamGenerator.APPROVED_PREMISES_TEAM), approvedPremises = listOf(ApprovedPremisesGenerator.DEFAULT)))
        staffRepository.save(StaffGenerator.generate("Not key-worker", teams = listOf(TeamGenerator.APPROVED_PREMISES_TEAM)))
        staffRepository.save(StaffGenerator.generate("Not key-worker and not in AP team", teams = listOf(TeamGenerator.NON_APPROVED_PREMISES_TEAM)))

        val personManagerStaff = StaffGenerator.generate(code = "N54A001")
        staffRepository.save(personManagerStaff)
        val person = PersonGenerator.generate(crn = "A000001")
        personRepository.save(person)
        personManagerRepository.save(PersonManagerGenerator.generate(person, staff = personManagerStaff, team = TeamGenerator.NON_APPROVED_PREMISES_TEAM))
        contactTypeRepository.saveAll(ContactTypeCode.values().map { ContactTypeGenerator.generate(it.code) })
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long>
interface ProbationAreaRepository : JpaRepository<ProbationArea, Long>

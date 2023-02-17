package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiStatusGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.TransferReasonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.Address
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val addressRepository: AddressRepository,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val caseloadRepository: CaseloadRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        datasetRepository.saveAll(DatasetGenerator.all())
        referenceDataRepository.saveAll(ReferenceDataGenerator.all())

        addressRepository.saveAll(listOf(AddressGenerator.Q001, AddressGenerator.Q002))

        approvedPremisesRepository.save(ApprovedPremisesGenerator.DEFAULT)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.NO_STAFF)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM)
        teamRepository.save(TeamGenerator.APPROVED_PREMISES_TEAM_WITH_NO_STAFF)
        teamRepository.save(TeamGenerator.NON_APPROVED_PREMISES_TEAM)
        teamRepository.save(TeamGenerator.UNALLOCATED)
        staffRepository.save(
            StaffGenerator.generate(
                "Key-worker",
                "KEY0001",
                teams = listOf(TeamGenerator.APPROVED_PREMISES_TEAM),
                approvedPremises = listOf(ApprovedPremisesGenerator.DEFAULT)
            )
        )
        staffRepository.save(
            StaffGenerator.generate(
                "Not key-worker",
                "KEY0002",
                teams = listOf(TeamGenerator.APPROVED_PREMISES_TEAM)
            )
        )
        staffRepository.save(
            StaffGenerator.generate(
                "Not key-worker and not in AP team",
                "KEY0003",
                teams = listOf(TeamGenerator.NON_APPROVED_PREMISES_TEAM)
            )
        )

        val personManagerStaff = StaffGenerator.generate(code = "N54A001")
        staffRepository.save(personManagerStaff)
        val person = PersonGenerator.DEFAULT
        personRepository.save(person)
        personManagerRepository.save(
            PersonManagerGenerator.generate(
                person,
                staff = personManagerStaff,
                team = TeamGenerator.NON_APPROVED_PREMISES_TEAM
            )
        )
        personAddressRepository.save(AddressGenerator.PERSON_ADDRESS)
        contactTypeRepository.saveAll(ContactTypeCode.values().map { ContactTypeGenerator.generate(it.code) })
        nsiTypeRepository.saveAll(NsiTypeCode.values().map { NsiTypeGenerator.generate(it.code) })
        nsiStatusRepository.saveAll(NsiStatusCode.values().map { NsiStatusGenerator.generate(it.code) })
        transferReasonRepository.save(TransferReasonGenerator.NSI)

        caseloadRepository.save(CaseloadGenerator.generate(person, TeamGenerator.NON_APPROVED_PREMISES_TEAM))
        caseloadRepository.save(CaseloadGenerator.generate(person, TeamGenerator.APPROVED_PREMISES_TEAM))
        caseloadRepository.save(CaseloadGenerator.generate(person, TeamGenerator.UNALLOCATED))
    }
}

interface AddressRepository : JpaRepository<Address, Long>
interface DatasetRepository : JpaRepository<Dataset, Long>

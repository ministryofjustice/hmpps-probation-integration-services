package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ANOTHER_EVENT
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.Address
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.MoveOnCategoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralSourceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.location.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.BoroughRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApGroupLink
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApGroupLinkId
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val referralSourceRepository: ReferralSourceRepository,
    private val moveOnCategoryRepository: MoveOnCategoryRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val addressRepository: AddressRepository,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val apGroupLinkRepository: ApGroupLinkRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val caseloadRepository: CaseloadRepository,
    private val registrationRepository: RegistrationRepository,
    private val probationCaseDataLoader: ProbationCaseDataLoader,
    private val lduRepository: LduRepository,
    private val staffUserRepository: StaffUserRepository,
    private val boroughRepository: BoroughRepository,
    private val referralBookingDataLoader: ReferralBookingDataLoader,
    private val documentDataLoader: DocumentDataLoader,
    private val entityManagerDataLoader: EntityManagerDataLoader
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        datasetRepository.saveAll(DatasetGenerator.all())
        referenceDataRepository.saveAll(ReferenceDataGenerator.all())
        referralSourceRepository.save(ReferenceDataGenerator.OTHER_REFERRAL_SOURCE)
        moveOnCategoryRepository.save(ReferenceDataGenerator.MC05)
        registerTypeRepository.saveAll(ReferenceDataGenerator.REGISTER_TYPES.values)
        referenceDataRepository.save(ReferenceDataGenerator.NSI_INITIAL_ALLOCATION)

        addressRepository.saveAll(AddressGenerator.ALL_ADDRESSES)
        boroughRepository.save(ProbationCaseGenerator.BOROUGH)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        probationAreaRepository.save(ProbationAreaGenerator.N58_SW)
        approvedPremisesRepository.saveAll(ApprovedPremisesGenerator.ALL_APS)
        // add a duplicate AP for testing selectable query
        approvedPremisesRepository.save(
            ApprovedPremisesGenerator.generate(
                ApprovedPremisesGenerator.DEFAULT.code,
                ApprovedPremisesGenerator.DEFAULT.address,
                selectable = false
            )
        )
        officeLocationRepository.save(OfficeLocationGenerator.DEFAULT)
        apGroupLinkRepository.saveAll(ApprovedPremisesGenerator.AP_GROUP_LINKS)

        lduRepository.save(TeamGenerator.AP_TEAM_LDU)
        teamRepository.saveAll(TeamGenerator.ALL_TEAMS)

        (1..3).forEach {
            staffRepository.save(
                StaffGenerator.generate(
                    "Key-worker $it",
                    "KEY000$it",
                    teams = TeamGenerator.ALL_AP_TEAMS,
                    approvedPremises = ApprovedPremisesGenerator.ALL_STAFFED_APS
                )
            )
        }

        staffRepository.save(
            StaffGenerator.generate(
                "Not key-worker",
                "NOTKEY1",
                teams = TeamGenerator.ALL_AP_TEAMS,
                approvedPremises = emptyList(),
            )
        )

        staffRepository.save(
            StaffGenerator.generate(
                "Unallocated",
                TeamGenerator.APPROVED_PREMISES_TEAM.code + "U",
                teams = TeamGenerator.ALL_AP_TEAMS
            )
        )

        staffRepository.save(StaffGenerator.DEFAULT_STAFF)
        staffUserRepository.save(StaffGenerator.DEFAULT_STAFF_USER)

        staffRepository.save(StaffGenerator.JIM_SNOW)
        staffUserRepository.save(StaffGenerator.JIM_SNOW_USER)

        staffRepository.save(StaffGenerator.LAO_FULL_ACCESS)
        staffUserRepository.save(StaffGenerator.LAO_FULL_ACCESS_USER)

        staffRepository.save(StaffGenerator.CAS2V2_CB)
        staffUserRepository.save(StaffGenerator.CAS2V2_CB_USER)

        staffRepository.save(StaffGenerator.CAS2V2_PB)
        staffUserRepository.save(StaffGenerator.CAS2V2_PB_USER)

        staffRepository.save(StaffGenerator.LAO_RESTRICTED)
        staffUserRepository.save(StaffGenerator.LAO_RESTRICTED_USER)

        staffRepository.save(StaffGenerator.CRU_WOMENS_ESTATE)
        staffUserRepository.save(StaffGenerator.CRU_WOMENS_ESTATE_USER)

        staffRepository.save(StaffGenerator.STAFF_WITHOUT_USERNAME)

        val personManagerStaff = StaffGenerator.generate(code = "N54A001")
        staffRepository.save(personManagerStaff)
        val person = PersonGenerator.DEFAULT
        personRepository.save(person)
        personRepository.save(PersonGenerator.PERSON_INACTIVE_EVENT)
        personManagerRepository.save(
            PersonManagerGenerator.generate(
                person,
                staff = personManagerStaff,
                team = TeamGenerator.NON_APPROVED_PREMISES_TEAM
            )
        )

        personManagerRepository.save(
            PersonManagerGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                staff = personManagerStaff,
                team = TeamGenerator.NON_APPROVED_PREMISES_TEAM
            )
        )

        entityManagerDataLoader.loadData()
        eventRepository.save(PersonGenerator.EVENT)
        eventRepository.save(PersonGenerator.INACTIVE_EVENT)
        registrationRepository.save(
            PersonGenerator.generateRegistration(
                person,
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.GANG_AFFILIATION.value]!!,
                LocalDate.now()
            )
        )

        val contactTypes = contactTypeRepository.saveAll(
            ContactTypeCode.entries.map { ContactTypeGenerator.generate(it.code) }
        ).associateBy { it.code }
        contactOutcomeRepository.saveAll(
            listOf(
                ContactOutcomeGenerator.generate("AP_N"),
                ContactOutcomeGenerator.generate("AP-D")
            )
        )
        nsiTypeRepository.saveAll(NsiTypeCode.entries.map { NsiTypeGenerator.generate(it.code) })
        val linkedContact = contactTypeRepository.save(ContactTypeGenerator.generate("SMLI001"))
        nsiStatusRepository.saveAll(NsiStatusCode.entries.map {
            NsiStatusGenerator.generate(
                it.code,
                when (it.code) {
                    NsiStatusCode.ACTIVE.code -> linkedContact
                    NsiStatusCode.AP_CASE_ALLOCATED.code -> contactTypes[ContactTypeCode.CASE_ALLOCATED.code]
                    else -> null
                }
            )
        })
        transferReasonRepository.save(TransferReasonGenerator.NSI)

        caseloadRepository.save(CaseloadGenerator.generate(person, TeamGenerator.NON_APPROVED_PREMISES_TEAM))
        caseloadRepository.save(CaseloadGenerator.generate(person, TeamGenerator.APPROVED_PREMISES_TEAM))
        caseloadRepository.save(CaseloadGenerator.generate(person, TeamGenerator.UNALLOCATED))

        caseloadRepository.save(
            CaseloadGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                TeamGenerator.NON_APPROVED_PREMISES_TEAM
            )
        )
        caseloadRepository.save(
            CaseloadGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                TeamGenerator.APPROVED_PREMISES_TEAM
            )
        )
        caseloadRepository.save(
            CaseloadGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                TeamGenerator.UNALLOCATED
            )
        )

        eventRepository.save(ANOTHER_EVENT)

        probationCaseDataLoader.loadData()
        referralBookingDataLoader.loadData()
        documentDataLoader.loadData()
    }
}

interface DatasetRepository : JpaRepository<Dataset, Long>
interface ProbationAreaRepository : JpaRepository<ProbationArea, Long>
interface ApGroupLinkRepository : JpaRepository<ApGroupLink, ApGroupLinkId>
interface AddressRepository : JpaRepository<Address, Long>
interface RegisterTypeRepository : JpaRepository<RegisterType, Long>
interface StaffUserRepository : JpaRepository<StaffUser, Long>

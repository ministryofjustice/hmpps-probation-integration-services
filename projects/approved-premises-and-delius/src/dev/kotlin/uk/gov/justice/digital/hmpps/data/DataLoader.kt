package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ANOTHER_EVENT
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import java.time.LocalDate

@Component
class DataLoader(
    dataManager: DataManager,
    private val probationCaseDataLoader: ProbationCaseDataLoader,
    private val referralBookingDataLoader: ReferralBookingDataLoader,
    private val documentDataLoader: DocumentDataLoader
) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(DatasetGenerator.all())
        saveAll(ReferenceDataGenerator.all())
        save(ReferenceDataGenerator.OTHER_REFERRAL_SOURCE)
        save(ReferenceDataGenerator.MC05)
        saveAll(ReferenceDataGenerator.REGISTER_TYPES.values)
        save(ReferenceDataGenerator.NSI_INITIAL_ALLOCATION)

        saveAll(AddressGenerator.ALL_ADDRESSES)
        save(ProbationCaseGenerator.BOROUGH)
        save(ProbationAreaGenerator.DEFAULT)
        save(ProbationAreaGenerator.N58_SW)
        saveAll(ApprovedPremisesGenerator.ALL_APS)
        // add a duplicate AP for testing selectable query
        save(
            ApprovedPremisesGenerator.generate(
                ApprovedPremisesGenerator.DEFAULT.code,
                ApprovedPremisesGenerator.DEFAULT.address,
                selectable = false
            )
        )
        save(OfficeLocationGenerator.DEFAULT)
        saveAll(ApprovedPremisesGenerator.AP_GROUP_LINKS)

        save(TeamGenerator.AP_TEAM_LDU)
        saveAll(TeamGenerator.ALL_TEAMS)

        (1..3).forEach {
            save(
                StaffGenerator.generate(
                    "Key-worker $it",
                    "KEY000$it",
                    teams = TeamGenerator.ALL_AP_TEAMS,
                    approvedPremises = ApprovedPremisesGenerator.ALL_STAFFED_APS
                )
            )
        }

        save(
            StaffGenerator.generate(
                "Not key-worker",
                "NOTKEY1",
                teams = TeamGenerator.ALL_AP_TEAMS,
                approvedPremises = emptyList(),
            )
        )

        save(
            StaffGenerator.generate(
                "Unallocated",
                TeamGenerator.APPROVED_PREMISES_TEAM.code + "U",
                teams = TeamGenerator.ALL_AP_TEAMS
            )
        )

        save(StaffGenerator.DEFAULT_STAFF)
        save(StaffGenerator.DEFAULT_STAFF_USER)

        save(StaffGenerator.JIM_SNOW)
        save(StaffGenerator.JIM_SNOW_USER)

        save(StaffGenerator.LAO_FULL_ACCESS)
        save(StaffGenerator.LAO_FULL_ACCESS_USER)

        save(StaffGenerator.CAS2V2_CB)
        save(StaffGenerator.CAS2V2_CB_USER)

        save(StaffGenerator.CAS2V2_PB)
        save(StaffGenerator.CAS2V2_PB_USER)

        save(StaffGenerator.LAO_RESTRICTED)
        save(StaffGenerator.LAO_RESTRICTED_USER)

        save(StaffGenerator.CRU_WOMENS_ESTATE)
        save(StaffGenerator.CRU_WOMENS_ESTATE_USER)

        save(StaffGenerator.STAFF_WITHOUT_USERNAME)

        val personManagerStaff = StaffGenerator.generate(code = "N54A001")
        save(personManagerStaff)
        val person = PersonGenerator.DEFAULT
        save(person)
        save(PersonGenerator.PERSON_INACTIVE_EVENT)
        save(
            PersonManagerGenerator.generate(
                person,
                staff = personManagerStaff,
                team = TeamGenerator.NON_APPROVED_PREMISES_TEAM
            )
        )

        save(
            PersonManagerGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                staff = personManagerStaff,
                team = TeamGenerator.NON_APPROVED_PREMISES_TEAM
            )
        )

        save(AddressGenerator.PERSON_ADDRESS)
        save(AddressGenerator.INACTIVE_PERSON_ADDRESS)
        save(ReferralGenerator.EXISTING_REFERRAL)
        save(ReferralGenerator.BOOKING_WITHOUT_ARRIVAL)
        save(ReferralGenerator.BOOKING_ARRIVED)
        save(ReferralGenerator.BOOKING_DEPARTED)

        save(PersonGenerator.EVENT)
        save(PersonGenerator.INACTIVE_EVENT)
        save(
            PersonGenerator.generateRegistration(
                person,
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.GANG_AFFILIATION.value]!!,
                LocalDate.now()
            )
        )

        val contactTypes = saveAll(
            ContactTypeCode.entries.map { ContactTypeGenerator.generate(it.code) }
        ).associateBy { it.code }
        saveAll(
            listOf(
                ContactOutcomeGenerator.generate("AP_N"),
                ContactOutcomeGenerator.generate("AP-D")
            )
        )
        saveAll(NsiTypeCode.entries.map { NsiTypeGenerator.generate(it.code) })
        val linkedContact = save(ContactTypeGenerator.generate("SMLI001"))
        saveAll(NsiStatusCode.entries.map {
            NsiStatusGenerator.generate(
                it.code,
                when (it.code) {
                    NsiStatusCode.ACTIVE.code -> linkedContact
                    NsiStatusCode.AP_CASE_ALLOCATED.code -> contactTypes[ContactTypeCode.CASE_ALLOCATED.code]
                    else -> null
                }
            )
        })
        save(TransferReasonGenerator.NSI)

        save(CaseloadGenerator.generate(person, TeamGenerator.NON_APPROVED_PREMISES_TEAM))
        save(CaseloadGenerator.generate(person, TeamGenerator.APPROVED_PREMISES_TEAM))
        save(CaseloadGenerator.generate(person, TeamGenerator.UNALLOCATED))

        save(
            CaseloadGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                TeamGenerator.NON_APPROVED_PREMISES_TEAM
            )
        )
        save(
            CaseloadGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                TeamGenerator.APPROVED_PREMISES_TEAM
            )
        )
        save(
            CaseloadGenerator.generate(
                PersonGenerator.PERSON_INACTIVE_EVENT,
                TeamGenerator.UNALLOCATED
            )
        )

        save(ANOTHER_EVENT)

        probationCaseDataLoader.loadData()
        referralBookingDataLoader.loadData()
        documentDataLoader.loadData()
    }
}

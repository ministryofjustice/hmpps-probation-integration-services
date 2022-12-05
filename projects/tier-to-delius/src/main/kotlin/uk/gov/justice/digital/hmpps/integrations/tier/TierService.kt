package uk.gov.justice.digital.hmpps.integrations.tier

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTier
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierId
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getByCodeAndSetName
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getByCode
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class TierService(
    private val personRepository: PersonRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val managementTierRepository: ManagementTierRepository,
    private val contactRepository: ContactRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val telemetryService: TelemetryService,
) {
    @Transactional
    fun updateTier(crn: String, tierCalculation: TierCalculation) {
        val person = personRepository.findByCrnAndSoftDeletedIsFalse(crn) ?: return let {
            telemetryService.trackEvent("PersonNotFound", tierCalculation.telemetryProperties(crn))
        }
        val tier = referenceDataRepository.getByCodeAndSetName("U${tierCalculation.tierScore}", "TIER")
        val changeReason = referenceDataRepository.getByCodeAndSetName("ATS", "TIER CHANGE REASON")
        val latestTierChangeDate = managementTierRepository.findFirstByIdPersonIdOrderByIdDateChangedDesc(person.id)?.id?.dateChanged

        if (person.currentTier == null || person.currentTier != tier.id) {
            createTier(person, tier, tierCalculation.calculationDate, changeReason)
            createContact(person, tier, tierCalculation.calculationDate, changeReason)
            // TODO: Update the MANAGEMENT_TIER.DATE_CHANGED column type from DATE to TIMESTAMP
            // Currently, the database only stores the dateChanged to date precision (no time), so this check will
            // always succeed for multiple tier updates in the same day. This could lead to an incorrect
            // OFFENDER.CURRENT_TIER value if messages are processed out of order on the same day. There's nothing we
            // can do here to remedy this without updating the Delius data model.
            if (latestTierChangeDate == null || tierCalculation.calculationDate > latestTierChangeDate) {
                updatePerson(person, tier)
            }
        } else {
            telemetryService.trackEvent("UnchangedTierIgnored", tierCalculation.telemetryProperties(crn))
        }
    }

    private fun createTier(
        person: Person,
        tier: ReferenceData,
        calculationDate: ZonedDateTime,
        changeReason: ReferenceData
    ) {
        managementTierRepository.save(
            ManagementTier(
                id = ManagementTierId(
                    personId = person.id,
                    tierId = tier.id,
                    dateChanged = calculationDate
                ),
                tierChangeReasonId = changeReason.id
            )
        )
    }

    private fun createContact(
        person: Person,
        tier: ReferenceData,
        calculationDate: ZonedDateTime,
        changeReason: ReferenceData
    ) {
        val areaCode = person.managers.firstOrNull()?.probationArea?.code
            ?: throw NotFoundException("PersonManager", "crn", person.crn)

        val formattedDate = DeliusDateTimeFormatter.format(calculationDate)
        contactRepository.save(
            Contact(
                date = calculationDate,
                person = person,
                startTime = calculationDate,
                notes = """
                        Tier Change Date: $formattedDate
                        Tier: ${tier.description}
                        Tier Change Reason: ${changeReason.description}
                """.trimIndent(),
                staffId = staffRepository.getByCode("${areaCode}UTSO").id,
                teamId = teamRepository.getByCode("${areaCode}UTS").id,
                type = contactTypeRepository.getByCode(ContactTypeCode.TIER_UPDATE.code)
            )
        )
    }

    private fun updatePerson(
        person: Person,
        tier: ReferenceData
    ) {
        person.currentTier = tier.id
        personRepository.save(person)
    }
}

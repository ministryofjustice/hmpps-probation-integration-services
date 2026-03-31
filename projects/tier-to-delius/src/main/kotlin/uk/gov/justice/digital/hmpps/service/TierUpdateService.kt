package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException.Companion.orIgnore
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
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
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import java.time.ZonedDateTime

@Service
class TierUpdateService(
    private val personRepository: PersonRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val managementTierRepository: ManagementTierRepository,
    private val contactRepository: ContactRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val optimisationTables: OptimisationTables,
    private val featureFlags: FeatureFlags,
) {
    @Transactional
    fun updateTier(crn: String, tierCalculation: TierCalculation) {
        val person = personRepository.findByCrnAndSoftDeletedIsFalse(crn).orIgnore { "PersonNotFound" }
        optimisationTables.rebuild(person.id)
        val tierCodePrefix = if (featureFlags.enabled("tier-to-delius-v3")) "SP" else "U"
        val tier = referenceDataRepository.getByCodeAndSetName("$tierCodePrefix${tierCalculation.tierScore}", "TIER")
        val changeReason = referenceDataRepository.getByCodeAndSetName("ATS", "TIER CHANGE REASON")
        val latestTier = managementTierRepository.findByIdPersonIdAndEndDateIsNull(person.id)

        if (person.currentTier == tier.id) throw IgnorableMessageException("UnchangedTierIgnored")
        if (latestTier != null && !latestTier.id.dateChanged.isBefore(tierCalculation.calculationDate)) {
            throw IgnorableMessageException("OutOfOrderMessageIgnored")
        }

        latestTier?.also {
            it.endDate = tierCalculation.calculationDate
            managementTierRepository.saveAndFlush(it)
        }
        createTier(person, tier, tierCalculation.calculationDate, changeReason)
        createContact(person, tier, tierCalculation.calculationDate, changeReason)
        updatePerson(person, tier)
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
                    dateChanged = calculationDate,
                ),
                tierChangeReasonId = changeReason.id,
                endDate = null
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
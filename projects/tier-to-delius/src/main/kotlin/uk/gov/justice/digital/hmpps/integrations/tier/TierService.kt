package uk.gov.justice.digital.hmpps.integrations.tier

import org.springframework.stereotype.Service
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
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedIsFalse
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getByCodeAndSetName
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getByCode
import java.time.format.DateTimeFormatter

@Service
class TierService(
    private val tierClient: TierClient,
    private val personRepository: PersonRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val managementTierRepository: ManagementTierRepository,
    private val contactRepository: ContactRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val contactTypeRepository: ContactTypeRepository,
) {
    fun updateTier(crn: String, calculationId: String) {
        writeTierUpdate(crn, calculationId)
    }

    private fun writeTierUpdate(crn: String, calculationId: String) {
        val tierCalculation = tierClient.getTierCalculation(crn, calculationId)
        val tier = referenceDataRepository.getByCodeAndSetName(tierCalculation.tierScore, "TIER")
        val person = personRepository.getByCrnAndSoftDeletedIsFalse(crn)
        val changeReason = referenceDataRepository.getByCodeAndSetName("ATS", "TIER CHANGE REASON")
        managementTierRepository.save(
            ManagementTier(
                id = ManagementTierId(
                    personId = person.id,
                    tierId = tier.id,
                    dateChanged = tierCalculation.calculationDate
                ),
                tierChangeReasonId = changeReason.id
            )
        )

        writeContact(person, tierCalculation, tier, changeReason)
    }

    private fun writeContact(
        person: Person,
        tierCalculation: TierCalculation,
        tier: ReferenceData,
        changeReason: ReferenceData
    ) {
        val areaCode = person.managers.firstOrNull()?.probationArea?.code
            ?: throw NotFoundException("PersonManager", "crn", person.crn)

        val formattedDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(tierCalculation.calculationDate)
        contactRepository.save(
            Contact(
                date = tierCalculation.calculationDate,
                person = person,
                startTime = tierCalculation.calculationDate,
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
}

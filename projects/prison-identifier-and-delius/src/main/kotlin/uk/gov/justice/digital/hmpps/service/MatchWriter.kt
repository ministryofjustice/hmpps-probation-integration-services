package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.model.PrisonIdentifiers

@Service
class MatchWriter(
    private val personRepository: PersonRepository,
    private val custodyRepository: CustodyRepository,
    private val additionalIdentifierRepository: AdditionalIdentifierRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val prisonerRepository: PrisonerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val orderManagerRepository: OrderManagerRepository,
) {
    @Transactional
    fun update(prisonIdentifiers: PrisonIdentifiers, person: Person, custody: Custody? = null): Boolean {
        val nomsNumberChanged = person.nomsNumber != prisonIdentifiers.prisonerNumber
        if (nomsNumberChanged) {
            removeDuplicateNomsNumbers(person, prisonIdentifiers.prisonerNumber)
            updateNomsNumber(person, prisonIdentifiers.prisonerNumber)
        }
        val bookingNumberChanged = custody != null && custody.prisonerNumber != prisonIdentifiers.bookingNumber
        if (bookingNumberChanged) {
            custody!!.prisonerNumber = prisonIdentifiers.bookingNumber
            custodyRepository.save(custody)
            person.mostRecentPrisonerNumber = prisonIdentifiers.bookingNumber
            personRepository.save(person)
            person.rebuildPrisonerLinks()
            custody.createContactForChange()
        }
        return nomsNumberChanged || bookingNumberChanged
    }

    private fun removeDuplicateNomsNumbers(person: Person, nomsNumber: String) {
        personRepository.findAllByNomsNumberAndIdNot(nomsNumber, person.id).forEach { duplicate ->
            duplicate.identifyDuplicateNomsNumber(nomsNumber)
            duplicate.nomsNumber = null
            personRepository.save(duplicate)
        }
    }

    private fun updateNomsNumber(person: Person, nomsNumber: String) {
        person.identifyFormerNomsNumber(person.nomsNumber)
        person.nomsNumber = nomsNumber
        personRepository.save(person)
    }

    private fun Person.identifyDuplicateNomsNumber(nomsNumber: String) {
        additionalIdentifierRepository.save(
            AdditionalIdentifier(
                personId = this.id,
                identifier = nomsNumber,
                type = referenceDataRepository.duplicateNomsNumberIdentifierType(),
            )
        )
    }

    private fun Person.identifyFormerNomsNumber(nomsNumber: String?) {
        nomsNumber?.let {
            additionalIdentifierRepository.save(
                AdditionalIdentifier(
                    personId = this.id,
                    identifier = nomsNumber,
                    type = referenceDataRepository.formerNomsNumberIdentifierType(),
                )
            )
        }
    }

    private fun Person.rebuildPrisonerLinks() {
        prisonerRepository.deleteAllByIdPersonId(id)
        events.mapNotNull { it.disposal?.custody?.prisonerNumber }
            .map { prisonerNumber -> Prisoner(PrisonerId(id, prisonerNumber)) }
            .let(prisonerRepository::saveAll)
    }

    private fun Custody.createContactForChange() {
        val orderManager = orderManagerRepository.getByEventId(disposal.event.id)
        contactRepository.save(
            Contact(
                personId = disposal.event.person.id,
                eventId = disposal.event.id,
                type = contactTypeRepository.getByCode(ContactType.CUSTODY_UPDATE),
                notes = "Prison Number: $prisonerNumber" + System.lineSeparator(),
                providerId = orderManager.providerId,
                teamId = orderManager.teamId,
                staffId = orderManager.staffId
            )
        )
    }
}
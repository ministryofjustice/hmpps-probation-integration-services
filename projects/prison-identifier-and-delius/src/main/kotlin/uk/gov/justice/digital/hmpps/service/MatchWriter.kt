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
) {
    @Transactional
    fun update(prisonIdentifiers: PrisonIdentifiers, person: Person, custody: Custody? = null) {
        if (person.nomsNumber != prisonIdentifiers.prisonerNumber) {
            removeDuplicateNomsNumbers(person, prisonIdentifiers.prisonerNumber)
            updateNomsNumber(person, prisonIdentifiers.prisonerNumber)
        }
        if (custody != null && custody.prisonerNumber != prisonIdentifiers.prisonerNumber) {
            custody.prisonerNumber = prisonIdentifiers.bookingNumber
            custodyRepository.save(custody)
            person.mostRecentPrisonerNumber = prisonIdentifiers.bookingNumber
            personRepository.save(person)
            person.rebuildPrisonerLinks()
        }
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
}
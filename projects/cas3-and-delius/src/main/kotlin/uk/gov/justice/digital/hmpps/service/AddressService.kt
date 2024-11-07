package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate

@Service
class AddressService(
    private val personAddressRepository: PersonAddressRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personRepository: PersonRepository
) {
    fun updateMainAddress(person: Person, details: PersonArrived): Pair<PersonAddress?, PersonAddress> {
        val previous = endMainAddress(person, details.arrivedAt.toLocalDate())
        val current = toPersonAddress(person, details).apply(personAddressRepository::save)
        return previous to current
    }

    fun updateCas3Address(person: Person, details: PersonArrived): PersonAddress? {
        personRepository.findForUpdate(person.id)
        val currentMain = personAddressRepository.findMainAddress(person.id)
        return currentMain
            ?.takeIf { it.type.code == AddressTypeCode.CAS3.code }
            ?.apply {
                val addressLines = details.premises.addressLines
                buildingName = addressLines.buildingName?.trim()
                streetName = addressLines.streetName.trim()
                district = addressLines.district?.trim()
                town = details.premises.town?.trim()
                county = details.premises.region.trim()
                postcode = details.premises.postcode.trim()
                startDate = details.arrivedAt.toLocalDate()
            }
    }

    fun endMainAddress(person: Person, endDate: LocalDate): PersonAddress? {
        personRepository.findForUpdate(person.id)
        val currentMain = personAddressRepository.findMainAddress(person.id)
        return currentMain?.also {
            val previousStatus = referenceDataRepository.previousAddressStatus()
            currentMain.status = previousStatus
            currentMain.endDate = maxOf(endDate, currentMain.startDate)
        }
    }

    fun endMainCAS3Address(person: Person, endDate: LocalDate): PersonAddress? {
        personRepository.findForUpdate(person.id)
        val currentMain = personAddressRepository.findMainAddress(person.id)
        return currentMain?.also {
            if (currentMain.type.code == AddressTypeCode.CAS3.code && currentMain.startDate <= endDate) {
                val previousStatus = referenceDataRepository.previousAddressStatus()
                currentMain.status = previousStatus
                currentMain.endDate = endDate
            }
        }
    }

    private fun toPersonAddress(person: Person, details: PersonArrived): PersonAddress {
        val addressLines = details.premises.addressLines
        return PersonAddress(
            0,
            person.id,
            referenceDataRepository.cas3AddressType(),
            referenceDataRepository.mainAddressStatus(),
            buildingName = addressLines.buildingName?.trim(),
            streetName = addressLines.streetName.trim(),
            district = addressLines.district?.trim(),
            town = details.premises.town?.trim(),
            county = details.premises.region.trim(),
            postcode = details.premises.postcode.trim(),
            startDate = details.arrivedAt.toLocalDate()
        )
    }
}

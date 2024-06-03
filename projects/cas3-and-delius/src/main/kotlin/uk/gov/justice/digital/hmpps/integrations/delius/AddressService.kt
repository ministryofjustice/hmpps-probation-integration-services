package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
@Transactional
class AddressService(
    private val personAddressRepository: PersonAddressRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personRepository: PersonRepository
) {
    fun updateMainAddress(person: Person, details: PersonArrived) {
        endMainAddress(person, details.arrivedAt.toLocalDate())
        toPersonAddress(person, details).apply(personAddressRepository::save)
    }

    fun updateCas3Address(person: Person, details: PersonArrived) {
        val personForUpdate = personRepository.getByIdForUpdate(person.id)
        val currentMain = personAddressRepository.findMainAddress(personForUpdate.id)
        if (currentMain?.type?.code == AddressTypeCode.CAS3.code) {
            val addressLines = details.premises.addressLines
            currentMain.apply {
                buildingName = addressLines.buildingName?.trim()
                streetName = addressLines.streetName.trim()
                district = addressLines.district?.trim()
                town = details.premises.town?.trim()
                county = details.premises.region.trim()
                postcode = details.premises.postcode.trim()
                startDate = details.arrivedAt.toLocalDate()
            }
        }
    }

    fun endMainAddress(person: Person, endDate: LocalDate) {
        val personForUpdate = personRepository.getByIdForUpdate(person.id)
        val currentMain = personAddressRepository.findMainAddress(personForUpdate.id)

        if (currentMain?.startDate?.isAfter(endDate) == true) {
            throw IllegalArgumentException("Cannot end address. The address start date is after the new end date ")
        }
        currentMain?.apply {
            val previousStatus = referenceDataRepository.previousAddressStatus()
            currentMain.status = previousStatus
            currentMain.endDate = maxOf(endDate, currentMain.startDate)
        }
    }

    fun endMainCAS3Address(person: Person, endDate: ZonedDateTime) {
        val personForUpdate = personRepository.getByIdForUpdate(person.id)
        val currentMain = personAddressRepository.findMainAddress(personForUpdate.id)

        if (currentMain?.startDate?.isAfter(endDate.toLocalDate()) == true) {
            throw IllegalArgumentException("Cannot end address. The address start date is after the new end date ")
        }

        currentMain?.apply {
            if (currentMain.type.code == AddressTypeCode.CAS3.code) {
                val previousStatus = referenceDataRepository.previousAddressStatus()
                currentMain.status = previousStatus
                currentMain.endDate = endDate.toLocalDate()
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

package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.approvedPremisesAddressType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.mainAddressStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.previousAddressStatus
import java.time.LocalDate

@Service
class AddressService(
    private val personAddressRepository: PersonAddressRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun updateMainAddress(
        person: Person,
        details: PersonArrived,
        ap: ApprovedPremises
    ): Pair<PersonAddress?, PersonAddress> {
        val previous = endMainAddress(person, details.arrivedAt.toLocalDate())
        val current = ap.arrival(person, details).let(personAddressRepository::save)
        return previous to current
    }

    fun endMainAddress(person: Person, endDate: LocalDate): PersonAddress? {
        val currentMain = personAddressRepository.findMainAddress(person.id)
        return currentMain?.also {
            val previousStatus = referenceDataRepository.previousAddressStatus()
            currentMain.status = previousStatus
            currentMain.endDate = endDate
        }
    }

    private fun ApprovedPremises.arrival(person: Person, details: PersonArrived) = PersonAddress(
        0,
        person.id,
        referenceDataRepository.approvedPremisesAddressType(),
        referenceDataRepository.mainAddressStatus(),
        details.premises.name,
        address.addressNumber,
        address.streetName,
        address.district,
        address.town,
        address.county,
        address.postcode,
        address.telephoneNumber,
        startDate = details.arrivedAt.toLocalDate()
    )
}

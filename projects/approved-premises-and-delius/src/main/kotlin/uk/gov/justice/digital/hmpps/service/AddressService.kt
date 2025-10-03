package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.delius.address.AddressTypeCode
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
    fun updateMainAddressOnArrival(
        person: Person,
        details: PersonArrived,
        ap: ApprovedPremises
    ): Pair<PersonAddress?, PersonAddress>? {
        val previous = personAddressRepository.findMainAddress(person.id)
        return if (previous?.startDate?.isAfter(details.arrivedAt.toLocalDate()) != false) {
            previous?.endMainAddress(details.arrivedAt.toLocalDate())
            previous to personAddressRepository.save(ap.toAddress(person, details))
        } else null
    }

    fun endMainAddressOnDeparture(person: Person, endDate: LocalDate): PersonAddress? {
        return personAddressRepository.findMainAddress(person.id)
            ?.takeIf { it.type?.code == AddressTypeCode.APPROVED_PREMISES.code && it.startDate <= endDate }
            ?.also { it.endMainAddress(endDate) }
    }

    fun PersonAddress.endMainAddress(endDate: LocalDate) {
        val previousStatus = referenceDataRepository.previousAddressStatus()
        this.status = previousStatus
        this.endDate = endDate
    }

    private fun ApprovedPremises.toAddress(person: Person, details: PersonArrived) = PersonAddress(
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

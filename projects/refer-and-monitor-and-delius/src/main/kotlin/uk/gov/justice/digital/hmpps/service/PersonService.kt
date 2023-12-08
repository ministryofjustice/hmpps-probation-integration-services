package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.api.model.CaseDetail
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.api.model.ContactDetails
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Profile
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonDetail
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonDetailRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.mainAddress

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val personDetailRepository: PersonDetailRepository,
    private val personAddressRepository: PersonAddressRepository,
) {
    fun findIdentifiers(crn: String) = CaseIdentifier(crn, personRepository.findNomsId(crn))

    fun findDetailsFor(crn: String): CaseDetail? = personDetailRepository.findByCrn(crn)?.caseDetail()

    fun PersonDetail.caseDetail(): CaseDetail {
        val mainAddress = personAddressRepository.mainAddress(id)
        return CaseDetail(
            crn,
            Name(forename, surname),
            dob,
            gender?.description,
            Profile.from(
                language?.description,
                ethnicity?.description,
                religion?.description,
                disabilities.forProfile(),
            ),
            ContactDetails(
                mainAddress?.noFixedAbode ?: false,
                mainAddress?.forContactDetails(),
                emailAddress,
                telephoneNumber,
                mobileNumber,
            ),
        )
    }

    fun List<Disability>.forProfile() =
        map {
            uk.gov.justice.digital.hmpps.api.model.Disability(
                it.type.description,
                it.startDate,
                it.notes,
            )
        }

    fun PersonAddress.forContactDetails() =
        Address.from(buildingName, addressNumber, streetName, district, town, county, postcode)
}

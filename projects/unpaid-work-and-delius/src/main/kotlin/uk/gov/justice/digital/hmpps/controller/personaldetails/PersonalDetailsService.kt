package uk.gov.justice.digital.hmpps.controller.personaldetails

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.AddressEntity
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.Person
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonRepository
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.getPerson
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.Address
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.PersonalContact
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.PersonalDetails
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.Type
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.name

@Service
class PersonalDetailsService(val personRepository: PersonRepository) {
    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personRepository.getPerson(crn)
        return PersonalDetails(crn, mapPersonalCircumstances(person), mapPersonalContacts(person))
    }

    private fun mapPersonalContacts(person: Person): List<PersonalContact> =
        person.personalContactEntities.map {
            PersonalContact(
                it.relationship,
                it.name(),
                mapAddressTelephoneNumber(it.addressEntity),
                it.mobileNumber,
                mapAddress(it.addressEntity)
            )
        }
    private fun mapAddressTelephoneNumber(addressEntity: AddressEntity?): String? = addressEntity?.telephoneNumber
    private fun mapAddress(addressEntity: AddressEntity?): Address? = if (addressEntity != null)
        Address(
            addressEntity.buildingName,
            addressEntity.addressNumber,
            addressEntity.streetName,
            addressEntity.district,
            addressEntity.town,
            addressEntity.county,
            addressEntity.postcode
        )
    else
        null

    private fun mapPersonalCircumstances(person: Person): List<PersonalCircumstance> =
        person.personalCircumstanceEntities.map {
            PersonalCircumstance(
                Type(it.type.code, it.type.description),
                mapSubType(it.subType),
                it.notes,
                it.evidenced
            )
        }

    private fun mapSubType(subType: PersonalCircumstanceSubType?): Type? =
        if (subType != null) Type(subType.code, subType.description)
        else null
}

package uk.gov.justice.digital.hmpps.controller.personaldetails

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonRepository
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.getPerson
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.PersonalDetails
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.name
import uk.gov.justice.digital.hmpps.integrations.common.model.Address
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalContact
import uk.gov.justice.digital.hmpps.integrations.common.model.Type

@Service
class PersonalDetailsService(val personRepository: PersonRepository) {
    fun getPersonalDetails(crn: String) = with(personRepository.getPerson(crn)) {
        PersonalDetails(
            crn = crn,
            personalCircumstances = personalCircumstances.map {
                PersonalCircumstance(
                    type = Type(it.type.code, it.type.description),
                    subType = it.subType?.let { t -> Type(t.code, t.description) },
                    notes = it.notes,
                    evidenced = it.evidenced ?: false
                )
            },
            personalContacts = personalContacts.map {
                PersonalContact(
                    relationship = it.relationship,
                    relationshipType = Type(it.relationshipType.code, it.relationshipType.description),
                    name = it.name(),
                    telephoneNumber = it.address?.telephoneNumber,
                    mobileNumber = it.mobileNumber,
                    address = it.address?.let { address ->
                        Address(
                            buildingName = address.buildingName,
                            addressNumber = address.addressNumber,
                            streetName = address.streetName,
                            district = address.district,
                            town = address.town,
                            county = address.county,
                            postcode = address.postcode
                        )
                    }
                )
            }
        )
    }
}

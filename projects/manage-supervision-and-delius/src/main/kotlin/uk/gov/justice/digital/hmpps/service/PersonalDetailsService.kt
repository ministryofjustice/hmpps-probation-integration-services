package uk.gov.justice.digital.hmpps.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.PersonalCircumstance
import uk.gov.justice.digital.hmpps.api.model.personalDetails.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactAddress
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonalContact

@Service
class PersonalDetailsService(
    private val personalDetailsRepository: PersonalDetailsRepository,
    private val addressRepository: PersonAddressRepository,
    private val documentRepository: DocumentRepository,
    private val alfrescoClient: AlfrescoClient
) {

    @Transactional
    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personalDetailsRepository.getPersonDetails(crn)
        val addresses = addressRepository.findByPersonId(person.id)
        val mainAddress = addresses.firstOrNull { it.status.code == "M" }?.toAddress()
        val otherAddresses = addresses.filter { it.status.code != "M" }.map(PersonAddress::toAddress).mapNotNull { it }
        val documents = documentRepository.findByPersonId(person.id)

        return PersonalDetails(
            crn = person.crn,
            name = person.name(),
            mainAddress = mainAddress,
            otherAddresses = otherAddresses,
            contacts = person.personalContacts.map(PersonalContact::toContact),
            preferredGender = person.gender.description,
            dateOfBirth = person.dateOfBirth,
            preferredName = person.preferredName,
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            circumstances = Circumstances(lastUpdated = person.personalCircumstances.maxByOrNull { it.lastUpdated }?.lastUpdated,
                circumstances = person.personalCircumstances.map {
                    PersonalCircumstance(
                        it.subType.description,
                        it.type.description
                    )
                }),
            disabilities = Disabilities(
                lastUpdated = person.disabilities.maxByOrNull { it.lastUpdated }?.lastUpdated,
                disabilities = person.disabilities.map { it.type.description }),
            provisions = Provisions(
                lastUpdated = person.provisions.maxByOrNull { it.lastUpdated }?.lastUpdated,
                provisions = person.provisions.map { it.type.description }),
            documents = documents.map(PersonDocument::toDocument),
            pnc = person.pnc,
            religionOrBelief = person.religion?.description,
            sex = person.gender.description,
            sexualOrientation = person.sexualOrientation?.description,
            email = person.emailAddress
        )
    }

    fun downloadDocument(crn: String, id: String): ResponseEntity<StreamingResponseBody> {
        val filename = documentRepository.getDocument(crn, id)
        return alfrescoClient.streamDocument(id, filename)
    }
}

fun PersonalContact.toContact() = uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonalContact(
    name = Name(forename = forename, middleName = middleNames, surname = surname),
    relationship = relationship,
    address = address.toAddress(),
    notes = notes,
    relationshipType = relationshipType.description
)

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
fun PersonAddress.toAddress() = Address.from(
    buildingName = buildingName,
    buildingNumber = buildingNumber,
    streetName = streetName,
    district = district,
    town = town,
    county = county,
    postcode = postcode,
    from = startDate,
    to = endDate,
    lastUpdated = lastUpdated,
    status = status.description,
    type = type.description
)

fun ContactAddress.toAddress() = uk.gov.justice.digital.hmpps.api.model.personalDetails.ContactAddress.from(
    buildingName = buildingName,
    buildingNumber = addressNumber,
    streetName = streetName,
    district = district,
    town = town,
    county = county,
    postcode = postcode,
    lastUpdated = lastUpdated
)

fun PersonDocument.toDocument() = Document(id = alfrescoId, name = name, lastUpdated = lastUpdated)
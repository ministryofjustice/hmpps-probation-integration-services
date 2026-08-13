package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.model.Address
import uk.gov.justice.digital.hmpps.model.DocumentCrn
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.PersonDetails
import java.util.*

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val documentRepository: DocumentRepository,
    private val ldapTemplate: LdapTemplate
) {

    fun getBasicDetails(crn: String, username: String): PersonDetails {
        ldapTemplate.findAttributeByUsername(username, "cn")
            ?: throw NotFoundException("User", "username", username)
        ldapTemplate.findAttributeByUsername(username, "userHomeArea")
            ?: throw IllegalArgumentException("No home area found for $username")
        val person = personRepository.findByCrn(crn).orNotFoundBy("crn", crn)
        val addresses = personAddressRepository.findByPersonId(person.id).map {
            Address(
                id = it.id,
                status = it.status.description,
                buildingName = it.buildingName,
                buildingNumber = it.addressNumber,
                streetName = it.streetName,
                townCity = it.townCity,
                district = it.district,
                county = it.county,
                postcode = it.postcode,
            )
        }
        return PersonDetails(
            title = person.title?.description,
            name = Name(person.firstName, person.middleName(), person.surname),
            dateOfBirth = person.dateOfBirth,
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            emailAddress = person.emailAddress,
            addresses = addresses
        )
    }

    fun crnFor(cossoId: UUID): DocumentCrn =
        documentRepository.findByExternalReference(DocumentEntity.cossoBreachNoticeUrn(cossoId))
            ?.let { DocumentCrn(it.person.crn) }
            ?: throw NotFoundException("Cosso", "id", cossoId)
}

package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.*
import java.util.*

@Service
@Transactional(readOnly = true)
class DetailsService(
    private val personRepository: PersonRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val documentRepository: DocumentRepository,
    private val ldapTemplate: LdapTemplate,
) {
    fun basicDetails(crn: String, username: String): BasicDetails {
        val homeArea = ldapTemplate.findAttributeByUsername(username, "userHomeArea")
            ?: throw IllegalArgumentException("No home area found for $username")
        val defaultReplyAddress = ldapTemplate.findPreferenceByUsername(username, "replyAddress")?.toLongOrNull()
        val officeLocations = officeLocationRepository.findAllByProviderCode(homeArea)
        val person = personRepository.getByCrn(crn)
        return BasicDetails(
            title = person.title?.description,
            name = person.name(),
            addresses = person.addresses.map(PersonAddress::toAddress),
            replyAddresses = officeLocations.map {
                it.toAddress().copy(status = if (it.id == defaultReplyAddress) "Default" else null)
            },
        )
    }

    fun crnFor(breachNoticeId: UUID): DocumentCrn =
        documentRepository.findByExternalReference(Document.breachNoticeUrn(breachNoticeId))
            ?.let { DocumentCrn(it.person.crn) }
            ?: throw NotFoundException("BreachNotice", "id", breachNoticeId)
}

fun Person.name() = Name(firstName, listOfNotNull(secondName, thirdName).joinToString(" "), surname)

fun PersonAddress.toAddress() = Address(
    id,
    status?.description,
    buildingName,
    buildingNumber,
    streetName,
    townCity,
    district,
    county,
    postcode,
)

fun OfficeLocation.toAddress() = OfficeAddress(
    id,
    null,
    description,
    buildingName,
    buildingNumber,
    streetName,
    townCity,
    district,
    county,
    postcode,
)

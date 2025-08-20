package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse

@Service
@Transactional(readOnly = true)
class DetailsService(
    private val personRepository: PersonRepository,
    private val staffRepository: StaffRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val ldapTemplate: LdapTemplate,
) {
    fun basicDetails(crn: String): BasicDetails {
        val person = personRepository.getByCrn(crn)

        return BasicDetails(
            title = person.title?.description,
            name = person.name(),
            prisonNumber = person.prisonerNumber,
            dateOfBirth = person.dateOfBirth,
            addresses = person.addresses.map { it.toAddress() }
        )
    }

    fun signAndSend(username: String): SignAndSendResponse {
        val ldapUser = ldapTemplate.findByUsername<LdapUser>(username)
            ?: throw NotFoundException("User", "username", username)
        if (ldapUser.userHomeArea == null) {
            throw IllegalArgumentException("No home area found for $username")
        }

        val staff = staffRepository.findByUserUsername(username)
            ?: throw IllegalArgumentException("No staff record found for $username")
        val defaultReplyAddress = ldapTemplate.findPreferenceByUsername(username, "replyAddress")?.toLongOrNull()
        val officeLocations = officeLocationRepository.findAllByProviderCode(ldapUser.userHomeArea)

        return SignAndSendResponse(
            title = staff.title?.code,
            name = staff.name(),
            telephoneNumber = ldapUser.telephoneNumber,
            emailAddress = ldapUser.email,
            addresses = officeLocations.map {
                it.toAddress().copy(status = if (it.id == defaultReplyAddress) "Default" else null)
            },
        )
    }
}
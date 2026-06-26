package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.ResponsibleOfficerResponse
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse
import uk.gov.justice.digital.hmpps.model.UserDetails

@Service
class SignAndSendService(
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate,
    private val officeLocationRepository: OfficeLocationRepository,
) {
    fun getSignAndSend(crn: String, username: String): SignAndSendResponse {
        if (!personRepository.existsByCrn(crn)) throw NotFoundException("Person", "CRN", crn)

        val user = ldapTemplate.findByUsername<LdapUser>(username).orNotFoundBy("username", username)
        val responsibleOfficer = responsibleOfficerRepository.findByPersonCrn(crn).orNotFoundBy("CRN", crn)
        val responsibleOfficerUser = responsibleOfficer.username?.let { ldapTemplate.findByUsername<LdapUser>(it) }
        val defaultReplyAddress = responsibleOfficer.username?.let {
            ldapTemplate.findPreferenceByUsername(it, "replyAddress")?.toLongOrNull()
        }

        return SignAndSendResponse(
            userDetails = UserDetails(name = user.name()),
            responsibleOfficer = ResponsibleOfficerResponse(
                title = responsibleOfficer.staff.title?.description,
                name = responsibleOfficer.staff.name(),
                telephoneNumber = responsibleOfficerUser?.telephoneNumber,
                emailAddress = responsibleOfficerUser?.email,
                addresses = responsibleOfficerUser?.userHomeArea
                    ?.let { officeLocationRepository.findAllByProviderCode(it) }
                    ?.map { it.toAddress().copy(status = if (it.id == defaultReplyAddress) "Default" else null) }
                    ?: emptyList(),
            )
        )
    }
}

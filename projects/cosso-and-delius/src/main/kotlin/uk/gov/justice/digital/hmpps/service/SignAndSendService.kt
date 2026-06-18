package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse
import uk.gov.justice.digital.hmpps.model.TitleAndName

@Service
class SignAndSendService(
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate,
    private val officeLocationRepository: OfficeLocationRepository,
) {
    fun getSignAndSend(crn: String, username: String): SignAndSendResponse {
        personRepository.findByCrn(crn)
        val ldapUser = ldapTemplate.findByUsername<LdapUser>(username) ?: throw NotFoundException("User", "username", username)
        val homeArea = ldapUser.userHomeArea ?: throw IllegalArgumentException("No home area found for $username")
        val defaultReplyAddress = ldapTemplate.findPreferenceByUsername(username, "replyAddress")?.toLongOrNull()

        val officeLocations = officeLocationRepository.findAllByProbationAreaCode(homeArea)
        val responsibleOfficer = responsibleOfficerRepository.findByPersonCrn(crn).orNotFoundBy("CRN", crn)
        val offenderManager = responsibleOfficer.offenderManager
        val prisonOffenderManager = responsibleOfficer.prisonOffenderManager
        val staff = (offenderManager?.staff ?: prisonOffenderManager?.staff) ?: throw NotFoundException("Person", "CRN", crn)

        return SignAndSendResponse(
            userDetails = ldapUser.name(),
            responsibleOfficer = TitleAndName(staff.title?.description, staff.name()),
            telephoneNumber = ldapUser.telephoneNumber,
            emailAddress = ldapUser.email,
            addresses = officeLocations.map {
                it.toAddress().copy(status = if (it.id == defaultReplyAddress) "Default" else null)
            },
        )
    }
}

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
    private val userRepository: UserRepository,
) {
    fun getSignAndSend(crn: String): SignAndSendResponse {
        personRepository.findByCrn(crn)

        val responsibleOfficer = responsibleOfficerRepository.findByPersonCrn(crn).orNotFoundBy("CRN", crn)
        val offenderManager = responsibleOfficer.offenderManager
        val prisonOffenderManager = responsibleOfficer.prisonOffenderManager
        val staff = (offenderManager?.staff ?: prisonOffenderManager?.staff)
            ?: throw NotFoundException("Person", "CRN", crn)

        val ldapUser = userRepository.findByStaffId(staff.id)?.let {
            ldapTemplate.findByUsername<LdapUser>(it.username)
        }

        val addresses = ldapUser?.userHomeArea?.let { homeArea ->
            val defaultReplyAddress =
                ldapTemplate.findPreferenceByUsername(ldapUser.username, "replyAddress")?.toLongOrNull()

            officeLocationRepository.findAllByProbationAreaCode(ldapUser.userHomeArea).map {
                it.toAddress().copy(status = if (it.id == defaultReplyAddress) "Default" else null)
            }
        } ?: emptyList()

        return SignAndSendResponse(
            responsibleOfficer = TitleAndName(staff.title?.description, staff.name()),
            userDetails = ldapUser?.name(),
            telephoneNumber = ldapUser?.telephoneNumber,
            emailAddress = ldapUser?.email,
            addresses = addresses,
        )
    }
}

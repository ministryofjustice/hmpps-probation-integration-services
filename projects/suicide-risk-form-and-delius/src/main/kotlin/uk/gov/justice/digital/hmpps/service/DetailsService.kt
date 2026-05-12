package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.model.DocumentCrn
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse
import java.util.*

@Service
@Transactional(readOnly = true)
class DetailsService(
    private val personRepository: PersonRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val ldapTemplate: LdapTemplate,
    private val documentRepository: DocumentRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val userRepository: UserRepository
) {
    fun basicDetails(crn: String): BasicDetails {
        val person = personRepository.getByCrn(crn)

        return BasicDetails(
            title = person.title?.description,
            name = person.name(),
            nomsNumber = person.nomsNumber,
            dateOfBirth = person.dateOfBirth,
            addresses = person.addresses.map { it.toAddress() }
        )
    }

    fun signAndSend(crn: String): SignAndSendResponse {
        personRepository.getByCrn(crn)

        val responsibleOfficer = responsibleOfficerRepository.findByPerson_Crn(crn)
            ?: throw NotFoundException("ResponsibleOfficer", "CRN", crn)
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

            officeLocationRepository.findAllByProviderCode(ldapUser.userHomeArea).map {
                it.toAddress().copy(status = if (it.id == defaultReplyAddress) "Default" else null)
            }
        } ?: emptyList()

        return SignAndSendResponse(
            name = ldapUser?.name() ?: staff.name(),
            telephoneNumber = ldapUser?.telephoneNumber,
            emailAddress = ldapUser?.email,
            addresses = addresses,
        )
    }

    fun crnFor(suicideRiskFormId: UUID): DocumentCrn =
        documentRepository.findByExternalReference(Document.suicideRiskFormUrn(suicideRiskFormId))
            ?.let { DocumentCrn(it.person.crn) }
            ?: throw NotFoundException("SuicideRiskForm", "id", suicideRiskFormId)
}
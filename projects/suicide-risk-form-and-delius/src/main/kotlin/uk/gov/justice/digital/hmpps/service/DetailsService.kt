package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.*
import java.util.*

@Service
@Transactional(readOnly = true)
class DetailsService(
    private val personRepository: PersonRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val ldapTemplate: LdapTemplate,
    private val documentRepository: DocumentRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
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

    fun signAndSend(crn: String, username: String): SignAndSendResponse {
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

    fun crnFor(suicideRiskFormId: UUID): DocumentCrn =
        documentRepository.findByExternalReference(Document.suicideRiskFormUrn(suicideRiskFormId))
            ?.let { DocumentCrn(it.person.crn) }
            ?: throw NotFoundException("SuicideRiskForm", "id", suicideRiskFormId)
}
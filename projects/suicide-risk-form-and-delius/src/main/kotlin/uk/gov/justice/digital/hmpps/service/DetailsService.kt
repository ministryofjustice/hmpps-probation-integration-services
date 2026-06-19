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
import uk.gov.justice.digital.hmpps.model.TitleAndName
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
        personRepository.getByCrn(crn)
        val ldapUser =
            ldapTemplate.findByUsername<LdapUser>(username) ?: throw NotFoundException("User", "username", username)
        val homeArea = ldapUser.userHomeArea ?: throw IllegalArgumentException("No home area found for $username")
        val defaultReplyAddress = ldapTemplate.findPreferenceByUsername(username, "replyAddress")?.toLongOrNull()
        val officeLocations = officeLocationRepository.findAllByProviderCode(homeArea)
        val responsibleOfficer = responsibleOfficerRepository.findByPerson_Crn(crn)
            ?: throw NotFoundException("ResponsibleOfficer", "CRN", crn)
        val offenderManager = responsibleOfficer.offenderManager
        val prisonOffenderManager = responsibleOfficer.prisonOffenderManager
        val staff = (offenderManager?.staff ?: prisonOffenderManager?.staff)
            ?: throw NotFoundException("Person", "CRN", crn)

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

    fun crnFor(suicideRiskFormId: UUID): DocumentCrn =
        documentRepository.findByExternalReference(Document.suicideRiskFormUrn(suicideRiskFormId))
            ?.let { DocumentCrn(it.person.crn) }
            ?: throw NotFoundException("SuicideRiskForm", "id", suicideRiskFormId)
}
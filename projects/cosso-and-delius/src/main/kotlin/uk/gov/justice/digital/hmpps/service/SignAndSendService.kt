package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.LdapUser
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.name
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse

@Service
class SignAndSendService(
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate,
) {
    fun getSignAndSend(crn: String, username: String): SignAndSendResponse {
        if (!personRepository.existsByCrn(crn)) throw NotFoundException("Person", "CRN", crn)
        val user = ldapTemplate.findByUsername<LdapUser>(username).orNotFoundBy("username", username)
        return SignAndSendResponse(userDetails = user.name())
    }
}

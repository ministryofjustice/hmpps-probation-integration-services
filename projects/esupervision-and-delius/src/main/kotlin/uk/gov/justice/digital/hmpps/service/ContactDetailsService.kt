package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.Team
import uk.gov.justice.digital.hmpps.integrations.delius.name
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsernames
import uk.gov.justice.digital.hmpps.model.CodedDescription
import uk.gov.justice.digital.hmpps.model.Practitioner

@Service
class ContactDetailsService(
    val comRepository: PersonManagerRepository,
    val ldapTemplate: LdapTemplate,
) {
    fun getContactDetailsForCrn(crn: String) =
        comRepository.findByPersonCrn(crn)?.let { com ->
            val email = com.staff.user?.username?.let { ldapTemplate.findEmailByUsername(it) }
            uk.gov.justice.digital.hmpps.model.ContactDetails(
                crn = com.person.crn,
                name = uk.gov.justice.digital.hmpps.model.Name(
                    forename = com.person.firstName,
                    surname = com.person.lastName
                ),
                mobile = com.person.mobile,
                email = com.person.emailAddress,
                com.asPractitioner { email }
            )
        }

    fun getContactDetailsForCrns(crns: List<String>) {
        val coms = comRepository.findByPersonCrnIn(crns)
        val emails = coms.mapNotNull { it.staff.user?.username }.takeIf { it.isNotEmpty() }?.let {
            ldapTemplate.findEmailByUsernames(it)
        } ?: emptyMap()
        coms.map { com ->
            uk.gov.justice.digital.hmpps.model.ContactDetails(
                crn = com.person.crn,
                name = uk.gov.justice.digital.hmpps.model.Name(
                    forename = com.person.firstName,
                    surname = com.person.lastName
                ),
                mobile = com.person.mobile,
                email = com.person.emailAddress,
                com.asPractitioner { emails[it] }
            )
        }
    }
}

fun PersonManager.asPractitioner(getEmail: (String) -> String?) = Practitioner(
    staff.code,
    staff.name(),
    team.ldu(),
    team.pdu(),
    with(provider) { CodedDescription(code, description) },
    staff.user?.username?.let { getEmail(it) }
)

fun Team.ldu() = with(ldu) { CodedDescription(code, description) }
fun Team.pdu() = with(ldu.pdu) { CodedDescription(code, description) }
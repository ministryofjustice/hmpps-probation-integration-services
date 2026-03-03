package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.entity.Team
import uk.gov.justice.digital.hmpps.entity.event.EventEntity
import uk.gov.justice.digital.hmpps.entity.name
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsernames
import uk.gov.justice.digital.hmpps.model.*

@Service
class ContactDetailsService(
    val comRepository: PersonManagerRepository,
    val ldapTemplate: LdapTemplate,
) {
    fun getContactDetailsForCrn(crn: String) =
        comRepository.findByPersonCrn(crn)?.let { com ->
            val email = com.staff.user?.username?.let { ldapTemplate.findEmailByUsername(it) }
            ContactDetails(
                crn = com.person.crn,
                name = Name(
                    forename = com.person.firstName,
                    surname = com.person.lastName
                ),
                mobile = com.person.mobile,
                email = com.person.emailAddress,
                events = com.person.events.map { it.asEvent() },
                practitioner = com.asPractitioner { email }
            )
        }

    fun getContactDetailsForCrns(crns: List<String>): List<ContactDetails> {
        return comRepository.findByPersonCrnIn(crns).let { coms ->
            val emails = coms.mapNotNull { it.staff.user?.username }
                .takeIf { it.isNotEmpty() }
                ?.let { usernames -> ldapTemplate.findEmailByUsernames(usernames) }
                ?: emptyMap()

            coms.map { com ->
                ContactDetails(
                    crn = com.person.crn,
                    name = Name(
                        forename = com.person.firstName,
                        surname = com.person.lastName,
                    ),
                    mobile = com.person.mobile,
                    email = com.person.emailAddress,
                    events = com.person.events.map { it.asEvent() },
                    practitioner = com.asPractitioner { emails[it] },
                )
            }
        }
    }

    fun PersonManager.asPractitioner(getEmail: (String) -> String?) = Practitioner(
        code = staff.code,
        name = staff.name(),
        localAdminUnit = team.ldu(),
        probationDeliveryUnit = team.pdu(),
        provider = with(provider) { CodedDescription(code, description) },
        email = staff.user?.username?.let { getEmail(it) }
    )

    fun EventEntity.asEvent() = Event(
        number = number.toInt(),
        mainOffence = CodedDescription(mainOffence.offence.code, mainOffence.offence.description),
        sentence = disposal?.let {
            Event.Sentence(
                date = it.date,
                description = it.type.description
            )
        }
    )

    fun Team.ldu() = with(ldu) { CodedDescription(code, description) }
    fun Team.pdu() = with(ldu.pdu) { CodedDescription(code, description) }
}

package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.event.EventEntity
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsernames
import uk.gov.justice.digital.hmpps.model.*

@Service
class ContactDetailsService(
    val comRepository: PersonManagerRepository,
    val registrationRepository: RegistrationRepository,
    val ldapTemplate: LdapTemplate,
    val personRepository: PersonRepository,
    val contactRepository: ContactRepository,
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
                dateOfBirth = com.person.dateOfBirth,
                mobile = com.person.mobile,
                email = com.person.emailAddress,
                events = com.person.activeEvents.map { it.asEvent() },
                practitioner = com.asPractitioner { email },
                contactSuspended = registrationRepository.existsByPersonIdAndTypeCode(
                    com.person.id,
                    RegisterType.CONTACT_SUSPENDED_TYPE_CODE
                ),
            )
        }

    fun getContactDetailsForCrns(crns: List<String>): List<ContactDetails> {
        return comRepository.findByPersonCrnIn(crns).let { coms ->
            val usernames = coms.mapNotNull { it.staff.user?.username }
            val emails = if (usernames.isNotEmpty()) {
                ldapTemplate.findEmailByUsernames(usernames)
            } else {
                emptyMap()
            }

            val personIds = coms.map { it.person.id }
            val casesWithContactSuspended = if (personIds.isNotEmpty()) {
                registrationRepository.findPersonIdsWithActiveType(personIds, RegisterType.CONTACT_SUSPENDED_TYPE_CODE)
            } else {
                emptySet()
            }

            coms.map { com ->
                ContactDetails(
                    crn = com.person.crn,
                    name = Name(
                        forename = com.person.firstName,
                        surname = com.person.lastName,
                    ),
                    dateOfBirth = com.person.dateOfBirth,
                    mobile = com.person.mobile,
                    email = com.person.emailAddress,
                    events = com.person.activeEvents.map { it.asEvent() },
                    practitioner = com.asPractitioner { emails[it] },
                    contactSuspended = com.person.id in casesWithContactSuspended,
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
        email = staff.user?.username?.let { getEmail(it) },
        unallocated = staff.code.endsWith("U"),
        username = staff.user?.username,
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

    @Transactional
    fun updateContactDetails(crn: String, update: UpdateContactDetails) {
        val person = personRepository.getByCrn(crn)
        person.mobile = update.mobileNumber
        person.emailAddress = update.emailAddress
    }

    fun getAlertCount(username: String): AlertCount {
        val count = contactRepository.countAlertsByUsername(username)
        return AlertCount(count)
    }
}

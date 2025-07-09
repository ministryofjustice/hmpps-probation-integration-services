package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.PersonalDetails
import uk.gov.justice.digital.hmpps.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate,
) {
    fun getPersonalDetails(crn: String) = personRepository.findByCrn(crn)?.let { person ->
        PersonalDetails(
            crn = person.crn,
            name = Name(
                forename = person.forename,
                middleNames = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                surname = person.surname
            ),
            dateOfBirth = person.dateOfBirth,
            sex = person.gender.toCodedValue(),
            ethnicity = person.ethnicity?.toCodedValue(),
            probationPractitioner = person.manager?.staff?.let { staff ->
                ProbationPractitioner(
                    name = Name(
                        forename = staff.forename,
                        surname = staff.surname
                    ),
                    email = staff.user?.let { ldapTemplate.findEmailByUsername(it.username) }
                )
            },
            probationDeliveryUnit = person.manager?.team?.localAdminUnit?.probationDeliveryUnit?.let { pdu ->
                CodedValue(pdu.code, pdu.description)
            }
        )
    } ?: throw NotFoundException("Person", "crn", crn)
}
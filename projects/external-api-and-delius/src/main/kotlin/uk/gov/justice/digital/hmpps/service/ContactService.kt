package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.Category.Companion.fromNumber
import uk.gov.justice.digital.hmpps.model.Category.entries
import uk.gov.justice.digital.hmpps.model.Provider
import java.time.LocalTime
import java.time.ZonedDateTime
import uk.gov.justice.digital.hmpps.integration.delius.entity.Team as TeamEntity

@Service
class ContactService(
    private val contactRepository: ContactRepository,
    private val personRepository: PersonRepository

) {
    fun getById(crn: String, contactId: Long, mappaCategories: List<Int>): ContactLogged {
        val person = personRepository.getMappaPersonInMappaCategory(crn, getMappaCategoryCodes(mappaCategories))
        return contactRepository.getContact(person.crn, contactId).asContactLogged()
    }

    fun getVisorContacts(crn: String, mappaCategories: List<Int>, pageable: Pageable): ContactsLogged {
        val person = personRepository.getMappaPersonInMappaCategory(crn, getMappaCategoryCodes(mappaCategories))
        return contactRepository.findVisorContacts(
            person.crn,
            pageable
        ).map { it.asContactLogged() }.asResponse()
    }

    private fun getMappaCategoryCodes(mappaCategories: List<Int>) =
        (mappaCategories.mapNotNull { fromNumber(it)?.name }.takeIf { it.isNotEmpty() }
            ?: entries.map { it.name }).toSet()
}

private fun Contact.asContactLogged() = ContactLogged(
    id,
    person.crn,
    createdDateTime,
    lastUpdatedDateTime,
    ZonedDateTime.of(date, startTime?.toLocalTime() ?: LocalTime.MIDNIGHT, EuropeLondon),
    CodedValue(type.code, type.description),
    description ?: type.description,
    location?.let { CodedValue(it.code, it.description) },
    outcome?.let { CodedValue(it.code, it.description) },
    staff.withTeam(team),
    notes
)

private fun Staff.withTeam(team: TeamEntity) = Officer(code, name(), team.asOfficerTeam())
private fun TeamEntity.asOfficerTeam() = OfficerTeam(code, description, pdu())
private fun TeamEntity.pdu() = with(lau.pdu) {
    OfficerPdu(code, description, Provider(provider.code, provider.description))
}

private fun Page<ContactLogged>.asResponse() = ContactsLogged(content, totalPages, totalElements)
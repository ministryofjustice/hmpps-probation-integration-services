package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integration.delius.entity.Contact
import uk.gov.justice.digital.hmpps.integration.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integration.delius.entity.getContact
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.Category.Companion.fromNumber
import uk.gov.justice.digital.hmpps.model.Category.entries
import java.time.LocalTime
import java.time.ZonedDateTime
import uk.gov.justice.digital.hmpps.integration.delius.entity.Team as TeamEntity

@Service
class ContactService(private val contactRepository: ContactRepository) {
    fun getById(crn: String, contactId: Long): ContactLogged =
        contactRepository.getContact(crn, contactId).asContactLogged()

    fun getVisorContacts(crn: String, mappaCategories: List<Int>, pageable: Pageable): ContactsLogged {
        return contactRepository.findVisorContacts(
            crn,
            (mappaCategories.mapNotNull { fromNumber(it)?.name }.takeIf { it.isNotEmpty() }
                ?: entries.map { it.name }).toSet(),
            pageable
        ).map { it.asContactLogged() }.asResponse()
    }
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
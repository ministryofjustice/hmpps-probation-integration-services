package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.*
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import java.time.LocalTime

@Service
class UserAlertService(
    private val contactRepository: ContactRepository,
    private val contactAlertRepository: ContactAlertRepository,
    val personRepository: PersonRepository
) {
    @Transactional(readOnly = true)
    fun getUserAlerts(pageable: Pageable): UserAlerts {
        val page: Page<Contact> = UserContext.get()?.let {
            contactRepository.findAllUserAlerts(it.username, pageable)
        } ?: PageImpl(emptyList(), pageable, 0)
        return UserAlerts(
            page.content.map(Contact::toUserAlert),
            page.totalElements.toInt(),
            page.totalPages,
            pageable.pageNumber,
            pageable.pageSize,
        )
    }

    @Transactional
    fun clearAlerts(toClear: ClearAlerts) {
        val username = requireNotNull(UserContext.get()?.username) { "username required in token" }
        contactAlertRepository.deleteByContactIdIn(toClear.alertIds)
        contactRepository.findAllById(toClear.alertIds).takeIf { it.isNotEmpty() }?.let { alerts ->
            val comConfirmations = personRepository.userIsCom(username, alerts.map { it.person.crn }.toSet())
                .associate { it.crn to it.userIsCom }
            alerts.forEach {
                if (comConfirmations[it.person.crn] == true && it.alert == true) {
                    it.alert = false
                    it.appendNotes("Alert cleared from the Manage people on probation service")
                }
            }
        }
    }

    fun getAlertNote(alertId: Long, noteId: Int): UserAlert? = contactRepository.getContact(alertId).toUserAlert(noteId)
}

private fun Contact.toUserAlert(noteId: Int? = null): UserAlert = UserAlert(
    id,
    UserAlertType(type.description, type.editable == true),
    person.crn,
    person.name(),
    date,
    startTime?.toLocalTime() ?: LocalTime.MIN,
    description,
    if (noteId == null) formatNote(notes, true) else listOf(),
    noteId?.let { formatNote(notes, false).takeIf { it.size > noteId }?.get(noteId) },
    Staff(Name(staff!!.forename, surname = staff.surname), staff.code)
)
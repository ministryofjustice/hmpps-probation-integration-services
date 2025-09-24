package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.Staff
import uk.gov.justice.digital.hmpps.api.model.user.UserAlert
import uk.gov.justice.digital.hmpps.api.model.user.UserAlertType
import uk.gov.justice.digital.hmpps.api.model.user.UserAlerts
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository

@Transactional
@Service
class UserAlertService(private val contactRepository: ContactRepository) {
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
}

private fun Contact.toUserAlert(): UserAlert = UserAlert(
    id,
    UserAlertType(type.description, type.editable),
    person.crn,
    date,
    description,
    notes,
    Staff(Name(staff!!.forename, surname = staff.surname), staff.code)
)
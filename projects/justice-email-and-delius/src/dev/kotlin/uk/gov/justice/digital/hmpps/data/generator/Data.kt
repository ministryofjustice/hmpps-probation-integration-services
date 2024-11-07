package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

object Data {
    val PERSON = Person(id(), crn = "A000001")
    val STAFF = staffWithUser(StaffUser(id(), username = "test-user"))
    val DUPLICATE_STAFF_1 = staffWithUser(StaffUser(id(), username = "duplicate1"))
    val DUPLICATE_STAFF_2 = staffWithUser(StaffUser(id(), username = "duplicate2"))
    val MANAGER_STAFF = Staff(id())
    val MANAGER = PersonManager(id(), PERSON, MANAGER_STAFF.id, 102, 103)
    val CONTACT_TYPES = ContactType.Code.entries.map { ContactType(id(), it.code) }.toTypedArray()
    val BUSINESS_INTERACTIONS = BusinessInteractionCode.entries
        .map { BusinessInteraction(id(), it.code, ZonedDateTime.now()) }.toTypedArray()

    private fun id() = IdGenerator.getAndIncrement()

    private fun staffWithUser(user: StaffUser) = Staff(id(), user = user).also { user.set("staff", it) }
}

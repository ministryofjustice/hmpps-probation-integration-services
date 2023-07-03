package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "ManagePomCasesAndDelius")
    val DEFAULT_STAFF_USER = generateStaffUser("DefaultStaff")

    fun generateStaffUser(username: String, id: Long = IdGenerator.getAndIncrement()) = StaffUser(username, id = id)
}

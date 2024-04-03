package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "ProbationSearchAndDelius")
    val SEARCH_USER = AuditUser(IdGenerator.getAndIncrement(), "SearchUser")
}

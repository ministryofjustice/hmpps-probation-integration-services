package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "EffectiveProposalFrameworkAndDelius")
    val JOHN_SMITH = AuditUser(IdGenerator.getAndIncrement(), "john-smith")
}

fun AuditUser.asLaoUser() = LimitedAccessUser(username, id)
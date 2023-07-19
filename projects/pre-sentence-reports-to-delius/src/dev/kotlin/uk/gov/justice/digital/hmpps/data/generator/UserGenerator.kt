package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.audit.entity.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "PreSentenceService")
    val DOCUMENT_USER = User(IdGenerator.getAndIncrement(), "probation-integration-dev", "Creator", "Document")
}

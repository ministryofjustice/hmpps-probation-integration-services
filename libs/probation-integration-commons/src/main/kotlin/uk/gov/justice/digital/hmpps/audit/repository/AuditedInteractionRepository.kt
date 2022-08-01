package uk.gov.justice.digital.hmpps.audit.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.AuditedInteractionId

interface AuditedInteractionRepository : JpaRepository<AuditedInteraction, AuditedInteractionId>

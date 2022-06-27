package uk.gov.justice.digital.hmpps.integrations.delius.audit.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteractionId

interface AuditedInteractionRepository : JpaRepository<AuditedInteraction, AuditedInteractionId>

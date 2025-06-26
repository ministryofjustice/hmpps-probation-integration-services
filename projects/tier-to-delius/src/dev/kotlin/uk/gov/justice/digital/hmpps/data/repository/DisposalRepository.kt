package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType

interface DisposalRepository : JpaRepository<Disposal, Long>

interface DisposalTypeRepository : JpaRepository<DisposalType, Long>

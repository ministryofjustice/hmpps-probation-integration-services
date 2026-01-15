package uk.gov.justice.digital.hmpps.data

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.RequirementTransfer

interface RequirementTransferRepository : JpaRepository<RequirementTransfer, Long>
interface EnforcementRepository : JpaRepository<Enforcement, Long>
interface EnforcementActionRepository : JpaRepository<EnforcementAction, Long>
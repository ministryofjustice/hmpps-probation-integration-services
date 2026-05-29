package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProject

interface UnpaidWorkProjectRepository : JpaRepository<UnpaidWorkProject, Long>
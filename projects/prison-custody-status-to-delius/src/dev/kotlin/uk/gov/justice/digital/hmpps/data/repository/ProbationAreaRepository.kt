package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity.ProbationArea

interface ProbationAreaRepository : JpaRepository<ProbationArea, Long>

package uk.gov.justice.digital.hmpps.data

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea

interface ProbationAreaRepository : JpaRepository<ProbationArea, Long>

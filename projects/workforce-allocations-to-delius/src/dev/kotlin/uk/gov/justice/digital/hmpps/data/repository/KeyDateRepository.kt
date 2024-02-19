package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.data.entity.KeyDate

interface KeyDateRepository : JpaRepository<KeyDate, Long>

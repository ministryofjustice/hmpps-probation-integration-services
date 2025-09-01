package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long>
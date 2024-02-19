package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementAdditionalMainCategory

interface RequirementAdditionalMainCategoryRepository : JpaRepository<RequirementAdditionalMainCategory, Long>

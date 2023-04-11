package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.controller.entity.RegisterType
import uk.gov.justice.digital.hmpps.controller.entity.RequirementMainCategory

interface RequirementMainCategoryRepository : JpaRepository<RequirementMainCategory, Long>

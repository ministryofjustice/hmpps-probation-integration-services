package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository

interface IapsRequirementRepository : JpaRepository<IapsRequirement, Long>

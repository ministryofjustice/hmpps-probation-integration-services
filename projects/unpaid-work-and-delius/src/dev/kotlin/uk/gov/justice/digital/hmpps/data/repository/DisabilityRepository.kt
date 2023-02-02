package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.DisabilityEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.ProvisionEntity

interface DisabilityRepository : JpaRepository<DisabilityEntity, Long>
interface ProvisionRepository : JpaRepository<ProvisionEntity, Long>

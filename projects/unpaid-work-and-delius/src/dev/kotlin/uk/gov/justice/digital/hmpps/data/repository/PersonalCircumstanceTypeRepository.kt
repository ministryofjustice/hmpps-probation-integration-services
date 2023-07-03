package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.PersonalCircumstanceType

interface PersonalCircumstanceTypeRepository : JpaRepository<PersonalCircumstanceType, Long>

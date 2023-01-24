package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalCircumstanceEntity

interface PersonalCircumstanceRepository : JpaRepository<PersonalCircumstanceEntity, Long>

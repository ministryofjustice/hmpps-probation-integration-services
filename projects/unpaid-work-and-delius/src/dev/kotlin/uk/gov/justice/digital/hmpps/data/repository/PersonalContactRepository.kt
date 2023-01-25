package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalContactEntity

interface PersonalContactRepository : JpaRepository<PersonalContactEntity, Long>

package uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance.entity.PersonalCircumstance

interface PersonalCircumstanceRepository : JpaRepository<PersonalCircumstance, Long> {
    fun findByPersonId(personId: Long): List<PersonalCircumstance>
}
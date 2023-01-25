package uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs

import org.springframework.data.jpa.repository.JpaRepository

interface OASYSAssessmentRepository : JpaRepository<OASYSAssessment, Long> {
    fun findByPersonIdOrderByAssessmentDateDesc(personId: Long): OASYSAssessment?
}

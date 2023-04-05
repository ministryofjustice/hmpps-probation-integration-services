package uk.gov.justice.digital.hmpps.controller.entity

import org.springframework.data.jpa.repository.JpaRepository

interface OASYSAssessmentRepository : JpaRepository<OASYSAssessment, Long> {
    fun findFirstByPersonIdAndScoreIsNotNullOrderByAssessmentDateDesc(personId: Long): OASYSAssessment?
}

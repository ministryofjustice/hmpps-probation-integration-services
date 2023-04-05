package uk.gov.justice.digital.hmpps.controller.entity

import org.springframework.data.jpa.repository.JpaRepository

interface OGRSAssessmentRepository : JpaRepository<OGRSAssessment, Long> {
    fun findFirstByEventPersonIdAndScoreIsNotNullOrderByAssessmentDateDesc(personId: Long): OGRSAssessment?
}

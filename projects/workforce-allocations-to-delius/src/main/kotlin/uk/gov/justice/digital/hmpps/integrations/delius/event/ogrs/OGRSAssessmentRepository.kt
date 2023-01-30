package uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs

import org.springframework.data.jpa.repository.JpaRepository

interface OGRSAssessmentRepository : JpaRepository<OGRSAssessment, Long> {
    fun findFirstByEventPersonIdAndScoreIsNotNullOrderByAssessmentDateDesc(personId: Long): OGRSAssessment?
}

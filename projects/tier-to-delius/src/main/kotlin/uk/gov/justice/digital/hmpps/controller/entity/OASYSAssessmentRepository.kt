package uk.gov.justice.digital.hmpps.controller.entity

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OASYSAssessmentRepository : JpaRepository<OASYSAssessment, Long> {
    @Query(
        """
        select a from OASYSAssessment a
        where a.personId = :personId
        order by a.assessmentDate desc, a.lastModifiedDateTime desc
    """,
    )
    fun findLatestAssessment(
        personId: Long,
        pageable: Pageable = PageRequest.of(0, 1),
    ): List<OASYSAssessment>
}

fun OASYSAssessmentRepository.findLatest(personId: Long) =
    findLatestAssessment(personId).firstOrNull()

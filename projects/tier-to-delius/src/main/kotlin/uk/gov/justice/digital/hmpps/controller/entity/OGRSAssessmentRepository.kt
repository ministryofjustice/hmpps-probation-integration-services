package uk.gov.justice.digital.hmpps.controller.entity

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OGRSAssessmentRepository : JpaRepository<OGRSAssessment, Long> {
    @Query(
        """
        select a from OGRSAssessment a
        join fetch a.event e
        where e.person.id = :personId
        order by a.assessmentDate desc, a.lastModifiedDateTime desc
    """,
    )
    fun findLatestAssessment(
        personId: Long,
        pageable: Pageable = PageRequest.of(0, 1),
    ): List<OGRSAssessment>
}

fun OGRSAssessmentRepository.findLatest(personId: Long) =
    findLatestAssessment(personId).firstOrNull()

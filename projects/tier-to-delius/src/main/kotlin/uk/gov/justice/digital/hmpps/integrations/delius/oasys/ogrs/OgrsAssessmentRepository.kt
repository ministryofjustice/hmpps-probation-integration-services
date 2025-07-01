package uk.gov.justice.digital.hmpps.integrations.delius.oasys.ogrs

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.ogrs.entity.OgrsAssessment

interface OGRSAssessmentRepository : JpaRepository<OgrsAssessment, Long> {
    @Query(
        """
        select a from OgrsAssessment a
        join fetch a.event e
        where e.person.id = :personId
        order by a.assessmentDate desc, a.lastModifiedDateTime desc
    """
    )
    fun findLatestAssessment(personId: Long, pageable: Pageable = PageRequest.of(0, 1)): List<OgrsAssessment>
}

fun OGRSAssessmentRepository.findLatest(personId: Long) =
    findLatestAssessment(personId).firstOrNull()

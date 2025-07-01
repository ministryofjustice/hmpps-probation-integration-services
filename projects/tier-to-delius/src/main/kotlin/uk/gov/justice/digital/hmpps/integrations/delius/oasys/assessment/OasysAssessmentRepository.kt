package uk.gov.justice.digital.hmpps.integrations.delius.oasys.assessment

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.assessment.entity.OasysAssessment

interface OASYSAssessmentRepository : JpaRepository<OasysAssessment, Long> {
    @Query(
        """
        select a from OasysAssessment a
        where a.personId = :personId
        order by a.assessmentDate desc, a.lastModifiedDateTime desc
    """
    )
    fun findLatestAssessment(personId: Long, pageable: Pageable = PageRequest.of(0, 1)): List<OasysAssessment>
}

fun OASYSAssessmentRepository.findLatest(personId: Long) =
    findLatestAssessment(personId).firstOrNull()

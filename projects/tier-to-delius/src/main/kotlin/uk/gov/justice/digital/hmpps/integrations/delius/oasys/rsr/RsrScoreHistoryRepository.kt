package uk.gov.justice.digital.hmpps.integrations.delius.oasys.rsr

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.rsr.entity.RsrScoreHistory

interface RsrScoreHistoryRepository : JpaRepository<RsrScoreHistory, Long> {
    @Query(
        """
        select r from RsrScoreHistory r
        where r.personId = :personId
        order by r.dateRecorded desc,
                 (case when r.reasonForChange.code = 'D' then 1 else 0 end) desc
        """
    )
    @EntityGraph(attributePaths = ["reasonForChange.set"])
    fun findLatestScore(personId: Long, pageable: Pageable = PageRequest.of(0, 1)): List<RsrScoreHistory>
}

fun RsrScoreHistoryRepository.findLatest(personId: Long) =
    findLatestScore(personId).firstOrNull()
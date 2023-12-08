package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Nsi

interface NsiRepository : JpaRepository<Nsi, Long> {
    @Query(
        """
        SELECT nsi FROM Nsi nsi
        JOIN NomisTypeNsiType nom ON nsi.type.id = nom.nsiType.id
        WHERE nsi.offenderId = :offenderId
        AND nom.caseNoteType = :cnTypeCode
        AND nsi.active = true AND nsi.softDeleted = false
        ORDER BY nsi.referralDate DESC
    """,
    )
    fun findCaseNoteRelatedNsis(
        offenderId: Long,
        cnTypeCode: String,
        pageRequest: PageRequest = PageRequest.of(0, 1),
    ): List<Nsi>
}

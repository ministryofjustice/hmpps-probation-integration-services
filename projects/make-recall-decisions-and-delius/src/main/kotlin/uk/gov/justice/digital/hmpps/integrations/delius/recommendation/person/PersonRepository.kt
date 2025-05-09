package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {

    @Query(
        """
        SELECT p FROM Person p 
        LEFT JOIN p.manager 
        WHERE p.crn = :crn 
        AND p.softDeleted = false 
        AND p.manager.active = true
    """
    )
    fun findByCrn(crn: String): Person?

    @Query(
        """
        SELECT p FROM Person p 
        LEFT JOIN p.manager
        JOIN p.additionalIdentifier ai
        WHERE ai.mergedFromCrn = :crn 
        AND p.softDeleted = false 
        AND p.manager.active = true 
        AND ai.mergeDetail.code = 'MFCRN'             
        """
    )
    fun findByMergedFromCrn(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String): Person =
    findByCrn(crn) ?: findByMergedFromCrn(crn)
    ?: throw IgnorableMessageException("Person not found", mapOf("crn" to crn))

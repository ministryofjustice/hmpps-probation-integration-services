package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {

    @Query(
        """
        SELECT p FROM Person p 
        LEFT JOIN p.manager 
        WHERE p.crn = :crn AND p.softDeleted = false 
        AND p.manager.active = true 
        UNION 
        SELECT p FROM Person p 
        LEFT JOIN p.manager
        JOIN p.additionalIdentifier ai
        WHERE ai.crn = :crn 
        AND p.softDeleted = false 
        AND p.manager.active = true 
        AND ai.mergeDetail.code IN ('DOFF', 'MFCRN', 'MTCRN', 'PCRN') 
    """
    )
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @Query(
        """
        select pa from PersonAddress pa
        join fetch pa.status.dataset
        join fetch pa.type.dataset
        where pa.personId = :personId 
        and pa.softDeleted = false  
        and pa.endDate is null 
        and pa.status.code = 'M'
    """
    )
    fun findMainAddress(personId: Long): PersonAddress?
}

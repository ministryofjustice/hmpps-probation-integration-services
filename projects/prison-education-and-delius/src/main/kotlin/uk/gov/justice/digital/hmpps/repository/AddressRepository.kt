package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Address

interface AddressRepository : JpaRepository<Address, Long> {
    @Query(
        """
        select a from Address a
        join fetch a.status
        where a.person.prisonerId = :prisonerId 
        and a.softDeleted = false  
        and a.endDate is null 
        and a.status.code = 'M'
        """
    )
    fun getMainAddressByPrisonerId(prisonerId: String): Address?
}
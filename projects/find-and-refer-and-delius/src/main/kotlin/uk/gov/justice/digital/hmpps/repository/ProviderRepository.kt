package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Provider

interface ProviderRepository : JpaRepository<Provider, Long> {
    @Query(
        """
        select p
        from Provider p 
        where p.selectable = true
        and (p.establishment is null or p.establishment)
        order by p.description
        """
    )
    fun findSelectableProviders(): List<Provider>
}

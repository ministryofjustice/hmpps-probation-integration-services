package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.model.DataQualityStats

interface PersonRepository : JpaRepository<Person, Long> {
    @Query(
        """
        select p from Person p
        join fetch p.manager m
        join fetch m.staff
        join fetch m.team.localAdminUnit.probationDeliveryUnit.provider pa
        where pa.code = :providerCode
        and p.events is not empty
        and not (
            replace(p.mobileNumber, ' ', '') like '07%' 
            and length(replace(p.mobileNumber, ' ', '')) = 11 
            and length(function('translate', p.mobileNumber, ' 0123456789', '')) = 0
        )
        """
    )
    fun getCasesWithInvalidMobileNumber(
        providerCode: String,
        pageable: Pageable
    ): Page<Person>

    @Query(
        """
        select p from Person p
        join fetch p.manager m
        join fetch m.staff
        join fetch m.team.localAdminUnit.probationDeliveryUnit.provider pa
        where pa.code = :providerCode
        and p.events is not empty
        and p.mobileNumber is null
        """
    )
    fun getCasesWithMissingMobileNumber(
        providerCode: String,
        pageable: Pageable
    ): Page<Person>

    @Query(
        """
        select sum(case when p.mobileNumber is null then 1 else 0 end) as missing,
               sum(case when not (
                      replace(p.mobileNumber, ' ', '') like '07%' 
                      and length(replace(p.mobileNumber, ' ', '')) = 11 
                      and length(function('translate', p.mobileNumber, ' 0123456789', '')) = 0
                  ) then 1 else 0 end) as invalid
        from Person p
        where p.manager.team.localAdminUnit.probationDeliveryUnit.provider.code = :providerCode
        and p.events is not empty
        """
    )
    fun getDataQualityStats(providerCode: String): DataQualityStats
}
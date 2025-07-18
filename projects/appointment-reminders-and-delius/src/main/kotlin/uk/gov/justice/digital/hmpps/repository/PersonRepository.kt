package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {
    @Query(
        """
        select p from Person p
        join fetch p.manager
        join fetch p.manager.team
        join fetch p.manager.team.localAdminUnit
        join fetch p.manager.team.localAdminUnit.probationDeliveryUnit
        join fetch p.manager.team.localAdminUnit.probationDeliveryUnit.provider pa
        join fetch p.manager.staff
        left join fetch p.manager.staff.user
        where pa.code = :providerCode
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
        join fetch p.manager
        join fetch p.manager.team
        join fetch p.manager.team.localAdminUnit
        join fetch p.manager.team.localAdminUnit.probationDeliveryUnit
        join fetch p.manager.team.localAdminUnit.probationDeliveryUnit.provider pa
        join fetch p.manager.staff
        left join fetch p.manager.staff.user
        where pa.code = :providerCode
        and p.mobileNumber is null
        """
    )
    fun getCasesWithMissingMobileNumber(
        providerCode: String,
        pageable: Pageable
    ): Page<Person>

    @Query(
        """
        select p
        from Person p
        join fetch p.manager
        join fetch p.manager.team
        join fetch p.manager.team.localAdminUnit
        join fetch p.manager.team.localAdminUnit.probationDeliveryUnit
        join fetch p.manager.team.localAdminUnit.probationDeliveryUnit.provider pa
        join fetch p.manager.staff
        left join fetch p.manager.staff.user
        where pa.code = :providerCode
          and p.mobileNumber is not null
          and replace(p.mobileNumber, ' ', '') in (
            select replace(p2.mobileNumber, ' ', '')
            from Person p2
            where p2.manager.team.localAdminUnit.probationDeliveryUnit.provider.code = :providerCode
              and p2.mobileNumber is not null
            group by replace(p2.mobileNumber, ' ', '')
            having count(p2) > 1
          )
        """
    )
    fun getCasesWithDuplicateMobileNumbers(
        providerCode: String,
        pageable: Pageable
    ): Page<Person>

    @Query(
        """
        select case when count(*) >= 100 then '99+' else to_char(count(*)) end as invalid
        from (
          select 1
          from offender
          join offender_manager on offender.offender_id = offender_manager.offender_id
           and offender_manager.active_flag = 1
           and offender_manager.soft_deleted = 0
          join team on team.team_id = offender_manager.team_id
          join district on district.district_id = team.district_id
          join borough on borough.borough_id = district.borough_id
          join probation_area on probation_area.probation_area_id = borough.probation_area_id
          where offender.soft_deleted = 0
            and offender.mobile_number is not null
            and probation_area.code = :providerCode
            and not (
                  replace(offender.mobile_number, ' ', '') like '07%'
              and length(replace(offender.mobile_number, ' ', '')) = 11
              and length(translate(offender.mobile_number, ' 0123456789', '')) = 0
            )
          fetch first 100 rows only
        )
        """, nativeQuery = true
    )
    fun getInvalidMobileNumberCount(providerCode: String): String
}
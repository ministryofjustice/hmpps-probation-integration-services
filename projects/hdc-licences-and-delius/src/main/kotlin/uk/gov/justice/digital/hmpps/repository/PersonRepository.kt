package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {
    @Query(
        """
            select distinct p.nomsNumber 
            from Person p
            left join p.communityManagers cm
            left join cm.staff cmStaff
            left join p.prisonManagers pm
            left join pm.staff pmStaff
            where ((cmStaff is not null and cmStaff.id = :staffId)
                or (pmStaff is not null and pmStaff.id = :staffId))
            and p.nomsNumber is not null
        """,
    )
    fun findManagedPrisonerIdentifiersByStaffId(staffId: Long): List<String>

    @Query(
        """
            select distinct p.nomsNumber 
            from Person p
            left join p.communityManagers cm
            left join cm.staff cmStaff
            left join p.prisonManagers pm
            left join pm.staff pmStaff
            where ((cmStaff is not null and cmStaff.code = :code)
                or (pmStaff is not null and pmStaff.code = :code))
            and p.nomsNumber is not null
        """,
    )
    fun findManagedPrisonerIdentifiersByStaffCode(code: String): List<String>
}

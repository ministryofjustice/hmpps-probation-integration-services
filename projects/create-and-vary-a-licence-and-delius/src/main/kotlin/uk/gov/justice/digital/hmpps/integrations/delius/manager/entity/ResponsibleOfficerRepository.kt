package uk.gov.justice.digital.hmpps.integrations.delius.manager.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long> {
    @Query("""
        select ro from ResponsibleOfficer ro
        join ro.person p
        join fetch ro.communityManager cm
        join fetch cm.provider
        join fetch cm.team t
        join fetch t.district d
        join fetch d.borough b
        join fetch cm.staff s
        left join fetch s.user
        where p.crn = :crn
        and ro.endDate is null
    """)
    fun findResponsibleOfficer(crn: String): ResponsibleOfficer?
}
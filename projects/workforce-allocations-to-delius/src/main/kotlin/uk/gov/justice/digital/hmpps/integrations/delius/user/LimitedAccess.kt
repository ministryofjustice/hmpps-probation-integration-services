package uk.gov.justice.digital.hmpps.integrations.delius.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.Restriction

interface ExclusionRepository : JpaRepository<Exclusion, Long> {
    @Query("select e from Exclusion e where e.person.id = :personId and (e.end is null or e.end > current_date)")
    fun findByPersonId(personId: Long): List<Exclusion>
}

interface RestrictionRepository : JpaRepository<Restriction, Long> {
    @Query("select r from Restriction r where r.person.id = :personId and (r.end is null or r.end > current_date)")
    fun findByPersonId(personId: Long): List<Restriction>
}
package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Person

interface AccessRepository : JpaRepository<Person, Long> {
    fun existsByCrn(crn: String): Boolean

    @Query(
        """
            select exists (
                select 1 from Person p
                where p.crn = :crn
                /* no restrictions or exclusions */
                  and not exists ( select 1 from Restriction r where r.person.id = p.id and (r.end is null or r.end > current_date) )
                  and not exists ( select 1 from Exclusion e where e.person.id = p.id and (e.end is null or e.end > current_date) )
                /* no suspended cases */
                  and not exists ( select 1 from Registration r where r.personId = p.id and r.type.code = 'PRC' )
                /* only cases with a noncustodial sentence */
                  and exists ( select 1 from Event e where e.personId = p.id and e.disposal.type.sentenceType not in ('SC', 'NC') )
            )
        """
    )
    fun isAllowed(crn: String): Boolean
}

package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.stream.Stream

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(nomsId: String): Person?

    @Query("select p.nomsId from Person p where p.crn = :crn and p.softDeleted = false")
    fun findNomsIdByCrn(crn: String): String?

    @Query(
        """
            select p.noms_number 
            from offender p
            join event e on e.offender_id = p.offender_id and e.active_flag = 1 and e.soft_deleted = 0
            join disposal d on d.event_id = e.event_id and d.active_flag = 1 and d.soft_deleted = 0
            join custody c on c.disposal_id = d.disposal_id and c.prisoner_number is not null and c.soft_deleted = 0
            join r_standard_reference_list cs on cs.standard_reference_list_id = c.custodial_status_id and cs.code_value <> 'P'
            where p.noms_number is not null and p.soft_deleted = 0
            group by p.noms_number
            having count(p.noms_number) = 1
    """, nativeQuery = true
    )
    fun findNomsSingleCustodial(): Stream<String>
}


package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?

    @Modifying
    @Query(
        """
    merge into iaps_offender using dual on (offender_id = ?2) 
    when matched then update set iaps_flag=?1 
    when not matched then insert(offender_id, iaps_flag) values(?2,?1)
    """,
        nativeQuery = true
    )
    fun updateIaps(personId: Long, iapsFlagValue: Long = 1)
}

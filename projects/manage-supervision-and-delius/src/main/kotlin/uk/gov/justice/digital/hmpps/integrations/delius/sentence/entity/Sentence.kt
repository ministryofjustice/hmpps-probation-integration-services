package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event

interface EventSentenceRepository : JpaRepository<Event, Long> {

    @Immutable
    @Entity
    @Table(name = "offender")
    @SQLRestriction("soft_deleted = 0")
    class Person(
        @Id
        @Column(name = "offender_id")
        val id: Long,

        @Column(columnDefinition = "char(7)")
        val crn: String
    )

    @Query(
        "SELECT e FROM Event e " +
            "JOIN Person p ON p.id = e.personId " +
            "LEFT JOIN FETCH e.mainOffence m " +
            "LEFT JOIN FETCH e.additionalOffences ao " +
            "LEFT JOIN FETCH m.offence mo " +
            "LEFT JOIN FETCH ao.offence aoo " +
            "WHERE p.crn = :crn " +
            "AND e.active = true "
    )
    fun findActiveSentencesByCrn(crn: String): List<Event>
}


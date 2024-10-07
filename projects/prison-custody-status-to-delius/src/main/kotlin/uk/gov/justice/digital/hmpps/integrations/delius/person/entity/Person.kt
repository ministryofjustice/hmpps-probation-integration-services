package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

@Immutable
@Entity
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val nomsNumber: String,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsNumberAndSoftDeletedIsFalse(nomsNumber: String): List<Person>
}

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "additional_identifier")
class AdditionalIdentifier(
    @Id
    @Column(name = "additional_identifier_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "identifier_name_id")
    val name: ReferenceData,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)

interface AdditionalIdentifierRepository : JpaRepository<AdditionalIdentifier, Long> {

    @Query(
        """
        select count(ai) > 0 
        from AdditionalIdentifier ai
        where ai.personId = :personId
        and ai.name.code = 'MFCRN'             
        """
    )
    fun personHasBeenMerged(personId: Long): Boolean
}


package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManager
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

    @OneToMany(mappedBy = "person")
    val additionalIdentifier: List<AdditionalIdentifier> = emptyList(),

    @OneToOne(mappedBy = "person")
    val manager: PersonManager? = null,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsNumberAndSoftDeletedIsFalse(nomsNumber: String): List<Person>

    @Query(
        """
        SELECT count(p) FROM Person p 
        LEFT JOIN p.manager
        JOIN p.additionalIdentifier ai
        WHERE ai.id = :id 
        AND p.softDeleted = false
        AND ai.mergeDetail.code = 'MFCRN'             
        """
    )
    fun findByMergedFromCrn(id: Long): Int
}

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "additional_identifier")
class AdditionalIdentifier(
    @Id
    @Column(name = "additional_identifier_id")
    val id: Long,

    @Column(name = "identifier")
    val mergedFromCrn: String,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "identifier_name_id")
    val mergeDetail: ReferenceData
)


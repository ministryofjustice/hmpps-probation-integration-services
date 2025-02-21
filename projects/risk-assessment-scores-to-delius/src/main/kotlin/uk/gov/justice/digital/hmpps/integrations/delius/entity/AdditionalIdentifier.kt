package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class AdditionalIdentifier(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "identifier_name_id")
    val type: ReferenceData,

    @Column(columnDefinition = "varchar2(30)")
    val identifier: String,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Id
    @Column(name = "additional_identifier_id")
    val id: Long,
)

interface AdditionalIdentifierRepository : JpaRepository<AdditionalIdentifier, Long> {
    @Query(
        """
        select ai from AdditionalIdentifier ai
        where ai.personId = :personId
        and ai.type.code = 'MTCRN'
        order by ai.createdDateTime desc
        """
    )
    fun findLatestMergedToCrn(personId: Long): AdditionalIdentifier?
}
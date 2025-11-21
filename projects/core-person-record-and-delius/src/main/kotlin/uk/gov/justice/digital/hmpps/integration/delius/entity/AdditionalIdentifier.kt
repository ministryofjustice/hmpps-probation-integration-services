package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class AdditionalIdentifier(
    @Id
    @Column(name = "additional_identifier_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "identifier_name_id")
    val type: ReferenceData,

    @Column(name = "identifier")
    val value: String,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface AdditionalIdentifierRepository : JpaRepository<AdditionalIdentifier, Long> {
    fun findByPersonId(personId: Long): List<AdditionalIdentifier>
}
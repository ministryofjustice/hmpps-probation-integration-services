package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
@Table(name = "additional_identifier")
@SQLRestriction("soft_deleted = 0")
class AdditionalIdentifier(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "identifier")
    val identifier: String,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "identifier_name_id")
    val type: ReferenceData,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "additional_identifier_id")
    val id: Long,
) {
    companion object {
        val TYPE = AdditionalIdentifier::type.name
        val IDENTIFIER = AdditionalIdentifier::identifier.name
    }
}
package uk.gov.justice.digital.hmpps.integration.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData

@Immutable
@Entity
@Table(name = "r_institution")
class Institution(

    @Column(columnDefinition = "char(6)")
    val code: String,
    val description: String,

    @ManyToOne
    @JoinColumn(name = "establishment_type_id")
    val type: ReferenceData?,

    @Column(name = "institution_name")
    val name: String?,
    val nomisCdeCode: String?,

    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val private: Boolean?,

    @Id
    @Column(name = "institution_id")
    val id: Long
)
package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.io.Serializable

@Immutable
@Entity
@Table(name = "r_institution")
class Institution(
    @EmbeddedId
    val id: InstitutionId,

    @Column(nullable = false, columnDefinition = "char(6)")
    val code: String,

    val description: String,

    val institutionName: String?,

    @ManyToOne
    @JoinColumn(name = "establishment_type_id")
    val establishmentType: ReferenceData?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val private: Boolean?,

    val nomisCdeCode: String?
)

@Embeddable
data class InstitutionId(
    @Column(name = "institution_id")
    val institutionId: Long,

    @Column(name = "establishment")
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean
) : Serializable
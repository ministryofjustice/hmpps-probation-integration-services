package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import java.io.Serializable

@Immutable
@Entity
@Table(name = "r_institution")
@Where(clause = "selectable = 'Y'")
class Institution(
    @EmbeddedId
    val id: InstitutionId,

    @Column(nullable = false, columnDefinition = "char(6)")
    val code: String,

    @Column
    val nomisCdeCode: String,

    @Column(nullable = false)
    val description: String,

    @Column
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @OneToOne(mappedBy = "institution")
    val probationArea: ProbationArea? = null,
)

@Embeddable
data class InstitutionId(
    @Column(name = "institution_id")
    val institutionId: Long,

    @Column(name = "establishment")
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,
) : Serializable

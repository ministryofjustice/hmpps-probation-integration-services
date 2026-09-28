package uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.controller.model.Offence

@Entity
@Immutable
@Table(name = "r_offence")
class OffenceWithSa2026Exclusions(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Column
    val description: String,

    @Column(columnDefinition = "char")
    @Convert(converter = YesNoConverter::class)
    val sa26ExcludedOffence: Boolean? = null,
) {
    fun toModel() = Offence(
        code = code,
        description = description,
        sentencingAct2026Exclusion = sa26ExcludedOffence == true,
    )
}
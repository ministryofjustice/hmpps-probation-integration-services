package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.controller.model.Offence

@Entity
@Immutable
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Column
    val description: String,

//    @Column(columnDefinition = "char")
//    @Convert(converter = YesNoConverter::class)
//    val sa26ExcludedOffence: Boolean? = null,
) {
    fun toModel() = Offence(
        code = code,
        description = description,
//        sentencingAct2026Exclusion = sa26ExcludedOffence == true,
        sentencingAct2026Exclusion = false,
    )
}
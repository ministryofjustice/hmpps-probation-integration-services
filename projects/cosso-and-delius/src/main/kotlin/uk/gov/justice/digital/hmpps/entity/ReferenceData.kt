package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.model.CodedDescription

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    override val code: String,

    @Column(name = "code_description")
    override val description: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) : CodeAndDescription

fun CodeAndDescription.codedDescription() = CodedDescription(code, description)
fun List<CodeAndDescription>.codedDescriptions() =
    map(CodeAndDescription::codedDescription).sortedBy { it.description.lowercase() }
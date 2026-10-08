package uk.gov.justice.digital.hmpps.entity.event.sentence

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column
    val description: String,

    @Column(name = "youth_sentence")
    @Convert(converter = YesNoConverter::class)
    val youthSentence: Boolean = false,
)
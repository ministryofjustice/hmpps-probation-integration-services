package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Immutable
@Entity
@Table(name = "additional_sentence")
@SQLRestriction("soft_deleted = 0")
class AdditionalSentence(

    @Id
    @Column(name = "additional_sentence_id")
    val id: Long,

    @Column(name = "length")
    val length: Long? = null,

    @Column(name = "amount")
    val amount: Long? = null,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "additional_sentence_type_id", nullable = false)
    val type: ReferenceData,

    )
package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal

@Entity
@SQLRestriction("soft_deleted = 0")
class AdditionalSentence(

    @Id
    @Column(name = "additional_sentence_id")
    var id: Long,

    var length: Long? = null,

    var amount: BigDecimal? = null,

    @Column(columnDefinition = "clob")
    var notes: String? = null,

    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "additional_sentence_type_id")
    var type: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "units_id")
    var units: ReferenceData?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface AdditionalSentenceRepository : JpaRepository<AdditionalSentence, Long> {
    fun findAllByEventId(id: Long): List<AdditionalSentence>
}
package uk.gov.justice.digital.hmpps.entity.sentence.custody

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class KeyDate(
    @Id
    @Column(name = "key_date_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) {
    companion object {
        const val POST_SENTENCE_SUPERVISION_END_DATE = "PSSED"
        const val LICENCE_EXPIRY_DATE = "LED"
        const val PROBATION_RESET_DATE = "PR1"
    }
}
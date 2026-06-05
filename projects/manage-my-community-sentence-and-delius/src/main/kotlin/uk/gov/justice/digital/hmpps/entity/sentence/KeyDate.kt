package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "key_date")
@SQLRestriction("soft_deleted = 0")
class KeyDate(
    @Id
    @Column(name = "key_date_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody? = null,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    val date: LocalDate,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    companion object {
        const val SENTENCE_EXPIRY_DATE = "SED"
    }
}
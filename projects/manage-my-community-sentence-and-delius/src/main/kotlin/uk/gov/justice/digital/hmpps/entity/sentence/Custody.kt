package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal? = null,

    @OneToMany(mappedBy = "custody")
    @SQLRestriction("key_date_type_id in (select t.standard_reference_list_id from r_standard_reference_list t where t.code_value = 'SED')")
    val sentenceExpiryDates: List<KeyDate> = emptyList(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    fun sentenceExpiryDateValue() = sentenceExpiryDates.maxOfOrNull { it.date }
}



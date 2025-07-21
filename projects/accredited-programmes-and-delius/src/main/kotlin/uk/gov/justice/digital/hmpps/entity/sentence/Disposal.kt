package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Custody
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "disposal_date")
    val date: LocalDate,

    @Column(name = "entry_length")
    val length: Long?,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val lengthUnits: ReferenceData?,

    @Column(name = "notional_end_date")
    val expectedEndDate: LocalDate,

    @Column(name = "entered_notional_end_date")
    val enteredExpectedEndDate: LocalDate?,

    @OneToMany(mappedBy = "disposal")
    val licenceConditions: List<LicenceCondition>,

    @OneToMany(mappedBy = "disposal")
    val requirements: List<Requirement>,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
) {
    fun description() = "${type.description}${lengthString()?.let { " ($it)" }.orEmpty()}"
    fun lengthString() =
        length?.let { "$length ${checkNotNull(lengthUnits?.description) { "Sentence has length without units" }}" }

    fun expectedEndDate() = enteredExpectedEndDate ?: expectedEndDate
}
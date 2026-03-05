package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.sentence.licencecondition.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement
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
    @Column(name = "notional_end_date")
    val expectedEndDate: LocalDate,
    @Column(name = "entered_notional_end_date")
    val enteredExpectedEndDate: LocalDate?,
    @OneToMany(mappedBy = "disposal")
    val licenceConditions: List<LicenceCondition> = emptyList(),
    @OneToMany(mappedBy = "disposal")
    val requirements: List<Requirement> = emptyList(),
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean = true,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)
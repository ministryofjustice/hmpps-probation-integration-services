package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Column(name = "notional_end_date")
    val notionalEndDate: LocalDate?,

    @Column(name = "entered_notional_end_date")
    val enteredNotionalEndDate: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", nullable = false)
    val disposalType: DisposalType,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

@Entity
@Table(name = "R_DISPOSAL_TYPE")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "disposal_type_code", nullable = false)
    val code: String,

    @Column(name = "description", nullable = false)
    val description: String
)
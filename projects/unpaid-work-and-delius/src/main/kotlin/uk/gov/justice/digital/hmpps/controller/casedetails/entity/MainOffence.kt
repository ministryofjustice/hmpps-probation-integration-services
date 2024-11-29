package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "offender_id", nullable = false)
    val offenderId: Long,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "event_number", nullable = false)
    val eventNumber: String,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence? = null,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "disposal")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    val disposalDate: LocalDate,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "main_offence")
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,
    @Column(name = "main_category_code", columnDefinition = "char(3)")
    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    @Column(name = "sub_category_code", columnDefinition = "char(2)")
    val subCategoryCode: String,
    val subCategoryDescription: String
)

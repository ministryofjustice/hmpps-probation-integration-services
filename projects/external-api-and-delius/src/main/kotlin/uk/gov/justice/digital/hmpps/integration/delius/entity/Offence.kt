package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
data class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,
    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,
    @Column(name = "offence_date")
    val date: LocalDate,
    @Column(name = "offence_count")
    val count: Int,
    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
data class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,
    @Column(name = "offence_date")
    val date: LocalDate?,
    @Column(name = "offence_count")
    val count: Int?,
    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
@Table(name = "r_offence")
data class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,
    @Column(columnDefinition = "char(5)")
    val code: String,
    @Column
    val description: String,
    @Column(columnDefinition = "char(3)")
    val mainCategoryCode: String,
    @Column
    val mainCategoryDescription: String,
    @Column(columnDefinition = "char(2)")
    val subCategoryCode: String,
    @Column
    val subCategoryDescription: String,
    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_sexual_offence")
    val schedule15SexualOffence: Boolean?,
    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_violent_offence")
    val schedule15ViolentOffence: Boolean?,
)

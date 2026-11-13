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

@Entity
@Table(name = "main_offence")
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @Column(name = "event_id")
    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @OneToOne
    @JoinColumn(
        name = "event_id",
        insertable = false,
        updatable = false
    )
    val event: Event? = null,

    @Column(columnDefinition = "number")
    @Convert(NumericBooleanConverter::class)
    val softDeleted: Boolean
)

@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(name = "main_category_code", columnDefinition = "char(3)")
    val mainCategoryCode: String,

    @Column(name = "sub_category_code", columnDefinition = "char(2)")
    val subCategoryCode: String,

    @Column(name = "main_category_description")
    val mainCategoryDescription: String,

    @Column(name = "sub_category_description")
    val subCategoryDescription: String,
)

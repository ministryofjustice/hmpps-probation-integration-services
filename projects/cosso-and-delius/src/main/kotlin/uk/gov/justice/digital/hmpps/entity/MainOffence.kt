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
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "main_offence")
class MainOffence(
    @Id
    @Column("main_offence_id")
    val id: Long,

    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne
    @JoinColumn(name = "offence_id")
    val offence: OffenceEntity,
)

@Entity
@Table(name = "r_offence")
class OffenceEntity(
    @Id
    val offenceId: Long,

    @Column(name = "main_category_code", columnDefinition = "char(3)")
    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    @Column(name = "sub_category_code", columnDefinition = "char(2)")
    val subCategoryCode: String,
    val subCategoryDescription: String,

    )

interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    fun findByEventId(eventId: Long): MainOffence?
}
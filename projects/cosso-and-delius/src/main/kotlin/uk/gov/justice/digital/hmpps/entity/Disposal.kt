package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,
    val disposalDate: LocalDate,
    val eventId: Long,
    val length: Int?,
    val length2: Int?,
    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val disposalType: DisposalType,
    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val lengthUnits: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "entry_length_2_units_id")
    val length2Units: ReferenceData,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean
)

@Entity
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,
    @Column(name = "description")
    val description: String
)

interface DisposalRepository : JpaRepository<Disposal, Long> {
    fun findFirstByEventIdOrderByDisposalDate(eventId: Long): Disposal?
}
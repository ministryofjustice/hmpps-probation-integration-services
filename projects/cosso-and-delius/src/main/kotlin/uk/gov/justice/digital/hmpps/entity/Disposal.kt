package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
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
    @Column(name = "length_2")
    val length2: Int? = null,
    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val disposalType: DisposalType,
    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val lengthUnits: ReferenceData? = null,
    @ManyToOne
    @JoinColumn(name = "entry_length_2_units_id")
    val length2Units: ReferenceData? = null,

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
    val description: String,
    @Column(name = "disposal_type_code")
    val code: String,
)

enum class SuspendedSentenceCode(val code: String) {
    ORA("330"),
    CJA("203"),
    SA2020("341");

    companion object {
        val ssoCodes = entries.map { it.code }.toSet()
    }
}

val DisposalType.isSuspendedSentenceOrder: Boolean get() = code in SuspendedSentenceCode.ssoCodes

interface DisposalRepository : JpaRepository<Disposal, Long> {
    fun findByEventId(eventId: Long): List<Disposal>
    fun findFirstByEventIdOrderByDisposalDate(eventId: Long): Disposal?
}

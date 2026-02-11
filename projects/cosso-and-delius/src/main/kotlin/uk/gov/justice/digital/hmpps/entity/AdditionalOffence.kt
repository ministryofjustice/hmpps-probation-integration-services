package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,
    val offenceDate: LocalDate,
    val eventId: Long,
    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: OffenceEntity,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long> {
    fun findAllByEventId(eventId: Long): List<AdditionalOffence>
}
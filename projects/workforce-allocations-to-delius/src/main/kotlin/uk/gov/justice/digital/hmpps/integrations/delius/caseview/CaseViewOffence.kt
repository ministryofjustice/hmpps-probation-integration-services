package uk.gov.justice.digital.hmpps.integrations.delius.caseview

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "main_offence")
@SQLRestriction("soft_deleted = 0")
class CaseViewMainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @Column(name = "event_id")
    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: CaseViewOffence,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "additional_offence")
@SQLRestriction("soft_deleted = 0")
class CaseViewAdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @Column(name = "event_id")
    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: CaseViewOffence,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_offence")
class CaseViewOffence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    val description: String,
    val mainCategoryDescription: String,
    val subCategoryDescription: String
)

interface CaseViewAdditionalOffenceRepository : JpaRepository<CaseViewAdditionalOffence, Long> {
    @EntityGraph(attributePaths = ["offence"])
    fun findAllByEventId(eventId: Long): List<CaseViewAdditionalOffence>
}

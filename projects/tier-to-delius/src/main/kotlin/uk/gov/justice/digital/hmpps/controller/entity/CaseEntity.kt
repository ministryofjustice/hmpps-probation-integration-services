package uk.gov.justice.digital.hmpps.controller.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "offender")
class CaseEntity(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val crn: String,
    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "current_tier")
    val tier: ReferenceData?,
    @Column(name = "dynamic_rsr_score", columnDefinition = "number(5,2)")
    val dynamicRsrScore: Double?,
    val createdByUserId: Long = 0,
    val lastUpdatedUserId: Long = 0,
    val createdDatetime: ZonedDateTime = ZonedDateTime.now(),
    val lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,
)

interface CaseEntityRepository : JpaRepository<CaseEntity, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): CaseEntity?
}

fun CaseEntityRepository.getCase(crn: String): CaseEntity =
    findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)

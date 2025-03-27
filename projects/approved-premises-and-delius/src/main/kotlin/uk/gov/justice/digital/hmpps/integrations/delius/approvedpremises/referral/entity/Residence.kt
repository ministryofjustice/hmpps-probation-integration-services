package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "approved_premises_residence")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "ap_residence_id_seq", sequenceName = "ap_residence_id_seq", allocationSize = 1)
class Residence(

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "approved_premises_referral_id")
    val referralId: Long,

    @Column(name = "approved_premises_id")
    var approvedPremisesId: Long,

    var arrivalDate: ZonedDateTime,

    var expectedDepartureDate: LocalDate?,

    @Lob
    val arrivalNotes: String?,

    val keyWorkerStaffId: Long?
) {
    var departureDate: ZonedDateTime? = null
    var departureReasonId: Long? = null
    var moveOnCategoryId: Long? = null

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ap_residence_id_seq")
    @Column(name = "approved_premises_residence_id")
    val id: Long = 0

    @Version
    var rowVersion: Long = 0

    @CreatedDate
    @Column
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @Column
    @CreatedBy
    var createdByUserId: Long = 0

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    @Column
    val partitionAreaId: Long = 0
}

@Entity
@Immutable
@Table(name = "r_move_on_category")
class MoveOnCategory(
    @Id
    @Column(name = "move_on_category_id")
    val id: Long,

    val code: String
)

interface ResidenceRepository : JpaRepository<Residence, Long> {
    fun findByReferralId(referralId: Long): Residence?
}

interface MoveOnCategoryRepository : JpaRepository<MoveOnCategory, Long> {
    fun findByCode(code: String): MoveOnCategory?
}

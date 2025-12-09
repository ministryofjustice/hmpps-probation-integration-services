package uk.gov.justice.digital.hmpps.appointments.domain.contact

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "enforcement")
@SQLRestriction("soft_deleted = 0")
open class Enforcement(
    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val action: EnforcementAction?,

    val responseDate: ZonedDateTime?,

    val actionTakenDate: ZonedDateTime = ZonedDateTime.now(),
    val actionTakenTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "enforcement_id_seq")
    @Column(name = "enforcement_id")
    val id: Long = 0,
) {
    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0

    @Column(name = "row_version")
    @Version
    val version: Long = 0

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
}

@Immutable
@Entity
@Table(name = "r_enforcement_action")
open class EnforcementAction(
    val code: String,
    val description: String,
    val responseByPeriod: Long?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Id
    @Column(name = "enforcement_action_id")
    val id: Long = 0
) {
    enum class Code(val value: String) {
        REFER_TO_PERSON_MANAGER("ROM")
    }
}

interface EnforcementActionRepository : JpaRepository<EnforcementAction, Long> {
    fun findByCode(code: String): EnforcementAction?
}

fun EnforcementActionRepository.getEnforcementActionByCode(code: String): EnforcementAction =
    findByCode(code) ?: throw NotFoundException("EnforcementAction", "code", code)

interface EnforcementRepository : JpaRepository<Enforcement, Long> {
    fun findByContactId(appointmentId: Long): Enforcement?
}
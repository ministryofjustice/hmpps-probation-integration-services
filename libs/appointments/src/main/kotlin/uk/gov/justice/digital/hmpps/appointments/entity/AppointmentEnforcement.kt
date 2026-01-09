package uk.gov.justice.digital.hmpps.appointments.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "enforcement")
@SQLRestriction("soft_deleted = 0")
open class AppointmentEnforcement(
    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: AppointmentContact,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val action: AppointmentEntities.EnforcementAction?,

    val responseDate: ZonedDateTime?,

    val actionTakenDate: ZonedDateTime = ZonedDateTime.now(),
    val actionTakenTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @GeneratedId(generator = "enforcement_id_seq")
    @Column(name = "enforcement_id")
    val id: Long = 0,
) {
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
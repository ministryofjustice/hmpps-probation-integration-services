package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(
    @Id
    @Column(name = "contact_id", updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long,
    @Column(name = "offender_id", updatable = false)
    val personId: Long,
    @Column(name = "contact_date")
    val date: ZonedDateTime = ZonedDateTime.now(),
    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime = ZonedDateTime.now(),
    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: ContactType,
    @Lob
    val notes: String,
    @Column(name = "probation_area_id", updatable = false)
    val providerId: Long,
    @Column(updatable = false)
    val teamId: Long,
    @Column(updatable = false)
    val staffId: Long,
    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id", updatable = false)
    val outcome: ContactOutcome? = null,
    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean = false,
    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    val alert: Boolean? = false,
    @Column(name = "soft_deleted", columnDefinition = "number")
    var softDeleted: Boolean = false,
    @Column(name = "row_version")
    @Version
    var version: Long = 0,
    @Column
    val partitionAreaId: Long = 0,
    @Column(name = "created_datetime")
    @CreatedDate
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),
    @Column(name = "created_by_user_id")
    @CreatedBy
    var createdUserId: Long = 0,
    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastModifiedUserId: Long = 0,
    @Column(name = "last_updated_datetime")
    @LastModifiedDate
    var lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,
    val code: String,
) {
    companion object {
        const val MANAGEMENT_OVERSIGHT_RECALL = "MO5"
    }
}

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
    val code: String,
) {
    companion object {
        const val DECISION_TO_RECALL = "MO10"
        const val DECISION_NOT_TO_RECALL = "MO11"
    }
}

package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
class Enforcement(
    @Id
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @GeneratedId(generator = "enforcement_id_seq")
    @Column(name = "enforcement_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val action: EnforcementAction? = null,

    @Column(name = "response_date")
    val responseDate: ZonedDateTime?,

    @Column(name = "action_taken_date")
    val actionTakenDate: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "action_taken_time")
    val actionTakenTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long = 0,
)

@Entity
@Immutable
@Table(name = "r_enforcement_action")
class EnforcementAction(
    @Id
    @Column(name = "enforcement_action_id")
    val id: Long = 0,

    val code: String,
    val description: String,
    val responseByPeriod: Long?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val contactType: ContactType,
)
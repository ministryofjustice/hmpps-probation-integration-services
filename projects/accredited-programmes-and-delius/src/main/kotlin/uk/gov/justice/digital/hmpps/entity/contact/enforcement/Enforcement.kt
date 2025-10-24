package uk.gov.justice.digital.hmpps.entity.contact.enforcement

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
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
class Enforcement(
    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    val responseDate: ZonedDateTime? = null,
    val actionTakenDate: ZonedDateTime = ZonedDateTime.now(),
    val actionTakenTime: ZonedDateTime = ZonedDateTime.now(),
    val partitionAreaId: Long = 0,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val action: EnforcementAction? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "enforcement_id_seq")
    @Column(name = "enforcement_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
)

@Entity
@Immutable
@Table(name = "r_enforcement_action")
class EnforcementAction(
    val code: String,
    val description: String,
    val responseByPeriod: Long?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val contactType: ContactType,

    @Id
    @Column(name = "enforcement_action_id")
    val id: Long = 0
) {
    companion object {
        const val REFER_TO_PERSON_MANAGER = "ROM"
    }
}
package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
class Contact(
    @ManyToOne
    @JoinColumn("offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @ManyToOne
    @JoinColumn("probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn("team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn("staff_id")
    val staff: Staff,

    val description: String,

    @Lob
    val notes: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "contact_id", updatable = false)
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long,
) {
    @Column(name = "alert_active")
    @Convert(converter = YesNoConverter::class)
    val alert: Boolean = true

    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean = false

    @Version
    @Column(name = "row_version")
    val version: Long = 0

    @Column
    val partitionAreaId: Long = 0L

    @Column
    val trustProviderTeamId: Long = team.id

    @Column(columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false

    @CreatedDate
    @Column(name = "created_datetime", updatable = false)
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    var createdByUserId: Long = 0

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    val code: String,

    @Id
    @Column(name = "contact_type_id")
    val id: Long,
) {
    companion object {
        const val E_SUPERVISION_CHECK_IN = "ESPCHI"
    }
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String): ContactType =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

interface ContactRepository : JpaRepository<Contact, Long>
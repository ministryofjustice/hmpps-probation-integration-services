package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(
    @Id
    @GeneratedId(generator = "contact_id_generator")
    @Column(name = "contact_id")
    val id: Long? = null,

    @Column(name = "contact_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime?,

    @Column(name = "provider_employee_id")
    val providerEmployeeId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team? = null,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(name = "visor_exported")
    @Convert(converter = YesNoConverter::class)
    val visorExported: Boolean? = false,

    @Column
    val partitionAreaId: Long = 0L,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    val alert: Boolean? = false,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "attended")
    val attended: Boolean? = false,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "event_id")
    val eventId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column
    val staffEmployeeId: Long,

    @Column
    val probationAreaId: Long? = null,

    @Column
    val trustProviderTeamId: Long? = null
)

interface ContactRepository : JpaRepository<Contact, Long>

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    @Column
    val code: String
)

enum class ContactTypeCode(val code: String) {
    COURT_APPEARANCE("EAPP")
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Contact", "code", code)
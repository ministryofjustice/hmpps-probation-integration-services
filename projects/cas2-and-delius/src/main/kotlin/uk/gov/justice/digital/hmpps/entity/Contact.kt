package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
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

@Entity
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class Contact(
    @Id
    @Column(name = "contact_id")
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,

    @[Version Column(name = "row_version")]
    val version: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @[ManyToOne JoinColumn(name = "contact_type_id")]
    val type: ContactType,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @Column
    val staffId: Long,

    @Column
    val teamId: Long,

    @Column
    val probationAreaId: Long,

    @Lob
    val notes: String,

    @Column
    val externalReference: String,

    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean = type.sensitive,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    // The following fields are not used, but must be set:
    val partitionAreaId: Long = 0,

    val trustProviderTeamId: Long = teamId,

    @Column(columnDefinition = "number")
    val trustProviderFlag: Boolean = false,
)

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    @[Id Column(name = "contact_type_id")]
    val id: Long,

    val code: String,

    @Column(name = "sensitive_contact")
    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean = false
) {
    companion object {
        const val REFERRAL_SUBMITTED = "EACB"
        const val REFERRAL_UPDATED = "EACC"
    }
}

interface ContactRepository : JpaRepository<Contact, Long> {
    fun existsByExternalReference(externalReference: String): Boolean
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Contact type", "code", code)

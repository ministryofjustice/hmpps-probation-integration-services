package uk.gov.justice.digital.hmpps.integrations.delius.entity

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
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
class Contact(
    @Id
    @Column(name = "contact_id", updatable = false)
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,

    val externalReference: String,

    @Column(updatable = false)
    val offenderId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: ContactType,

    @Lob
    var notes: String,

    @Column(name = "contact_date")
    var date: LocalDate,

    @Column(name = "contact_start_time")
    var startTime: ZonedDateTime,

    @Column(updatable = false)
    val staffId: Long,

    @Column(updatable = false)
    val teamId: Long,

    @Column(updatable = false)
    val probationAreaId: Long,

    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean = type.isSensitive,

    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_datetime")
    val lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column(updatable = false)
    val trustProviderTeamId: Long = teamId,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val trustProviderFlag: Boolean = false,

    @Column(updatable = false)
    val partitionAreaId: Long = 0L,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String,

    @Column(name = "sensitive_contact")
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean
) {
    companion object {
        const val REFERRAL_SUBMITTED = "EARS"
        const val BOOKING_CANCELLED = "EACA"
        const val BOOKING_CONFIRMED = "EACO"
        const val BOOKING_PROVISIONAL = "EABP"
        const val PERSON_ARRIVED = "EAAR"
        const val PERSON_DEPARTED = "EADP"
    }
}

interface ContactRepository : JpaRepository<Contact, Long> {
    fun getByExternalReference(externalReference: String): Contact?
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

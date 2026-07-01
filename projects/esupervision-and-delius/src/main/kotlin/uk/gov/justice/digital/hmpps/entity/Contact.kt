package uk.gov.justice.digital.hmpps.entity

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
import uk.gov.justice.digital.hmpps.entity.event.EventEntity
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_EXPIRED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_RECEIVED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_REVIEWED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_UPDATED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.SENTENCE_TERMINATED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.SETUP_COMPLETED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.SETUP_REMOVED
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "contact")
@EntityListeners(AuditingEntityListener::class)
class Contact(
    @ManyToOne
    @JoinColumn("offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var outcome: ContactOutcome? = null,

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

    val description: String? = null,

    @Lob
    var notes: String? = null,

    val externalReference: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "contact_id", updatable = false)
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedId(generator = "contact_id_seq")
    val id: Long = 0,

    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    var isSensitive: Boolean? = false
) {

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

    companion object {
        fun externalReferencePrefix(eventType: String): String = when (eventType) {
            CHECK_IN_RECEIVED, CHECK_IN_REVIEWED, CHECK_IN_UPDATED -> "urn:uk:gov:hmpps:esupervision:check-in:"
            CHECK_IN_EXPIRED -> "urn:uk:gov:hmpps:esupervision:check-in-expiry:"
            SETUP_COMPLETED, SETUP_REMOVED, SENTENCE_TERMINATED -> "urn:uk:gov:hmpps:esupervision:setup:"
            else -> throw IllegalArgumentException("Unexpected event type: $eventType")
        }

        val checkInExternalReferencePrefixes =
            listOf("urn:uk:gov:hmpps:esupervision:check-in:", "urn:uk:gov:hmpps:esupervision:check-in-expiry:")
    }
}

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
        const val E_SUPERVISION_CHECK_IN = "ESPCHI"
        const val E_SUPERVISION_SETUP_COMPLETED = "ESPCHS"
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
        const val SETUP_COMPLETED = "ESPSC"
        const val SETUP_REMOVED = "ESPRD"
        const val MANUAL_STOP = "ESPMP"
        const val NO_ACTIVE_EVENTS = "ESPNA"
        const val IN_RESET = "ESPRS"
    }
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
    fun getByCode(code: String) = findByCode(code).orNotFoundBy("code", code)
}

interface ContactOutcomeRepository : JpaRepository<ContactOutcome, Long> {
    fun findByCode(code: String): ContactOutcome?
    fun getByCode(code: String) = findByCode(code).orNotFoundBy("code", code)
}

interface ContactRepository : JpaRepository<Contact, Long> {
    fun findByPersonCrnAndEventNumberAndTypeCode(
        crn: String,
        eventNumber: String,
        typeCode: String = ContactType.E_SUPERVISION_SETUP_COMPLETED
    ): Contact?

    fun findByExternalReference(externalReference: String): Contact?
    fun findByExternalReferenceIn(externalReference: List<String>): Contact?
    fun getByExternalReferenceIn(externalReference: List<String>): Contact =
        findByExternalReferenceIn(externalReference).orNotFoundBy("externalReference", externalReference)
}

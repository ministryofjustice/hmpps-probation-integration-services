package uk.gov.justice.digital.hmpps.appointments.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.ZonedDateTime

object AppointmentEntities {
    @Immutable
    @Entity
    @Table(name = "r_contact_type")
    // TODO restrict to attendance contacts?
    open class AppointmentType(
        @Id
        @Column(name = "contact_type_id")
        val id: Long = 0,

        override val code: String,
    ) : CodedReferenceData {
        enum class Code(val value: String) {
            REVIEW_ENFORCEMENT_STATUS("ARWS"),
        }
    }

    @Entity
    @Table(name = "event")
    @SQLRestriction("soft_deleted = 0")
    @EntityListeners(AuditingEntityListener::class)
    open class Event(
        @Id
        @Column(name = "event_id")
        val id: Long = 0,

        @OneToOne(mappedBy = "event")
        val disposal: Disposal?,

        var ftcCount: Long?,
        val breachEnd: LocalDate?,

        @CreatedDate
        var createdDatetime: ZonedDateTime? = ZonedDateTime.now(),

        @CreatedBy
        var createdByUserId: Long? = 0,

        @LastModifiedDate
        var lastUpdatedDatetime: ZonedDateTime? = ZonedDateTime.now(),

        @LastModifiedBy
        var lastUpdatedUserId: Long? = 0,

        @Column(columnDefinition = "number")
        @Convert(converter = NumericBooleanConverter::class)
        val softDeleted: Boolean = false,
    )

    @Entity
    @Immutable
    @Table(name = "disposal")
    @SQLRestriction("soft_deleted = 0")
    open class Disposal(
        @Id
        @Column(name = "disposal_id")
        val id: Long = 0,

        @OneToOne
        @JoinColumn(name = "event_id")
        val event: Event,

        @ManyToOne
        @JoinColumn(name = "disposal_type_id")
        val type: DisposalType,

        @Column(name = "disposal_date")
        val date: LocalDate,

        @Column(columnDefinition = "number")
        @Convert(converter = NumericBooleanConverter::class)
        val softDeleted: Boolean = false,
    )

    @Entity
    @Immutable
    @Table(name = "r_disposal_type")
    open class DisposalType(
        @Id
        @Column(name = "disposal_type_id")
        val id: Long = 0,

        @Column(name = "disposal_type_code")
        override val code: String,

        @Column
        val sentenceType: String? = null,

        @Column
        val ftcLimit: Long? = null,
    ) : CodedReferenceData

    @Immutable
    @Entity
    @Table(name = "offender")
    @SQLRestriction("soft_deleted = 0")
    open class Person(
        @Column(columnDefinition = "char(7)")
        val crn: String,

        @OneToOne(mappedBy = "person")
        val manager: PersonManager,

        @Id
        @Column(name = "offender_id")
        val id: Long
    )

    @Entity
    @Immutable
    @Table(name = "offender_manager")
    @SQLRestriction("active_flag = 1 and soft_deleted = 0")
    open class PersonManager(
        @Id
        @Column(name = "offender_manager_id")
        val id: Long = 0,

        @OneToOne
        @JoinColumn(name = "offender_id")
        val person: Person,

        @ManyToOne
        @JoinColumn(name = "team_id")
        val team: Team,

        @ManyToOne
        @JoinColumn(name = "allocation_staff_id")
        val staff: Staff,

        @Column(columnDefinition = "number", nullable = false)
        @Convert(converter = NumericBooleanConverter::class)
        val softDeleted: Boolean = false,

        @Column(name = "active_flag", columnDefinition = "number", nullable = false)
        @Convert(converter = NumericBooleanConverter::class)
        val active: Boolean = true,
    )

    @Entity
    @Immutable
    @Table(name = "probation_area")
    open class Provider(
        @Id
        @Column(name = "probation_area_id")
        val id: Long = 0,

        @Column(name = "code", columnDefinition = "char(3)")
        override val code: String,
    ) : CodedReferenceData

    @Entity
    @Immutable
    @Table(name = "team")
    open class Team(
        @Id
        @Column(name = "team_id")
        val id: Long = 0,

        @Column(name = "code", columnDefinition = "char(6)")
        override val code: String,

        val description: String,

        @ManyToOne
        @JoinColumn(name = "probation_area_id")
        val provider: Provider,
    ) : CodedReferenceData

    @Immutable
    @Entity
    @Table(name = "office_location")
    @SQLRestriction("end_date is null or end_date > current_date")
    open class OfficeLocation(
        @Id
        @Column(name = "office_location_id")
        val id: Long = 0,

        @Column(name = "code", columnDefinition = "char(7)")
        override val code: String,

        val endDate: LocalDate?,
    ) : CodedReferenceData

    @Immutable
    @Entity
    @Table(name = "staff")
    open class Staff(
        @Id
        @Column(name = "staff_id")
        val id: Long = 0,

        @Column(name = "officer_code", columnDefinition = "char(7)")
        override val code: String,
    ) : CodedReferenceData

    @Entity
    @Immutable
    @Table(name = "r_enforcement_action")
    open class EnforcementAction(
        @Id
        @Column(name = "enforcement_action_id")
        val id: Long = 0,

        override val code: String,

        @ManyToOne
        @JoinColumn(name = "contact_type_id")
        @Fetch(FetchMode.JOIN)
        val type: AppointmentEntities.AppointmentType,
    ) : CodedReferenceData {
        companion object {
            const val REFER_TO_PERSON_MANAGER = "ROM"
        }
    }

    interface CodedReferenceData {
        val code: String
    }
}
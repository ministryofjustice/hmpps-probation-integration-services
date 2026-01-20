package uk.gov.justice.digital.hmpps.appointments.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate
import java.time.ZonedDateTime

internal object AppointmentEntities {
    @Entity(name = "AppointmentContact")
    @EntityListeners(AuditingEntityListener::class)
    @Table(name = "contact")
    @SQLRestriction("soft_deleted = 0")
    @SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
    @NamedEntityGraph(
        name = "AppointmentContact.all",
        includeAllAttributes = true,
        attributeNodes = [NamedAttributeNode("event", subgraph = "Event")],
        subgraphs = [
            NamedSubgraph(
                name = "Event",
                attributeNodes = [NamedAttributeNode(value = "disposal", subgraph = "Disposal")]
            ),
            NamedSubgraph(name = "Disposal", attributeNodes = [NamedAttributeNode(value = "type")]),
        ]
    )
    class AppointmentContact(
        @Id
        @GeneratedId(generator = "contact_id_generator")
        @Column(name = "contact_id")
        val id: Long? = null,

        @Version
        @Column(name = "row_version")
        val version: Long = 0,

        val externalReference: String? = null,

        // Relates to
        @Column(name = "offender_id")
        val personId: Long,

        @ManyToOne
        @JoinColumn(name = "offender_id", insertable = false, updatable = false)
        @Fetch(FetchMode.JOIN)
        val person: Person? = null,

        @Column(name = "event_id")
        val eventId: Long? = null,

        @ManyToOne
        @JoinColumn(name = "event_id", insertable = false, updatable = false)
        val event: Event? = null,

        @Column(name = "rqmnt_id")
        val requirementId: Long? = null,

        @Column(name = "lic_condition_id")
        val licenceConditionId: Long? = null,

        @Column(name = "nsi_id")
        val nsiId: Long? = null,

        @Column(name = "pss_rqmnt_id")
        val pssRequirementId: Long? = null,

        // When
        @Column(name = "contact_date")
        var date: LocalDate,

        @Column(name = "contact_start_time")
        var startTime: ZonedDateTime,

        @Column(name = "contact_end_time")
        var endTime: ZonedDateTime? = null,

        // Who
        @ManyToOne
        @JoinColumn(name = "staff_id")
        var staff: Staff,

        @ManyToOne
        @JoinColumn(name = "team_id")
        var team: Team,

        @ManyToOne
        @JoinColumn(name = "probation_area_id")
        var provider: Provider = team.provider,

        // Where
        @ManyToOne
        @JoinColumn(name = "office_location_id")
        var officeLocation: OfficeLocation?,

        // What
        @ManyToOne
        @JoinColumn(name = "contact_type_id")
        val type: Type,

        @ManyToOne
        @JoinColumn(name = "contact_outcome_type_id")
        var outcome: AppointmentOutcome? = null,

        @Convert(converter = YesNoConverter::class)
        var attended: Boolean? = null,

        @Convert(converter = YesNoConverter::class)
        var complied: Boolean? = null,

        @Column(name = "latest_enforcement_action_id")
        var enforcementActionId: Long? = null,

        var enforcement: Boolean? = null,

        var linkedContactId: Long? = null,

        @Lob
        var notes: String? = null,

        // Flags
        @Convert(converter = YesNoConverter::class)
        @Column(name = "alert_active")
        var alert: Boolean? = null,

        @Convert(converter = YesNoConverter::class)
        var sensitive: Boolean? = null,

        @Convert(converter = YesNoConverter::class)
        var rarActivity: Boolean? = null,

        @Convert(converter = YesNoConverter::class)
        var visorContact: Boolean? = null,

        @Convert(converter = YesNoConverter::class)
        var visorExported: Boolean? = null,

        @Column(columnDefinition = "number")
        @Convert(converter = NumericBooleanConverter::class)
        val softDeleted: Boolean = false,

        // Audit
        @CreatedDate
        @Column(name = "created_datetime")
        var createdDatetime: ZonedDateTime? = ZonedDateTime.now(),

        @LastModifiedDate
        @Column(name = "last_updated_datetime")
        var lastUpdatedDatetime: ZonedDateTime? = ZonedDateTime.now(),

        @CreatedBy
        @Column(name = "created_by_user_id")
        var createdByUserId: Long? = null,

        @LastModifiedBy
        @Column(name = "last_updated_user_id")
        var lastUpdatedUserId: Long? = null,

        // The following fields are not used but must be set:
        val partitionAreaId: Long = 0,

        val trustProviderTeamId: Long = team.id,

        @Column(columnDefinition = "number")
        @Convert(converter = NumericBooleanConverter::class)
        val trustProviderFlag: Boolean = false,
    )

    @Immutable
    @Entity
    @Table(name = "r_contact_type")
    class Type(
        @Id
        @Column(name = "contact_type_id")
        val id: Long = 0,

        override val code: String,

        @Column(name = "attendance_contact")
        @Convert(converter = YesNoConverter::class)
        val attendance: Boolean? = null,

        @Column(name = "national_standards_contact")
        @Convert(converter = YesNoConverter::class)
        val nationalStandards: Boolean? = null,
    ) : CodedReferenceData {
        companion object {
            const val REVIEW_ENFORCEMENT_STATUS = "ARWS"
        }
    }

    @Entity
    @Immutable
    @Table(name = "r_contact_outcome_type")
    class AppointmentOutcome(
        @Id
        @Column(name = "contact_outcome_type_id")
        val id: Long,

        @Column(name = "code")
        override val code: String,

        @Column(name = "description")
        val description: String,

        @Column(name = "outcome_attendance")
        @Convert(converter = YesNoConverter::class)
        val attended: Boolean?,

        @Column(name = "outcome_compliant_acceptable")
        @Convert(converter = YesNoConverter::class)
        val complied: Boolean?,

        @Convert(converter = YesNoConverter::class)
        val enforceable: Boolean?,
    ) : CodedReferenceData

    @Entity
    @Table(name = "event")
    @SQLRestriction("soft_deleted = 0")
    @EntityListeners(AuditingEntityListener::class)
    class Event(
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
    class Disposal(
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
    class DisposalType(
        @Id
        @Column(name = "disposal_type_id")
        val id: Long = 0,

        @Column(name = "disposal_type_code")
        override val code: String,

        @Column
        val ftcLimit: Long? = null,
    ) : CodedReferenceData

    @Immutable
    @Entity
    @Table(name = "offender")
    class Person(
        @Id
        @Column(name = "offender_id")
        val id: Long,

        @Column(columnDefinition = "char(7)")
        val crn: String,
    )

    @Entity
    @Immutable
    @Table(name = "probation_area")
    class Provider(
        @Id
        @Column(name = "probation_area_id")
        val id: Long = 0,

        @Column(name = "code", columnDefinition = "char(3)")
        override val code: String,
    ) : CodedReferenceData

    @Entity
    @Immutable
    @Table(name = "team")
    class Team(
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
    class OfficeLocation(
        @Id
        @Column(name = "office_location_id")
        val id: Long = 0,

        @Column(name = "code", columnDefinition = "char(7)")
        override val code: String,

        val endDate: LocalDate? = null,
    ) : CodedReferenceData

    @Immutable
    @Entity
    @Table(name = "staff")
    class Staff(
        @Id
        @Column(name = "staff_id")
        val id: Long = 0,

        @Column(name = "officer_code", columnDefinition = "char(7)")
        override val code: String,
    ) : CodedReferenceData

    @Entity
    @Table(name = "enforcement")
    @SQLRestriction("soft_deleted = 0")
    @EntityListeners(AuditingEntityListener::class)
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    class Enforcement(
        @Id
        @GeneratedId(generator = "enforcement_id_seq")
        @Column(name = "enforcement_id")
        val id: Long = 0,

        @Column(name = "row_version")
        @Version
        val version: Long = 0,

        @ManyToOne
        @JoinColumn(name = "contact_id")
        val contact: AppointmentContact,

        @ManyToOne
        @JoinColumn(name = "enforcement_action_id")
        val action: EnforcementAction?,

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

        override val code: String,
        val description: String,
        val responseByPeriod: Long?,

        @ManyToOne
        @JoinColumn(name = "contact_type_id")
        val type: Type,
    ) : CodedReferenceData {
        companion object {
            const val REFER_TO_PERSON_MANAGER = "ROM"
        }
    }

    @Entity
    @Table(name = "contact_alert")
    @SequenceGenerator(name = "contact_alert_id_generator", sequenceName = "contact_alert_id_seq", allocationSize = 1)
    class Alert(
        @Id
        @Column(name = "contact_alert_id")
        @GeneratedId(generator = "contact_alert_id_generator")
        val id: Long = 0,

        @Version
        @Column(name = "row_version")
        val version: Long = 0,

        @Column(name = "offender_id")
        val personId: Long,

        @Column(name = "contact_id")
        val appointmentId: Long?,

        @Column(name = "contact_type_id")
        val appointmentTypeId: Long,

        @Column(name = "offender_manager_id")
        val managerId: Long,

        @Column(name = "trust_provider_team_id")
        val teamId: Long,

        @Column(name = "staff_employee_id")
        val staffId: Long
    )

    @Entity
    @Immutable
    @Table(name = "offender_manager")
    @SQLRestriction("soft_deleted = 0 and active_flag = 1")
    class PersonManager(
        @Id
        @Column(name = "offender_manager_id")
        val id: Long,

        @Column(name = "offender_id")
        val personId: Long,

        @Column(name = "allocation_staff_id")
        val staffId: Long,

        @Column(name = "team_id")
        val teamId: Long,

        @Column(name = "active_flag", columnDefinition = "number")
        @Convert(converter = NumericBooleanConverter::class)
        val active: Boolean = true,

        @Column(columnDefinition = "number")
        @Convert(converter = NumericBooleanConverter::class)
        val softDeleted: Boolean = false
    )

    interface CodedReferenceData {
        val code: String
    }
}
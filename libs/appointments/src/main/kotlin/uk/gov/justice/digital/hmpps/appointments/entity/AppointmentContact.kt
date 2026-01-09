package uk.gov.justice.digital.hmpps.appointments.entity

import jakarta.persistence.*
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

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
open class AppointmentContact(
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
    val person: AppointmentEntities.Person? = null,

    @Column(name = "event_id")
    val eventId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    val event: AppointmentEntities.Event? = null,

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
    var startTime: ZonedDateTime?,

    @Column(name = "contact_end_time")
    var endTime: ZonedDateTime?,

    // Who
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    var provider: AppointmentEntities.Provider,

    @ManyToOne
    @JoinColumn(name = "team_id")
    var team: AppointmentEntities.Team,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: AppointmentEntities.Staff,

    // Where
    @ManyToOne
    @JoinColumn(name = "office_location_id")
    var officeLocation: AppointmentEntities.OfficeLocation?,

    // What
    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: AppointmentEntities.AppointmentType,

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
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long? = null,

    @LastModifiedBy
    var lastUpdatedUserId: Long? = null,

    // The following fields are not used but must be set:
    val partitionAreaId: Long = 0,

    val trustProviderTeamId: Long = team.id,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,
) {
    fun appendNotes(parts: List<String>) =
        appendNotes(*parts.toTypedArray())

    fun appendNotes(vararg extraNotes: String) = apply {
        notes = listOfNotNull(notes, *extraNotes)
            .filter { it.isNotBlank() }
            .joinToString(System.lineSeparator() + System.lineSeparator())
    }

    fun exportToVisor(visor: Boolean?) = apply {
        visorContact = visor
        visorExported = if (visorContact == true && visorExported == null) {
            false
        } else if (visorContact != true) {
            null
        } else {
            visorExported
        }
    }
}
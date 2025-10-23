package uk.gov.justice.digital.hmpps.entity.contact

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.entity.PersonCrn
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
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

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: PersonCrn,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement? = null,

    @ManyToOne
    @JoinColumn(name = "lic_condition_id")
    val licenceCondition: LicenceCondition? = null,

    @Column(name = "contact_date")
    var date: LocalDate,

    @Column(name = "contact_start_time")
    var startTime: ZonedDateTime? = null,

    @Column(name = "contact_end_time")
    var endTime: ZonedDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var outcome: ContactOutcome? = null,

    @Convert(converter = YesNoConverter::class)
    var attended: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    var complied: Boolean? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var enforcement: Boolean? = null,

    @Column(name = "latest_enforcement_action_id")
    var enforcementActionId: Long? = null,

    val linkedContactId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    var location: OfficeLocation? = null,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    var team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    var provider: Provider,

    notes: String? = null,

    @Column
    val externalReference: String? = null,

    @Convert(converter = YesNoConverter::class)
    var sensitive: Boolean = false,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    // The following fields are not used, but must be set:
    val partitionAreaId: Long = 0,

    val trustProviderTeamId: Long = team.id,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,
) {
    @Lob
    var notes: String? = notes
        private set

    fun appendNotes(extraNotes: String) {
        notes = notes + System.lineSeparator() + System.lineSeparator() + extraNotes
    }

    companion object {
        const val REFERENCE_PREFIX = "urn:uk:gov:accredited-programmes:appointment:"
    }
}
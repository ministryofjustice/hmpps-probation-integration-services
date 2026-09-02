package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.person.Address
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import uk.gov.justice.digital.hmpps.utils.Extensions.reportMissing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "upw_project")
@SequenceGenerator(name = "upw_project_id_generator", sequenceName = "upw_project_id_seq", allocationSize = 1)
@EntityListeners(AuditingEntityListener::class)
class UnpaidWorkProject(
    @Id
    @GeneratedId(generator = "upw_project_id_generator")
    @Column(name = "upw_project_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val rowVersion: Long = 0,

    val name: String,

    val code: String,

    val actualStartDate: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "placement_address_id")
    val placementAddress: Address?,

    @ManyToOne
    @JoinColumn(name = "project_type_id")
    val projectType: ReferenceData,

    @OneToMany(mappedBy = "project")
    val availability: List<UnpaidWorkProjectAvailability>,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "high_visibility_vest_required")
    val hiVisRequired: Boolean?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "report_to_site")
    val reportToSite: Boolean? = null,

    @ManyToOne
    @JoinColumn(name = "pickup_point_id")
    val pickupPointLocation: OfficeLocation? = null,

    val expectedEndDate: LocalDate?,

    val completionDate: LocalDate?,

    val beneficiary: String?,
    val beneficiaryAdditionalDetails: String? = null,
    val beneficiaryContactName: String?,
    val beneficiaryEmailAddress: String?,
    val beneficiaryUrl: String?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "beneficiary_sla")
    val beneficiarySla: Boolean? = null,

    @Column(name = "sla_start_date")
    val beneficiarySlaStartDate: LocalDate? = null,

    @Column(name = "sla_end_date")
    val beneficiarySlaEndDate: LocalDate? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "beneficiary_contribution")
    val beneficiaryContribution: Boolean? = null,

    @Column(name = "placement_contact_name")
    val placementContactName: String? = null,

    @Column(name = "location_description")
    val locationDescription: String? = null,

    @Column(name = "placement_email_address")
    val placementEmailAddress: String? = null,

    @Column(name = "placement_url")
    val placementUrl: String? = null,

    @Column(name = "placement_notes")
    val placementNotes: String? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "selectable")
    val selectable: Boolean = false,

    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "beneficiary_contact_address_id")
    val beneficiaryContactAddress: Address?,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
) {
    fun requireAvailabilityOnDate(date: LocalDate) = apply {
        require(completionDate == null || completionDate > date) {
            "Appointment cannot be scheduled after the project completion date (${date} > $completionDate)"
        }
        if (availability.isNotEmpty()) {
            val availableDays = availability.map { DayOfWeek.valueOf(it.dayOfWeek.weekDay.uppercase()) }.toSet()
            require(date.dayOfWeek in availableDays) {
                "Project is not available on the following day: $date (${date.dayOfWeek}). Available days: $availableDays"
            }
        }
    }

    fun telemetry() = mapOf(
        "id" to id.toString(),
        "code" to code,
        "name" to name,
        "teamCode" to team.code,
        "projectTypeCode" to projectType.code,
    )
}

interface UnpaidWorkProjectRepository : JpaRepository<UnpaidWorkProject, Long> {
    @EntityGraph(attributePaths = ["placementAddress", "beneficiaryContactAddress", "team.provider", "projectType", "availability", "pickupPointLocation"])
    fun findByCode(code: String): UnpaidWorkProject?

    @EntityGraph(attributePaths = ["placementAddress", "beneficiaryContactAddress", "team.provider", "projectType", "availability", "pickupPointLocation"])
    fun findByCodeIn(codes: Collection<String>): List<UnpaidWorkProject>

    @EntityGraph(attributePaths = ["placementAddress", "beneficiaryContactAddress", "team.provider", "projectType", "availability", "pickupPointLocation"])
    fun findAllByIdIn(ids: Collection<Long>): List<UnpaidWorkProject>
}

fun UnpaidWorkProjectRepository.getByCode(code: String) = findByCode(code).orNotFoundBy("code", code)

fun UnpaidWorkProjectRepository.getByCodeIn(codes: List<String>) = codes.toSet().let { codes ->
    findByCodeIn(codes).associateBy { it.code }.reportMissing(codes)
}
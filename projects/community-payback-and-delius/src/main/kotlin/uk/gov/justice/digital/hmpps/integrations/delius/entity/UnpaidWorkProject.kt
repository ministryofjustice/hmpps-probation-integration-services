package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "upw_project")
@Immutable
class UpwProject(
    @Id
    @Column(name = "upw_project_id")
    val id: Long,

    val name: String,

    val code: String,

    val teamId: Long,

    @ManyToOne
    @JoinColumn(name = "placement_address_id")
    val placementAddress: Address?,

    @ManyToOne
    @JoinColumn(name = "project_type_id")
    val projectType: ReferenceData,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "high_visibility_vest_required")
    val hiVisRequired: Boolean
)

@Entity
@Table(name = "upw_project_availability")
@Immutable
class UpwProjectAvailability(
    @Id
    @Column(name = "upw_project_availability_id")
    val id: Long,

    val upwProjectId: Long
)

interface UnpaidWorkProjectRepository : JpaRepository<UpwProject, Long> {
    fun getUpwProjectByCode(projectCode: String): UpwProject
}
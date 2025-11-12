package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
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
    val projectType: ReferenceData
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
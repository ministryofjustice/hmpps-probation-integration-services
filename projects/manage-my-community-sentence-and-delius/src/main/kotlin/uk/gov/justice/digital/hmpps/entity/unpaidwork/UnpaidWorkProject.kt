package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.address.Address

@Entity
@Table(name = "upw_project")
@Immutable
class UnpaidWorkProject(
    @Id
    @Column(name = "upw_project_id")
    val id: Long,

    val name: String,

    val code: String,

    @ManyToOne
    @JoinColumn(name = "placement_address_id")
    val placementAddress: Address?,
)

interface UnpaidWorkProjectRepository : JpaRepository<UnpaidWorkProject, Long>
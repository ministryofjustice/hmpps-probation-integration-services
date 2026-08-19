package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class CommunityManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Column(name = "soft_deleted")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Column(name = "active_flag")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @OneToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,
)

interface CommunityManagerRepository : JpaRepository<CommunityManager, Long> {
    fun findByPersonCrn(crn: String): CommunityManager?
}

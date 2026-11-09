package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

@Entity
@Table(name = "supervision_package")
@SQLRestriction("soft_deleted = 0")
class SupervisionPackage (
    @Id
    @Column(name = "supervision_package_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(name = "supervision_package_type_id")
    val packageType: Long,

    @Column("start_datetime")
    val supervisionPackageStart: ZonedDateTime,

    @Column("last_updated_datetime")
    val supervisionPackageLastUpdated: ZonedDateTime

)

interface SupervisionPackageRepository : JpaRepository<SupervisionPackage, Long> {
    fun findAllByPersonCrn(crn: String): List<SupervisionPackage>
}
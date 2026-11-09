package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

@Entity
@Table(name = "supervision_package")
@SQLRestriction("soft_deleted = 0")
class SupervisionPackage(
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

    @Column(name = "start_datetime")
    val supervisionPackageStart: ZonedDateTime,

    @Column(name = "last_updated_datetime")
    val supervisionPackageLastUpdated: ZonedDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface SupervisionPackageRepository : JpaRepository<SupervisionPackage, Long> {
    fun findAllByPersonCrn(crn: String): List<SupervisionPackage>
}
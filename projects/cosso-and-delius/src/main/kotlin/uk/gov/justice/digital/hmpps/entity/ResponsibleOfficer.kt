package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "responsible_officer")
@SQLRestriction("end_date is null")
class ResponsibleOfficer(
    @Id
    @Column(name = "responsible_officer_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne
    @JoinColumn(name = "offender_manager_id")
    val offenderManager: OffenderManager? = null,

    @OneToOne
    @JoinColumn(name = "prison_offender_manager_id")
    val prisonOffenderManager: PrisonOffenderManager? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null

)

@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0")
class OffenderManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea,

    @OneToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Entity
@Table(name = "prison_offender_manager")
@SQLRestriction("soft_deleted = 0")
class PrisonOffenderManager(
    @Id
    @Column(name = "prison_offender_manager_id")
    val id: Long,
    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @OneToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff? = null,
    @OneToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea,
    val emailAddress: String? = null,
    val telephoneNumber: String? = null,
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long> {
    fun findByPerson_Crn(crn: String): ResponsibleOfficer?
}



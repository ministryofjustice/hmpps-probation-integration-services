package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "responsible_officer")
@SQLRestriction("end_date is null")
class ResponsibleOfficer(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "offender_manager_id")
    val communityManager: PersonManager?,

    @ManyToOne
    @JoinColumn(name = "prison_offender_manager_id")
    val prisonManager: PrisonManager?,

    val endDate: ZonedDateTime? = null,

    @Id
    @Column(name = "responsible_officer_id")
    val id: Long,
) {
    fun provider(): Provider? = listOfNotNull(communityManager?.provider, prisonManager?.provider).firstOrNull()
}

@Immutable
@Entity
@Table(name = "prison_offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PrisonManager(

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val provider: Provider,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Id
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long,
)

interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long> {
    fun findByPersonCrn(crn: String): ResponsibleOfficer?

    @Query(
        """
        select distinct p.crn as crn from Staff s
        left join PersonManager pm on pm.staff = s
        left join ResponsibleOfficer ro on ro.communityManager = pm
        left join ro.person p
        where upper(trim(s.code)) = upper(trim(:officerCode))
        order by p.crn
        """
    )
    fun findAllByOfficerCode(officerCode: String, pageable: Pageable): Page<ManagedCaseCrn>
}

interface ManagedCaseCrn {
    val crn: String?
}

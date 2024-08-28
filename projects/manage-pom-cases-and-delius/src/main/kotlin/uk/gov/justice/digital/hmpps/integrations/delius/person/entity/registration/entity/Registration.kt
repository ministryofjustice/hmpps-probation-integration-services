package uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.LocalDate
import java.time.LocalDateTime

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
@Table(name = "registration")
class Registration(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData?,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "next_review_date")
    val nextReviewDate: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData?,

    @Column(name = "deregistered", columnDefinition = "number")
    val deRegistered: Boolean,

    @ManyToOne
    @JoinColumn(name = "registering_team_id")
    val registeringTeam: Team,

    @ManyToOne
    @JoinColumn(name = "registering_staff_id")
    val registeringStaff: Staff,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val registrationNotes: String? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDatetime: LocalDateTime,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(

    val code: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long
) {
    companion object {
        const val MAPPA_CODE = "MAPP"
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {

    @Query(
        """
        select r from Registration r
        join fetch r.type
        join fetch r.level
        where r.person.id = :personId and r.type.code = :code
        order by r.date desc
    """
    )
    fun findRegistrationsByTypeCode(
        personId: Long,
        code: String,
        pageRequest: PageRequest = PageRequest.of(0, 1)
    ): List<Registration>

    @Query(
        """
            select count(r) > 0 from Registration r
            where r.person.id = :personId
            and r.type.code in ('DASO', 'INVI')
        """
    )
    fun hasVloAssigned(personId: Long): Boolean

    @Query(
        "select registration from Registration registration " +
            "where registration.type.code = 'MAPP' " +
            "and registration.person.id = :offenderId " +
            "and registration.softDeleted = false " +
            "and registration.deRegistered = false " +
            "order by registration.createdDatetime desc"
    )
    fun findActiveMappaRegistrationByOffenderId(offenderId: Long?, pageable: Pageable?): Page<Registration>
}

fun RegistrationRepository.findMappaRegistration(personId: Long) =
    findRegistrationsByTypeCode(personId, RegisterType.MAPPA_CODE).firstOrNull()

package uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.RoshLevel
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
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
    @Convert(converter = NumericBooleanConverter::class)
    val deRegistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
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

    @OneToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData? = null,

    @Id
    @Column(name = "register_type_id")
    val id: Long
) {
    companion object {
        const val MAPPA_CODE = "MAPP"
        const val ROSH_FLAG = "1"
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        """
        select count(r) > 0 from Registration r
        where r.person.id = :personId
        and r.type.code in ('DASO', 'INVI')
        """
    )
    fun hasVloAssigned(personId: Long): Boolean

    @Query(
        """
        select r from Registration r
        join fetch r.type
        join fetch r.level
        where r.person.id = :personId 
        and r.type.code = '${RegisterType.MAPPA_CODE}'
        order by r.date desc
        """
    )
    fun findMappaRegistration(personId: Long, pageRequest: PageRequest = PageRequest.of(0, 1)): Registration?

    @Query(
        """
        select r from Registration r
        join fetch r.type
        where r.person.id = :personId 
        and r.type.flag.code = '${RegisterType.ROSH_FLAG}' 
        and r.type.code in (:roshTypes)
        order by r.date desc
        """
    )
    fun findRoshRegistration(
        personId: Long,
        roshTypes: List<String> = RoshLevel.entries.map { it.code },
        pageRequest: PageRequest = PageRequest.of(0, 1)
    ): Registration?
}

package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
@Table(name = "registration")
class Registration(

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "registration_notes")
    val notes: String? = null,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deRegistered: Boolean,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

interface RegistrationRepository : JpaRepository<Registration, Long> {

    @EntityGraph(attributePaths = ["type"])
    fun findByPersonId(personId: Long): List<Registration>

    @Query(
        """ select r from Registration r
        where r.personId = :personId and r.type.code = 'HOIE' order by r.id desc"""
    )
    fun findHomeOfficeInterestByPersonId(personId: Long, pageable: Pageable): List<Registration>
}

fun RegistrationRepository.getHomeOfficeInterestByPersonId(personId: Long): Registration? =
    findHomeOfficeInterestByPersonId(personId, Pageable.ofSize(1)).firstOrNull()

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(

    val code: String,

    val description: String,

    val colour: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long,
)
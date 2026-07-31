package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
class Registration(

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "registration_notes")
    val notes: String? = null,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean = false,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        """ select r from Registration r
        where r.personId = :personId and r.type.code = 'HOIE' order by r.id desc"""
    )
    fun findHomeOfficeInterestByPersonId(personId: Long): Registration?
}

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(
    val code: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long,
)
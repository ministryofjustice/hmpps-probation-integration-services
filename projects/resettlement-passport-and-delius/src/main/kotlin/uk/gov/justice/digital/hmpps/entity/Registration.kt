package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

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
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData?,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "next_review_date")
    val reviewDate: LocalDate?,

    @Column(name = "deregistered", columnDefinition = "number")
    val deRegistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(

    @Id
    @Column(name = "register_type_id")
    val id: Long,

    val code: String
) {
    companion object {
        const val MAPPA_CODE = "MAPP"
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {

    @EntityGraph(attributePaths = ["type", "category", "level"])
    fun findFirstByPersonCrnAndTypeCodeOrderByDateDesc(crn: String, typeCode: String): Registration?
}

fun RegistrationRepository.findMappa(crn: String) =
    findFirstByPersonCrnAndTypeCodeOrderByDateDesc(crn, RegisterType.MAPPA_CODE)

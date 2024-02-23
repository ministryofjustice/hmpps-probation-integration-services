package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
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
    val personId: Long,

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

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

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

    val code: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long,
) {
    companion object {
        const val MAPPA_CODE = "MAPP"
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {

    @EntityGraph(attributePaths = ["type", "category", "level"])
    fun findFirstByPersonIdAndTypeCodeOrderByDateDesc(personId: Long, typeCode: String): Registration?
}

fun RegistrationRepository.findMappa(personId: Long) =
    findFirstByPersonIdAndTypeCodeOrderByDateDesc(personId, RegisterType.MAPPA_CODE)
package uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity

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
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
class Registration(

    @Column(name = "offender_id")
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

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(columnDefinition = "number")
    val deregistered: Boolean,

    val lastUpdatedDatetime: ZonedDateTime,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_register_type")
class RegisterType(

    @Column
    val code: String,

    val description: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        GANG_AFFILIATION("STRG"),
        SEX_OFFENCE("ARSO"),
        MAPPA("MAPP")
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun existsByPersonIdAndTypeCode(personId: Long, code: String): Boolean

    @EntityGraph(attributePaths = ["type", "category", "level"])
    fun findByPersonId(personId: Long): List<Registration>
}

enum class Category(val number: Int) { X9(0), M1(1), M2(2), M3(3), M4(4) }
enum class Level(val number: Int) { M0(0), M1(1), M2(2), M3(3) }

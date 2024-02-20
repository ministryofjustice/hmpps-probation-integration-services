package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

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
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Table(name = "registration")
@Entity(name = "CaseSummaryRegistration")
@SQLRestriction("soft_deleted = 0")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long,

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

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(columnDefinition = "number")
    val deregistered: Boolean = false
)

@Immutable
@Table(name = "r_register_type")
@Entity(name = "CaseSummaryRegisterType")
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData
) {
    companion object {
        const val MAPPA_TYPE = "MAPP"
        const val ROSH_FLAG = "1"
    }
}

interface CaseSummaryRegistrationRepository : JpaRepository<Registration, Long> {
    @Query("select r.type.description from CaseSummaryRegistration r where r.personId = :personId and r.deregistered = false")
    fun findActiveTypeDescriptionsByPersonId(personId: Long): List<String>

    @EntityGraph(attributePaths = ["type.flag", "category", "level"])
    fun findByPersonIdAndTypeFlagCodeOrderByDateDesc(personId: Long, typeCode: String): List<Registration>

    @EntityGraph(attributePaths = ["type.flag", "category", "level"])
    fun findFirstByPersonIdAndTypeCodeAndDeregisteredFalseOrderByDateDesc(
        personId: Long,
        typeCode: String
    ): Registration?
}

fun CaseSummaryRegistrationRepository.findRoshHistory(personId: Long) =
    findByPersonIdAndTypeFlagCodeOrderByDateDesc(personId, RegisterType.ROSH_FLAG)

fun CaseSummaryRegistrationRepository.findMappa(personId: Long) =
    findFirstByPersonIdAndTypeCodeAndDeregisteredFalseOrderByDateDesc(personId, RegisterType.MAPPA_TYPE)

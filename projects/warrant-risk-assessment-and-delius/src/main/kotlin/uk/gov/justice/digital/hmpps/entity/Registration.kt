package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "registration")
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String? = null,

    @ManyToOne
    @JoinColumn(name = "registration_category_id")
    val category: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "registration_level_id")
    val level: ReferenceData? = null,

    @Column(name = "document_linked", columnDefinition = "char")
    @Convert(converter = YesNoConverter::class)
    val documentLinked: Boolean = false,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean = false,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long,

    val code: String,

    val description: String,
) {
    companion object {
        val MAPPA_CODES = Mappa.entries.map { it.code }
    }
}

enum class Mappa(val code: String, val description: String) {
    M1("M1", "MAPPA Level 1"),
    M2("M2", "MAPPA Level 2"),
    M3("M3", "MAPPA Level 3"),
    M3L2("M3L2", "MAPPA Level 3 - Level 2"),
    M3L3("M3L3", "MAPPA Level 3 - Level 3"),
    MAPP("MAPP", "MAPPA - Other"),
}

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        """
        select r from Registration r
        join fetch r.type
        where r.personId = :personId
        and r.type.code in :codes
        order by r.date desc, r.id desc
        """
    )
    fun findLatestByPersonIdAndTypeCodes(
        personId: Long,
        codes: List<String>,
        pageable: Pageable = PageRequest.of(0, 1)
    ): List<Registration>
}

fun RegistrationRepository.findLatestMappaRegistration(personId: Long): Registration? =
    findLatestByPersonIdAndTypeCodes(personId, RegisterType.MAPPA_CODES).firstOrNull()

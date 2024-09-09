package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
@Table(name = "registration")
class RegistrationEntity(
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
    val description: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long,
) {
    companion object {
        const val MAPPA_CODE = "MAPP" // Multi-Agency Public Protection Arrangements
        const val CHILD_CONCERNS_CODE = "RCCO" // Safeguarding concerns where a child is at risk from the offender
        const val CHILD_PROTECTION_CODE = "RCPR" // Child is subject to a protection plan/conference
        const val RISK_TO_VULNERABLE_ADULT_CODE = "RVAD" // Risk to a vulnerable adult
        const val STREET_GANGS_CODE = "STRG" // Involved in serious group offending
        const val VISOR_CODE = "AVIS" // Subject has a ViSOR record
        const val WEAPONS_CODE = "WEAP" // Known to use/carry weapon
        const val LOW_ROSH_CODE = "RLRH" // Low risk of serious harm
        const val MED_ROSH_CODE = "RMRH" // Medium risk of serious harm
        const val HIGH_ROSH_CODE = "RHRH" // High risk of serious harm
        const val VERY_HIGH_ROSH_CODE = "RVRH" // Very high risk of serious harm
        const val SERIOUS_FURTHER_OFFENCE_CODE = "ASFO" // Subject to SFO review/investigation
        const val WARRANT_SUMMONS_CODE = "WRSM" // Outstanding warrant or summons
    }
}

interface RefData {
    val codeSet: String
    val code: String
    val description: String
}
interface RegistrationRepository : JpaRepository<RegistrationEntity, Long> {
    @EntityGraph(attributePaths = ["type", "category", "level"])
    fun findFirstByPersonIdAndTypeCodeOrderByDateDesc(personId: Long, typeCode: String): RegistrationEntity?

    @EntityGraph(attributePaths = ["type", "category", "level"])
    fun findByPersonIdAndTypeCodeInOrderByDateDesc(personId: Long, typeCode: List<String>): List<RegistrationEntity>

    @Query("""
        select rdm.code_set_name as codeSet, rdl.code_value as code, rdl.code_description as description
        from r_standard_reference_list rdl join r_reference_data_master rdm 
        on rdm.reference_data_master_id = rdl.reference_data_master_id
        where rdm.code_set_name in ('GENDER','ETHNICITY')
        union
        select 'REGISTER_TYPES' as set_name, rt.code, rt.description 
        from r_register_type rt
    """, nativeQuery = true)
    fun getReferenceData() : List<RefData>

}

fun RegistrationRepository.findMappa(personId: Long) =
    findFirstByPersonIdAndTypeCodeOrderByDateDesc(personId, RegisterType.MAPPA_CODE)

fun RegistrationRepository.findDynamicRiskRegistrations(personId: Long) = findByPersonIdAndTypeCodeInOrderByDateDesc(
    personId, listOf(
        RegisterType.CHILD_CONCERNS_CODE,
        RegisterType.CHILD_PROTECTION_CODE,
        RegisterType.RISK_TO_VULNERABLE_ADULT_CODE,
        RegisterType.STREET_GANGS_CODE,
        RegisterType.VISOR_CODE,
        RegisterType.WEAPONS_CODE,
        RegisterType.LOW_ROSH_CODE,
        RegisterType.MED_ROSH_CODE,
        RegisterType.HIGH_ROSH_CODE,
        RegisterType.VERY_HIGH_ROSH_CODE,
    )
)

fun RegistrationRepository.findPersonStatusRegistrations(personId: Long) = findByPersonIdAndTypeCodeInOrderByDateDesc(
    personId, listOf(
        RegisterType.SERIOUS_FURTHER_OFFENCE_CODE,
        RegisterType.WARRANT_SUMMONS_CODE,
    )
)
package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(name = "active_flag")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "disposal")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "active_flag")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "r_disposal_type")
class DisposalType(
    @Column(name = "disposal_type_code")
    val code: String,
    val sentenceType: String?,
    val requiredInformation: String?,

    @Id
    @Column(name = "disposal_type_id")
    val id: Long
) {
    fun defaultSentenceTypeCode() = when {
        code == "233" -> "SDO"
        sentenceType == "SC" -> "PSS"
        code in listOf("331", "342", "204") -> "YRO"
        requiredInformation == "LL" -> "SSO"
        else -> "CO"
    }
}

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(
    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Requirement(

    @ManyToOne
    @JoinColumn(name = "disposal_id", nullable = false)
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,

    @Column(name = "active_flag")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "rqmnt_id")
    val id: Long,
)

interface RequirementRepository : JpaRepository<Requirement, Long> {
    @Query(
        """
            SELECT r 
            FROM Requirement r
            WHERE r.mainCategory.code = 'W'
            AND r.subCategory.code in ('W01', 'W03', 'W05')
            AND r.disposal.id = :id
        """
    )
    fun getUnpaidWorkRequirementsByDisposal(id: Long): List<Requirement>
}

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    override val code: String,
    override val description: String,
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long
) : CodeAndDescription

@Entity
@Immutable
@Table(name = "pss_rqmnt")
@SQLRestriction("soft_deleted = 0")
class PssRequirement(

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "pss_rqmnt_type_main_cat_id")
    val mainCategory: PssRequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "pss_rqmnt_type_sub_cat_id")
    val subCategory: PssRequirementSubCategory?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "pss_rqmnt_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_pss_rqmnt_type_main_category")
class PssRequirementMainCategory(
    override val code: String,
    override val description: String,
    @Id
    @Column(name = "pss_rqmnt_type_main_cat_id")
    val id: Long
) : CodeAndDescription

@Immutable
@Entity
@Table(name = "r_pss_rqmnt_type_sub_category")
class PssRequirementSubCategory(
    override val code: String,
    override val description: String,
    @Id
    @Column(name = "pss_rqmnt_type_sub_cat_id")
    val id: Long
) : CodeAndDescription

interface DisposalRepository : JpaRepository<Disposal, Long> {
    fun getByEventId(eventId: Long): Disposal?
}
package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(

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

interface DisposalRepository : JpaRepository<Disposal, Long> {
    fun getByEventId(eventId: Long): Disposal
}
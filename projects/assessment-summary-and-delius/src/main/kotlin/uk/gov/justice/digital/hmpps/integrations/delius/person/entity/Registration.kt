package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.AuditableEntity
import uk.gov.justice.digital.hmpps.integrations.delius.entity.AuditableNonPartitionedEntity
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Entity
@Table(name = "registration")
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("deregistered = 0 and soft_deleted = 0")
@SequenceGenerator(name = "registration_id_seq", sequenceName = "registration_id_seq", allocationSize = 1)
class Registration(

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "registration_date")
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @Column(name = "registering_team_id")
    val teamId: Long = contact.teamId,

    @Column(name = "registering_staff_id")
    val staffId: Long = contact.staffId,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    var category: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    var level: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "initial_register_category_id")
    val initialCategory: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "initial_register_level_id")
    val initialLevel: ReferenceData? = null,

    var nextReviewDate: LocalDate? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var deregistered: Boolean = false,

    @OneToOne(mappedBy = "registration", cascade = [CascadeType.ALL])
    var deregistration: DeRegistration? = null,

    @OneToMany(mappedBy = "registration", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("date, createdDatetime")
    var reviews: MutableList<RegistrationReview> = mutableListOf(),

    @Lob
    @Column(name = "registration_notes")
    var notes: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "registration_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_id_seq")
    val id: Long = 0
) : AuditableEntity()

@Immutable
@Table(name = "r_register_type")
@Entity
class RegisterType(
    @Column
    val code: String,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "registration_contact_type_id")
    val registrationContactType: ContactType?,

    @Column(name = "register_review_period")
    val reviewPeriod: Long?,

    @ManyToOne
    @JoinColumn(name = "review_contact_type_id")
    val reviewContactType: ContactType?,

    val colour: String?,

    @Id
    @Column(name = "register_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        MAPPA("MAPP"),
        VISOR("AVIS"),
    }
}

@Immutable
@Entity
@Table(name = "r_register_duplicate_group")
class RegisterDuplicateGroup(
    @ManyToMany
    @JoinTable(
        name = "r_register_type_dup_grp",
        joinColumns = [JoinColumn(name = "register_group_id")],
        inverseJoinColumns = [JoinColumn(name = "register_type_id")]
    )
    val types: List<RegisterType>,

    @Id
    @Column(name = "register_group_id")
    val id: Long
)

@Entity
@Table(name = "registration_review")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "registration_review_id_seq", sequenceName = "registration_review_id_seq", allocationSize = 1)
@EntityListeners(AuditingEntityListener::class)
class RegistrationReview(
    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration,

    @Column(name = "offender_id")
    val personId: Long = registration.personId,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @Column(name = "review_date")
    var date: LocalDate?,

    @Column(name = "review_date_due")
    var reviewDue: LocalDate?,

    @Column(name = "reviewing_team_id")
    val teamId: Long,

    @Column(name = "reviewing_staff_id")
    val staffId: Long,

    @Lob
    var notes: String? = null,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData? = null,

    @Convert(converter = YesNoConverter::class)
    var completed: Boolean = false,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "registration_review_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_review_id_seq")
    val id: Long = 0
) : AuditableEntity()

@Entity
@Table(name = "reg_category_level_history")
@SequenceGenerator(
    name = "reg_cat_level_history_id_seq",
    sequenceName = "reg_cat_level_history_id_seq",
    allocationSize = 1
)
@EntityListeners(AuditingEntityListener::class)
class RegistrationHistory(
    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration,

    @Column
    val startDate: LocalDate,

    @Column
    var endDate: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData? = registration.category,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData? = registration.level,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "reg_category_level_history_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reg_cat_level_history_id_seq")
    val id: Long = 0,
) : AuditableNonPartitionedEntity()

@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "deregistration")
@SequenceGenerator(name = "deregistration_id_seq", sequenceName = "deregistration_id_seq", allocationSize = 1)
@EntityListeners(AuditingEntityListener::class)
class DeRegistration(

    @Column(name = "deregistration_date")
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration,

    @Column(name = "offender_id")
    val personId: Long = registration.personId,

    @OneToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @Column(name = "deregistering_team_id")
    val teamId: Long,
    @Column(name = "deregistering_staff_id")
    val staffId: Long,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "deregistration_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deregistration_id_seq")
    val id: Long = 0
) : AuditableEntity()

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @EntityGraph(attributePaths = ["contact", "type.flag", "type.registrationContactType", "type.reviewContactType", "reviews.contact"])
    fun findByPersonIdAndTypeFlagCode(personId: Long, flagCode: String): List<Registration>

    @EntityGraph(attributePaths = ["contact", "type.flag", "type.registrationContactType", "type.reviewContactType", "reviews.contact"])
    fun findByPersonIdAndTypeCodeInOrderByLastUpdatedDatetimeDesc(
        personId: Long,
        typeCodes: List<String>
    ): List<Registration>

    @Query(
        """
        select r1.type.code as type, count(r1) as number from Registration r1
        where r1.personId = :personId and r1.type.code = 'AVIS'
        group by r1.type.code
        union all 
        select r2.type.code as type, count(r2) as number from Registration r2
        where r2.personId = :personId and r2.type.code = 'MAPP' 
        and r2.category.code in ('M1', 'M2', 'M3', 'M4') and r2.level.code in ('M1', 'M2', 'M3')
        group by r2.type.code
    """
    )
    fun hasVisorAndMappa(personId: Long): List<RegisterTypeCount>
}

interface RegisterTypeCount {
    val type: String
    val number: Int
}

fun RegistrationRepository.findByPersonIdAndTypeCode(personId: Long, typeCode: String) =
    findByPersonIdAndTypeCodeInOrderByLastUpdatedDatetimeDesc(personId, listOf(typeCode))

fun RegistrationRepository.findByPersonIdAndTypeCodeIn(personId: Long, typeCodes: List<String>) =
    findByPersonIdAndTypeCodeInOrderByLastUpdatedDatetimeDesc(personId, typeCodes)

interface RegisterTypeRepository : JpaRepository<RegisterType, Long> {
    @EntityGraph(attributePaths = ["flag", "registrationContactType", "reviewContactType"])
    fun findByCode(code: String): RegisterType?

    @Query(
        """
        select distinct t2.code
        from RegisterDuplicateGroup g 
        join g.types t1 on t1.code = :code
        join g.types t2 on t2.code <> :code
        """
    )
    fun findOtherTypesInGroup(code: String): List<String>
}

fun RegisterTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("RegisterType", "code", code)

interface RegistrationHistoryRepository : JpaRepository<RegistrationHistory, Long> {
    fun findByRegistrationIdAndEndDateIsNull(registrationId: Long): RegistrationHistory?
}
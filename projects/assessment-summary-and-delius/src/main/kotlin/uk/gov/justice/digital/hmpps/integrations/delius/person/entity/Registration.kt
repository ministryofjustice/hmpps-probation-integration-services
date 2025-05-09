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
    val teamId: Long,

    @Column(name = "registering_staff_id")
    val staffId: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    var category: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    var level: ReferenceData? = null,

    var nextReviewDate: LocalDate? = null,

    @Lob
    @Column(name = "registration_notes")
    val notes: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "registration_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_id_seq")
    val id: Long = 0
) : AuditableEntity() {

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var deregistered: Boolean = false
        private set

    @OneToOne(mappedBy = "registration", cascade = [CascadeType.ALL])
    var deregistration: DeRegistration? = null
        private set

    @OneToMany(mappedBy = "registration", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("date, createdDatetime")
    var reviews: MutableList<RegistrationReview> = mutableListOf()
        private set

    fun withReview(contact: Contact, notes: String? = contact.notes): Registration {
        reviews += RegistrationReview(personId, this, contact, nextReviewDate, null, teamId, staffId, notes)
        return this
    }

    fun deregister(contact: Contact): Boolean {
        deregistration = DeRegistration(LocalDate.now(), this, personId, contact, contact.teamId, contact.staffId)
        deregistered = true
        nextReviewDate = null
        reviews.removeIf { !it.completed && it.notes.isNullOrBlank() && it.lastUpdatedDatetime == it.createdDatetime }
        return reviews.lastOrNull()?.let {
            it.reviewDue = null
            if (it.completed) {
                false
            } else {
                it.completed = true
                true
            }
        } == true
    }
}

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

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration,

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
    val personId: Long,

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
    fun findByPersonIdAndTypeCodeIn(personId: Long, typeCodes: List<String>): List<Registration>

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
    findByPersonIdAndTypeCodeIn(personId, listOf(typeCode))

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

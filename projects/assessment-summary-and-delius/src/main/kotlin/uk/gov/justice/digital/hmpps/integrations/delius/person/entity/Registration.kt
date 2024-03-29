package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
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

    var nextReviewDate: LocalDate? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "registration_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_id_seq")
    val id: Long = 0
) : AuditableEntity() {

    @Column(columnDefinition = "number")
    var deregistered: Boolean = false
        private set

    @OneToOne(mappedBy = "registration", cascade = [CascadeType.ALL])
    var deregistration: DeRegistration? = null
        private set

    @OneToMany(mappedBy = "registration", cascade = [CascadeType.ALL])
    @OrderBy("date, createdDatetime")
    var reviews: List<RegistrationReview> = listOf()
        private set

    fun withReview(contact: Contact): Registration {
        reviews = reviews + RegistrationReview(personId, this, contact, nextReviewDate, null, teamId, staffId)
        return this
    }

    fun deregister(contact: Contact): List<Contact> {
        deregistration = DeRegistration(LocalDate.now(), this, personId, contact, contact.teamId, contact.staffId)
        deregistered = true
        nextReviewDate = null
        val splitReviews = reviews.groupBy { it.completed }
        reviews = splitReviews[true] ?: listOf()
        reviews.firstOrNull()?.reviewDue = null
        return splitReviews[false]?.map { it.contact } ?: listOf()
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
    val date: LocalDate?,

    @Column(name = "review_date_due")
    var reviewDue: LocalDate?,

    @Column(name = "reviewing_team_id")
    val teamId: Long,
    @Column(name = "reviewing_staff_id")
    val staffId: Long,

    @Convert(converter = YesNoConverter::class)
    val completed: Boolean = false,

    @Column(columnDefinition = "number")
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
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "deregistration_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deregistration_id_seq")
    val id: Long = 0
) : AuditableEntity()

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @EntityGraph(attributePaths = ["contact", "type.flag", "type.registrationContactType", "type.reviewContactType", "reviews.contact"])
    fun findByPersonIdAndTypeFlagCode(personId: Long, flagCode: String): List<Registration>
}

interface RegisterTypeRepository : JpaRepository<RegisterType, Long> {
    @EntityGraph(attributePaths = ["flag", "registrationContactType", "reviewContactType"])
    fun findByCode(code: String): RegisterType?
}

fun RegisterTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("RegisterType", "code", code)

package uk.gov.justice.digital.hmpps.flagged

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.entity.AuditableEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Entity
@Table(name = "registration")
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("deregistered = 0 and soft_deleted = 0")
@SequenceGenerator(name = "registration_id_seq", sequenceName = "registration_id_seq", allocationSize = 1)
class FlaggedRegistration(

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

    var nextReviewDate: LocalDate? = null,

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
) : AuditableEntity() {
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var deregistered: Boolean = false
        private set

    @OneToOne(mappedBy = "registration", cascade = [CascadeType.ALL])
    var deregistration: FlaggedDeRegistration? = null
        private set

    @OneToMany(mappedBy = "registration", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("date, createdDatetime")
    var reviews: MutableList<FlaggedRegistrationReview> = mutableListOf()
        private set

    fun withReview(contact: Contact, notes: String? = contact.notes): FlaggedRegistration {
        reviews += FlaggedRegistrationReview(personId, this, contact, nextReviewDate, null, teamId, staffId, notes)
        return this
    }

    fun deregister(contact: Contact): Boolean {
        deregistration =
            FlaggedDeRegistration(LocalDate.now(), this, personId, contact, contact.teamId, contact.staffId)
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

@Entity
@Table(name = "registration_review")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "registration_review_id_seq", sequenceName = "registration_review_id_seq", allocationSize = 1)
@EntityListeners(AuditingEntityListener::class)
class FlaggedRegistrationReview(
    @Column(name = "offender_id")
    val personId: Long = registration.personId,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: FlaggedRegistration,

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
class FlaggedDeRegistration(

    @Column(name = "deregistration_date")
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "registration_id")
    val registration: FlaggedRegistration,

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

interface FlaggedRegistrationRepository : JpaRepository<FlaggedRegistration, Long> {
    @EntityGraph(attributePaths = ["contact", "type.flag", "type.registrationContactType", "type.reviewContactType", "reviews.contact"])
    fun findByPersonIdAndTypeFlagCode(personId: Long, flagCode: String): List<FlaggedRegistration>

    @EntityGraph(attributePaths = ["contact", "type.flag", "type.registrationContactType", "type.reviewContactType", "reviews.contact"])
    fun findByPersonIdAndTypeCodeIn(personId: Long, typeCodes: List<String>): List<FlaggedRegistration>
}

fun FlaggedRegistrationRepository.findByPersonIdAndTypeCode(personId: Long, typeCode: String) =
    findByPersonIdAndTypeCodeIn(personId, listOf(typeCode))

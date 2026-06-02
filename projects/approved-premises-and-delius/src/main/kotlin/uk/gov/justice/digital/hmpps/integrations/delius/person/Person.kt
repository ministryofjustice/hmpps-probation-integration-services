package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "offender")
class PersonFull(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String? = null,

    @Column(name = "cro_number")
    val croNumber: String? = null,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsNumber: String? = null,

    @Column(name = "immigration_number")
    val immigrationNumber: String? = null,

    @Column(name = "ni_number", columnDefinition = "char(9)")
    val niNumber: String? = null,

    @Column(name = "most_recent_prisoner_number")
    val mostRecentPrisonerNumber: String? = null,

    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "preferred_name", length = 35)
    val preferredName: String? = null,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "telephone_number")
    val telephoneNumber: String? = null,

    @Column(name = "mobile_number")
    val mobileNumber: String? = null,

    @Column(name = "e_mail_address")
    val emailAddress: String? = null,

    @Column(name = "previous_surname")
    val previousSurname: String? = null,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "language_id")
    val language: ReferenceData? = null,

    @Column(name = "language_concerns")
    val languageConcerns: String? = null,

    @ManyToOne
    @JoinColumn(name = "sexual_orientation_id")
    val sexualOrientation: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "gender_identity_id")
    val genderIdentity: ReferenceData? = null,

    @Column(name = "gender_identity_description")
    val genderIdentityDescription: String? = null,

    @Column(name = "Interpreter_required")
    @Convert(converter = YesNoConverter::class)
    val requiresInterpreter: Boolean? = false,

    @OneToMany(mappedBy = "personId")
    val disabilities: List<Disability> = emptyList(),

    @OneToMany(mappedBy = "personId")
    val offenderAliases: List<OffenderAlias> = emptyList(),

    @OneToMany(mappedBy = "personId")
    val provisions: List<Provision> = emptyList(),

    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    val nationality: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "second_nationality_id")
    val secondNationality: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "immigration_status_id")
    val immigrationStatus: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "current_tier")
    val currentTier: ReferenceData? = null,

    @OneToMany(mappedBy = "personId", fetch = FetchType.LAZY)
    val addresses: List<PersonAddress> = emptyList(),

    @Column(name = "allow_sms")
    @Convert(converter = YesNoConverter::class)
    val allowSms: Boolean? = false,

    @Column(name = "current_exclusion", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentExclusion: Boolean = false,

    @Column(name = "current_restriction", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentRestriction: Boolean = false,
)

@Immutable
@Entity
@Table(name = "alias")
@SQLRestriction("soft_deleted = 0")
class OffenderAlias(

    @Id
    @Column(name = "alias_id")
    val aliasID: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "first_name")
    val firstName: String,

    @Column(name = "second_name")
    val secondName: String? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    val surname: String,

    @Column(name = "third_name")
    val thirdName: String? = null,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData
)
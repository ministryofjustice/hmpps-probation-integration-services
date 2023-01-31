package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.controller.common.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
class CaseEntity(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(name = "surname", length = 35)
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,
    @Column(columnDefinition = "char(12)")
    val croNumber: String? = null,
    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,
    @Column(name = "e_mail_address")
    val emailAddress: String? = null,
    @Column(name = "telephoneNumber")
    val telephoneNumber: String? = null,
    @Column(name = "mobileNumber")
    val mobileNumber: String? = null,
    @OneToOne(mappedBy = "person")
    val mainAddress: CaseAddress? = null,
    @ManyToOne
    @JoinColumn(name = "gender_identity_id")
    val genderIdentity: ReferenceData? = null,
    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData? = null,

    @OneToMany(mappedBy = "case", fetch = FetchType.EAGER)
    val personalCircumstances: List<CasePersonalCircumstanceEntity>,

    @OneToMany(mappedBy = "case", fetch = FetchType.EAGER)
    val personalContacts: List<CasePersonalContactEntity>,

    @OneToMany(mappedBy = "case", fetch = FetchType.EAGER)
    val aliases: List<AliasEntity>,

    @OneToMany(mappedBy = "case", fetch = FetchType.EAGER)
    val disabilities: List<DisabilityEntity>,

    @ManyToOne
    @JoinColumn(name = "language_id")
    val primaryLanguage: ReferenceData? = null,

    @Column(name = "Interpreter_required")
    @Convert(converter = YesNoConverter::class)
    val requiresInterpreter: Boolean? = false,

    )

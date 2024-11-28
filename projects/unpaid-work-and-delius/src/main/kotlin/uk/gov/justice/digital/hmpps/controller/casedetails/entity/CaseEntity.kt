package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData
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
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(name = "surname", length = 35)
    val surname: String,
    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
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
    @OneToMany(mappedBy = "person")
    @SQLRestriction(
        """
        address_status_id = (
        select at.standard_reference_list_id from r_standard_reference_list at 
        join r_reference_data_master rm on rm.reference_data_master_id = at.reference_data_master_id
        where rm.code_set_name = 'ADDRESS STATUS' and at.code_value = 'M'
        )
    """
    )
    val addresses: List<CaseAddress> = listOf(),
    @ManyToOne
    @JoinColumn(name = "gender_identity_id")
    val genderIdentity: ReferenceData? = null,
    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData? = null,

    @OneToMany(mappedBy = "case")
    val personalCircumstances: List<CasePersonalCircumstanceEntity>,

    @OneToMany(mappedBy = "case")
    val personalContacts: List<CasePersonalContactEntity>,

    @OneToMany(mappedBy = "case")
    val aliases: List<AliasEntity>,

    @OneToMany(mappedBy = "case")
    val disabilities: List<DisabilityEntity>,

    @OneToMany(mappedBy = "case")
    val provisions: List<ProvisionEntity>,

    @ManyToOne
    @JoinColumn(name = "language_id")
    val primaryLanguage: ReferenceData? = null,

    @Column(name = "Interpreter_required")
    @Convert(converter = YesNoConverter::class)
    val requiresInterpreter: Boolean? = false,

    @OneToMany(mappedBy = "case")
    val registrations: List<RegistrationEntity>,

    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,

    )

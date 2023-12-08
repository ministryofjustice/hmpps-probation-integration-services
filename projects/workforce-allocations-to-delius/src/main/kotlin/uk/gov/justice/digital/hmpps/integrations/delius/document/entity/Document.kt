package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.document.Relatable
import uk.gov.justice.digital.hmpps.integrations.delius.document.RelatedTo
import uk.gov.justice.digital.hmpps.integrations.delius.document.RelatedType
import uk.gov.justice.digital.hmpps.integrations.delius.document.toDocumentEvent
import java.time.ZonedDateTime

@Entity
@Immutable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "table_name", discriminatorType = DiscriminatorType.STRING)
abstract class Document : Relatable {
    @Id
    @Column(name = "document_id")
    open var id: Long = 0

    @Column(name = "document_name")
    open var name: String = ""

    @Column(name = "offender_id")
    open var personId: Long = 0

    @Column(name = "primary_key_id")
    open var primaryKeyId: Long = 0

    @Column(columnDefinition = "char(1)")
    open var status: String = "N"
    open var softDeleted: Boolean = false

    @Convert(converter = YesNoConverter::class)
    open var workInProgress: Boolean? = false

    @Column(name = "alfresco_document_id")
    open var alfrescoId: String? = null
    open var sensitive: Boolean = false
    open var lastSaved: ZonedDateTime? = ZonedDateTime.now()
    open var dateProduced: ZonedDateTime? = ZonedDateTime.now()

    @Column(name = "created_datetime")
    open var createdDate: ZonedDateTime? = ZonedDateTime.now()

    @Column(name = "document_type")
    @Enumerated(EnumType.STRING)
    open var type: DocumentType = DocumentType.DOCUMENT
}

enum class DocumentType {
    DOCUMENT,
    CPS_PACK,
    PREVIOUS_CONVICTION,
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("EQUALITY")
class Equality : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.EQUALITY)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("DRUGS_TEST")
class DrugTest : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.DRUGS_TEST)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("OFFENDER")
class OffenderDocument : Document() {
    override fun findRelatedTo(): RelatedTo =
        if (type == DocumentType.PREVIOUS_CONVICTION) {
            RelatedTo(RelatedType.PRECONS)
        } else {
            RelatedTo(RelatedType.PERSON)
        }
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("OFFENDER_ADDRESS")
class OffenderAddress : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.OFFENDER_ADDRESS)
}

const val ENTITY_NOT_FOUND = "ENTITY_NOT_FOUND"

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("EVENT")
class EventDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "event_id", insertable = false, updatable = false)
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val event: DocEvent?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        if (type == DocumentType.CPS_PACK) {
            RelatedTo(
                RelatedType.CPSPACK,
                event = event?.toDocumentEvent(),
            )
        } else {
            RelatedTo(
                RelatedType.EVENT,
                if (event == null) ENTITY_NOT_FOUND else event.disposal?.type?.description ?: "",
                event?.toDocumentEvent(),
            )
        }
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("ADDRESSASSESSMENT")
class AddressAssessmentDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "address_assessment_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val addressAssessment: AddressAssessment?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.ADDRESS_ASSESSMENT,
            getPersonAddressLine(addressAssessment?.personAddress),
        )

    private fun getPersonAddressLine(personAddress: DocPersonAddress?): String {
        if (personAddress == null) {
            return ENTITY_NOT_FOUND
        } else {
            (
                return listOfNotNull(
                    personAddress.buildingName,
                    personAddress.addressNumber,
                    personAddress.streetName,
                ).joinToString(", ")
            )
        }
    }
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("APPROVED_PREMISES_REFERRAL")
class ApprovedPremisesReferralDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "approved_premises_referral_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val approvedPremisesReferral: ApprovedPremisesReferral?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.APPROVED_PREMISES_REFERRAL,
            approvedPremisesReferral?.category?.description ?: ENTITY_NOT_FOUND,
            approvedPremisesReferral?.event?.toDocumentEvent(),
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("ASSESSMENT")
class AssessmentDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "assessment_id", insertable = false, updatable = false)
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val assessment: Assessment?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.ASSESSMENT,
            assessment?.type?.description ?: ENTITY_NOT_FOUND,
            assessment?.referral?.event?.toDocumentEvent(),
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("CASE_ALLOCATION")
class CaseAllocationDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "case_allocation_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val allocation: CaseAllocation?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.CASE_ALLOCATION,
            allocation?.event?.disposal?.type?.description ?: ENTITY_NOT_FOUND,
            allocation?.event?.toDocumentEvent(),
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("CONTACT")
class ContactDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "contact_id", insertable = false, updatable = false)
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val contact: DocContact?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.CONTACT,
            contact?.type?.description ?: ENTITY_NOT_FOUND,
            contact?.event?.toDocumentEvent(),
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("COURT_REPORT")
class CourtReportDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "court_report_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val courtReport: CourtReport?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.COURT_REPORT,
            courtReport?.type?.description ?: ENTITY_NOT_FOUND,
            courtReport?.documentCourtAppearance?.event?.toDocumentEvent(),
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("INSTITUTIONAL_REPORT")
class InstitutionalReportDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "institutional_report_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val institutionalReport: InstitutionalReport?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(RelatedType.INSTITUTIONAL_REPORT, institutionalReport?.type?.description ?: ENTITY_NOT_FOUND)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("NSI")
class NsiDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "nsi_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val nsi: Nsi?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.NSI,
            nsi?.type?.description ?: ENTITY_NOT_FOUND,
            nsi?.event?.toDocumentEvent(),
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("PERSONAL_CIRCUMSTANCE")
class PersonalCircumstanceDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "personal_circumstance_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val personalCircumstance: PersonalCircumstance?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(RelatedType.PERSONAL_CIRCUMSTANCE, getCircName())

    private fun getCircName(): String {
        var circName: String = personalCircumstance?.type?.description ?: ENTITY_NOT_FOUND
        if (personalCircumstance?.subType != null) {
            circName += " - ${personalCircumstance.subType.description}"
        }
        return circName
    }
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("PERSONALCONTACT")
class PersonalContactDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "personal_contact_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val personalContact: DocPersonalContact?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(RelatedType.PERSONAL_CONTACT, getPersonalContactLine())

    private fun getPersonalContactLine(): String {
        return if (personalContact == null) {
            ENTITY_NOT_FOUND
        } else {
            listOfNotNull(
                personalContact.title?.description,
                personalContact.forename,
                personalContact.surname,
            ).joinToString(" ")
        }
    }
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("REFERRAL")
class ReferralDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "referral_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val referral: Referral?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.REFERRAL,
            referral?.type?.description ?: ENTITY_NOT_FOUND,
            referral?.event?.toDocumentEvent(),
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("REGISTRATION")
class RegistrationDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "registration_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val registration: DocRegistration?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(RelatedType.REGISTRATION, registration?.type?.description ?: ENTITY_NOT_FOUND)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("UPW_APPOINTMENT")
class UPWAppointmentDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "upw_appointment_id",
        insertable = false,
        updatable = false,
    )
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val upwAppointment: UpwAppointment?,
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.UPW_APPOINTMENT,
            upwAppointment?.upwDetails?.disposal?.type?.description ?: ENTITY_NOT_FOUND,
            upwAppointment?.upwDetails?.disposal?.event?.toDocumentEvent(),
        )
}

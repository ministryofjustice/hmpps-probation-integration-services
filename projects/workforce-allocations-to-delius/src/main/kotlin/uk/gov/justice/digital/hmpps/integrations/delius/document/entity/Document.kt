package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.integrations.delius.document.Relatable
import uk.gov.justice.digital.hmpps.integrations.delius.document.RelatedTo
import uk.gov.justice.digital.hmpps.integrations.delius.document.RelatedType
import uk.gov.justice.digital.hmpps.integrations.delius.document.toDocumentEvent
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.DiscriminatorColumn
import javax.persistence.DiscriminatorType
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

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

    @Type(type = "yes_no")
    open var workInProgress: Boolean? = false

    @Column(name = "alfresco_document_id")
    open var alfrescoId: String? = null
    open var sensitive: Boolean = false
    open var lastSaved: ZonedDateTime = ZonedDateTime.now()
    @Column(name = "created_datetime")
    open var createdDate: ZonedDateTime = ZonedDateTime.now()
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("OFFENDER")
class OffenderDocument : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.OFFENDER)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("OFFENDER_ADDRESS")
class OffenderAddress : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.OFFENDER_ADDRESS)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("EVENT")
class EventDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.EVENT,
            event.disposal?.type?.description ?: "",
            event.toDocumentEvent()
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("ADDRESSASSESSMENT")
class AddressAssessmentDocument : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.ADDRESS_ASSESSMENT)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("APPROVED_PREMISES_REFERRAL")
class ApprovedPremisesReferralDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "approved_premises_referral_id",
        insertable = false,
        updatable = false
    )
    @ManyToOne
    val approvedPremisesReferral: ApprovedPremisesReferral
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.APPROVED_PREMISES_REFERRAL,
            approvedPremisesReferral.category.description,
            approvedPremisesReferral.event.toDocumentEvent()
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("ASSESSMENT")
class AssessmentDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "assessment_id", insertable = false, updatable = false)
    @ManyToOne
    val assessment: Assessment
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.ASSESSMENT,
            assessment.type.description,
            assessment.referral?.event?.toDocumentEvent()
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
        updatable = false
    )
    @ManyToOne
    val allocation: CaseAllocation
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.CASE_ALLOCATION,
            allocation.event.disposal?.type?.description ?: "",
            allocation.event.toDocumentEvent()
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("CONTACT")
class ContactDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "contact_id", insertable = false, updatable = false)
    @ManyToOne
    val contact: DocContact
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.CONTACT,
            contact.type.description,
            contact.event?.toDocumentEvent()
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
        updatable = false
    )
    @ManyToOne
    val courtReport: CourtReport
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.COURT_REPORT,
            courtReport.type.description,
            courtReport.courtAppearance?.event?.toDocumentEvent()
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("INSTITUTIONAL_REPORT")
class InstitutionalReportDocument() : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.INSTITUTIONAL_REPORT)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("NSI")
class NsiDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "nsi_id",
        insertable = false,
        updatable = false
    )
    @ManyToOne
    val nsi: Nsi
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.NSI,
            nsi.type.description,
            nsi.event?.toDocumentEvent()
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("PERSONAL_CIRCUMSTANCE")
class PersonalCircumstanceDocument : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.PERSONAL_CIRCUMSTANCE)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("PERSONALCONTACT")
class PersonalContactDocument : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.PERSONAL_CONTACT)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("REFERRAL")
class ReferralDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "referral_id",
        insertable = false,
        updatable = false
    )
    @ManyToOne
    val referral: Referral
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.REFERRAL,
            referral.type.description,
            referral.event.toDocumentEvent()
        )
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("REGISTRATION")
class RegistrationDocument : Document() {
    override fun findRelatedTo(): RelatedTo = RelatedTo(RelatedType.REGISTRATION)
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("UPW_APPOINTMENT")
class UPWAppointmentDocument(
    @JoinColumn(
        name = "primary_key_id",
        referencedColumnName = "upw_appointment_id",
        insertable = false,
        updatable = false
    )
    @ManyToOne
    val upwAppointment: UpwAppointment
) : Document() {
    override fun findRelatedTo(): RelatedTo =
        RelatedTo(
            RelatedType.UPW_APPOINTMENT,
            "",
            upwAppointment.upwDetails?.disposal?.event?.toDocumentEvent()
        )
}

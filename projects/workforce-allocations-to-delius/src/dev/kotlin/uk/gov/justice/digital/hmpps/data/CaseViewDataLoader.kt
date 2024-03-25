package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.repository.CaseViewDisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.CaseViewEventRepository
import uk.gov.justice.digital.hmpps.data.repository.CaseViewMainOffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.CaseViewOffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.CaseViewPersonAddressRepository
import uk.gov.justice.digital.hmpps.data.repository.CaseViewRequirementMainCategoryRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewAdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtappearance.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtappearance.CourtRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalType
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class CaseViewDataLoader(
    val personRepository: CaseViewPersonRepository,
    val addressRepository: CaseViewPersonAddressRepository,
    val caseViewEventRepository: CaseViewEventRepository,
    val eventRepository: EventRepository,
    val disposalTypeRepository: DisposalTypeRepository,
    val caseViewDisposalRepository: CaseViewDisposalRepository,
    val disposalRepository: DisposalRepository,
    val offenceRepository: CaseViewOffenceRepository,
    val mainOffenceRepository: CaseViewMainOffenceRepository,
    val additionalOffenceRepository: CaseViewAdditionalOffenceRepository,
    val requirementMainCategoryRepository: CaseViewRequirementMainCategoryRepository,
    val requirementRepository: CaseViewRequirementRepository,
    val requirementManagerRepository: RequirementManagerRepository,
    val documentRepository: DocumentRepository,
    val courtRepository: CourtRepository,
    val courtAppearanceRepository: CourtAppearanceRepository,
    val contactRepository: ContactRepository
) {
    fun loadData() {
        personRepository.save(PersonGenerator.CASE_VIEW)
        addressRepository.saveAll(
            listOf(
                AddressGenerator.forCaseView(
                    "Previous House",
                    postcode = "SM3 8WR",
                    person = PersonGenerator.CASE_VIEW,
                    status = ReferenceDataGenerator.ADDRESS_STATUS_PREVIOUS
                ),
                AddressGenerator.CASE_VIEW
            )
        )
        caseViewEventRepository.save(EventGenerator.CASE_VIEW)

        offenceRepository.saveAll(
            listOf(
                OffenceGenerator.CASE_VIEW_MAIN_OFFENCE_TYPE,
                OffenceGenerator.CASE_VIEW_ADDITIONAL_OFFENCE_TYPE
            )
        )
        mainOffenceRepository.save(OffenceGenerator.CASE_VIEW_MAIN_OFFENCE)
        additionalOffenceRepository.save(OffenceGenerator.CASE_VIEW_ADDITIONAL_OFFENCE)

        val event = eventRepository.findById(EventGenerator.CASE_VIEW.id).orElseThrow()
        val disposalType = DisposalType(IdGenerator.getAndIncrement(), "CV", "Case View Sentence Type")
        disposalTypeRepository.save(disposalType)
        val disposal = disposalRepository.save(
            DisposalGenerator.generate(
                event = event,
                type = disposalType,
                notionalEndDate = ZonedDateTime.now().plusDays(7)
            )
        )
        DisposalGenerator.CASE_VIEW = caseViewDisposalRepository.findById(disposal.id).orElseThrow()
        RequirementGenerator.CASE_VIEW.set("disposal", DisposalGenerator.CASE_VIEW)
        requirementMainCategoryRepository.save(RequirementGenerator.CASE_VIEW.mainCategory)
        requirementRepository.save(RequirementGenerator.CASE_VIEW)
        requirementManagerRepository.save(RequirementManagerGenerator.generate(requirementId = RequirementGenerator.CASE_VIEW.id))
        courtRepository.save(CourtGenerator.DEFAULT)
        CourtAppearanceGenerator.DEFAULT = courtAppearanceRepository.save(CourtAppearanceGenerator.generate(event))
        documentRepository.saveAll(
            listOf(
                DocumentGenerator.PREVIOUS_CONVICTION,
                DocumentGenerator.CPS_PACK,
                DocumentGenerator.COURT_REPORT
            )
        )

        contactRepository.save(ContactGenerator.INITIAL_APPOINTMENT_CASE_VIEW)
    }
}

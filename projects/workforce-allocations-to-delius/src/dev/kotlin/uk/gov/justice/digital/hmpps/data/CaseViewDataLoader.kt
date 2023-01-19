package uk.gov.justice.digital.hmpps.data

import IdGenerator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
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
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalType
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

@Component
@Profile("dev", "integration-test")
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
    val documentRepository: DocumentRepository
) {
    fun loadData() {
        personRepository.save(PersonGenerator.CASE_VIEW)
        addressRepository.save(AddressGenerator.CASE_VIEW)
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

        documentRepository.saveAll(
            listOf(
                DocumentGenerator.PREVIOUS_CONVICTION,
                DocumentGenerator.CPS_PACK,
                DocumentGenerator.COURT_REPORT,
            )
        )
    }
}

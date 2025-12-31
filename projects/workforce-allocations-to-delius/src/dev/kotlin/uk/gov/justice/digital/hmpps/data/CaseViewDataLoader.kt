package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewDisposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalType
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

@Component
class CaseViewDataLoader(private val dataManager: DataManager, private val entityManager: EntityManager) {
    fun loadData() {
        dataManager.save(PersonGenerator.CASE_VIEW)
        dataManager.saveAll(
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
        dataManager.save(EventGenerator.CASE_VIEW)

        dataManager.saveAll(
            listOf(
                OffenceGenerator.CASE_VIEW_MAIN_OFFENCE_TYPE,
                OffenceGenerator.CASE_VIEW_ADDITIONAL_OFFENCE_TYPE
            )
        )
        dataManager.save(OffenceGenerator.CASE_VIEW_MAIN_OFFENCE)
        dataManager.save(OffenceGenerator.CASE_VIEW_ADDITIONAL_OFFENCE)

        val event = entityManager.find(Event::class.java, EventGenerator.CASE_VIEW.id)!!
        val disposalType = DisposalType(IdGenerator.getAndIncrement(), "CV", "Case View Sentence Type")
        dataManager.save(disposalType)
        val disposal = dataManager.save(
            DisposalGenerator.generate(
                event = event,
                type = disposalType,
                notionalEndDate = ZonedDateTime.now().plusDays(7)
            )
        )
        DisposalGenerator.CASE_VIEW = entityManager.find(CaseViewDisposal::class.java, disposal.id)!!
        RequirementGenerator.CASE_VIEW.set("disposal", DisposalGenerator.CASE_VIEW)
        dataManager.save(RequirementGenerator.CASE_VIEW.mainCategory)
        dataManager.save(RequirementGenerator.CASE_VIEW)
        dataManager.save(RequirementManagerGenerator.generate(requirementId = RequirementGenerator.CASE_VIEW.id))
        dataManager.save(CourtGenerator.DEFAULT)
        dataManager.save(CourtAppearanceGenerator.DEFAULT)
        dataManager.saveAll(
            listOf(
                DocumentGenerator.PREVIOUS_CONVICTION,
                DocumentGenerator.CPS_PACK,
                DocumentGenerator.COURT_REPORT
            )
        )
    }
}

package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff

@Component
class PersonAllocationDataLoader(private val dataManager: DataManager) {
    fun loadData() {
        dataManager.saveAll(listOf(TransferReasonGenerator.CASE_ORDER, TransferReasonGenerator.COMPONENT))

        val (dpm, dro) = createPersonWithManagers(PersonGenerator.DEFAULT)
        PersonManagerGenerator.DEFAULT = dpm
        ResponsibleOfficerGenerator.DEFAULT = dro

        val (npm, nro) = createPersonWithManagers(PersonGenerator.NEW_PM)
        PersonManagerGenerator.NEW = npm
        ResponsibleOfficerGenerator.NEW = nro

        val (hpm, hro) = createPersonWithManagers(PersonGenerator.HISTORIC_PM)
        PersonManagerGenerator.HISTORIC = hpm
        ResponsibleOfficerGenerator.HISTORIC = hro

        OrderManagerGenerator.DEFAULT = createEventWithManager(EventGenerator.DEFAULT)
        OrderManagerGenerator.NEW = createEventWithManager(EventGenerator.NEW)
        OrderManagerGenerator.HISTORIC = createEventWithManager(EventGenerator.HISTORIC)
        OrderManagerGenerator.REALLOCATION = createEventWithManager(EventGenerator.REALLOCATION)
        OrderManagerGenerator.DELETED_EVENT = createEventWithManager(EventGenerator.DELETED)
        OrderManagerGenerator.INACTIVE_EVENT =
            createEventWithManager(EventGenerator.INACTIVE, StaffGenerator.STAFF_FOR_INACTIVE_EVENT)

        dataManager.save(DisposalGenerator.DEFAULT.type)
        dataManager.saveAll(listOf(DisposalGenerator.DEFAULT, DisposalGenerator.INACTIVE))
        RequirementManagerGenerator.DEFAULT = createRequirementWithManager(RequirementGenerator.DEFAULT)
        RequirementManagerGenerator.NEW = createRequirementWithManager(RequirementGenerator.NEW)
        RequirementManagerGenerator.HISTORIC = createRequirementWithManager(RequirementGenerator.HISTORIC)
        RequirementManagerGenerator.REALLOCATION = createRequirementWithManager(RequirementGenerator.REALLOCATION)

        dataManager.save(CustodyGenerator.DEFAULT)
        dataManager.save(KeyDateGenerator.DEFAULT)
        dataManager.save(InstitutionalReportGenerator.DEFAULT)

//        dataManager.save(ContactGenerator.INITIAL_APPOINTMENT)

        dataManager.save(CourtReportGenerator.DEFAULT)

        dataManager.save(RegistrationGenerator.DEFAULT)
        dataManager.save(OgrsAssessmentGenerator.DEFAULT)
        dataManager.save(OasysAssessmentGenerator.DEFAULT)
    }

    fun createPersonWithManagers(person: Person): Pair<PersonManager, ResponsibleOfficer> {
        dataManager.save(person)
        val pm = dataManager.save(
            PersonManagerGenerator.generate(
                personId = person.id,
                startDateTime = ManagerGenerator.START_DATE_TIME,
                allocationReason = ReferenceDataGenerator.REALLOCATION_ORDER_ALLOCATION
            )
        )
        val ro = dataManager.save(
            ResponsibleOfficerGenerator.generate(
                personId = person.id,
                communityManager = pm,
                startDateTime = ManagerGenerator.START_DATE_TIME
            )
        )
        return Pair(pm, ro)
    }

    fun createEventWithManager(event: Event, staff: Staff? = null): OrderManager {
        dataManager.save(event)
        dataManager.save(OffenceGenerator.generateMainOffence(event = event))
        dataManager.save(OffenceGenerator.generateAdditionalOffence(event = event))
        return dataManager.save(
            OrderManagerGenerator.generate(
                eventId = event.id,
                startDateTime = ManagerGenerator.START_DATE_TIME,
                staff = staff ?: StaffGenerator.DEFAULT
            )
        )
    }

    fun createRequirementWithManager(requirement: Requirement): RequirementManager {
        dataManager.save(requirement)
        return dataManager.save(
            RequirementManagerGenerator.generate(
                requirementId = requirement.id,
                startDateTime = ManagerGenerator.START_DATE_TIME
            )
        )
    }
}

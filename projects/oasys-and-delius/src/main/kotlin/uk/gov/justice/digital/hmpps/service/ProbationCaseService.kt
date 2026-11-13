package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseDetails
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.PersonDetailRepository
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.getPersonDetail
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Event
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.EventRepository
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.RequirementMainCategory

@Service
class ProbationCaseService(
    private val personDetailRepository: PersonDetailRepository,
    private val eventRepository: EventRepository,
) {

    // A case is a standalone order only if all of a person's active events are standalone orders.
    // A standalone order is an event that has at least one requirement,
    // and all requirements are restrictive or unpaid work requirements.
    fun getCaseDetails(crn: String): CaseDetails {
        val person = personDetailRepository.getPersonDetail(crn)
        val events = eventRepository.findByPersonId(person.id)
        return CaseDetails(
            standaloneOrderOnly = isStandaloneOrderOnly(events),
            hasProbationRequirement = hasProbationRequirement(events)
        )
    }

    private fun isStandaloneOrderOnly(events: List<Event>): Boolean {
        if (events.isEmpty()) {
            return false
        }

        return events.all { event ->
            event.disposal?.let { disposal ->
                // All requirements must be either restrictive 'Y' or unpaid work (W)
                disposal.requirements.isNotEmpty() && disposal.requirements.all { requirement ->
                    requirement.mainCategory != null &&
                        (requirement.mainCategory.restrictive || requirement.mainCategory.code == RequirementMainCategory.UPW_RQMNT_MAIN_CATEGORY)
                }
            } ?: false
        }
    }

    private fun hasProbationRequirement(events: List<Event>): Boolean =
        events.any { event ->
            event.disposal?.requirements?.any { requirement ->
                requirement.mainCategory?.code == RequirementMainCategory.RAR_RQMNT_MAIN_CATEGORY ||
                    requirement.mainCategory?.code == RequirementMainCategory.PROBATION_RQMNT_MAIN_CATEGORY
            } == true
        }
}
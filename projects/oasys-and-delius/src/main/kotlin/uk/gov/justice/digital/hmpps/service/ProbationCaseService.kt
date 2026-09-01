package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseDetails
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.PersonDetailRepository
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.getPersonDetail
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
    fun isStandaloneOrderOnlyCase(crn: String): CaseDetails {
        val person = personDetailRepository.getPersonDetail(crn)
        val standaloneOrderOnly = isStandaloneOrderOnly(person.id)
        return CaseDetails(standaloneOrderOnly = standaloneOrderOnly)
    }

    private fun isStandaloneOrderOnly(personId: Long): Boolean {
        val events = eventRepository.findByPersonId(personId)

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
}
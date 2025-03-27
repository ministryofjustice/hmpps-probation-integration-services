package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.activity.Activity
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivitySearchRequest
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivitySearchResponse
import uk.gov.justice.digital.hmpps.client.ActivitySearchRequest
import uk.gov.justice.digital.hmpps.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getSummary

@Service
class ActivityService(
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val probationSearchClient: ProbationSearchClient
) {

    @Transactional
    fun activitySearch(
        crn: String,
        searchRequest: PersonActivitySearchRequest,
        pageable: Pageable
    ): PersonActivitySearchResponse {
        val summary = personRepository.getSummary(crn)
        val probationSearchRequest = ActivitySearchRequest(
            crn = crn,
            keywords = searchRequest.keywords,
            dateFrom = searchRequest.dateFrom,
            dateTo = searchRequest.dateTo,
            filters = searchRequest.filters
        )
        val response =
            probationSearchClient.contactSearch(probationSearchRequest, pageable.pageNumber, pageable.pageSize)
        val ids = response.results.map { it.id }

        val contactMap = contactRepository.findByPersonIdAndIdIn(summary.id, ids).associateBy { it.id }
        val activities = ids.mapNotNull { contactId -> contactMap[contactId]?.toActivity() }

        return PersonActivitySearchResponse(
            size = response.size,
            page = response.page,
            totalResults = response.totalResults,
            totalPages = response.totalPages,
            personSummary = summary.toPersonSummary(),
            activities = activities
        )
    }

    @Transactional
    fun getPersonActivity(crn: String): PersonActivity {
        val summary = personRepository.getSummary(crn)
        val contacts = contactRepository.findByPersonId(summary.id)

        return PersonActivity(
            personSummary = summary.toPersonSummary(),
            activities = contacts.map { it.toActivity() }
        )
    }

    @Transactional
    fun getPersonSentenceActivity(personId: Long, eventId: List<Long>): List<Activity> {
        return contactRepository.findByPersonIdAndEventIdIn(personId, eventId).map { it.toActivity() }
    }
}


package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.entity.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import java.time.ZonedDateTime

@Service
class SearchAuditService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository
) : AuditableService(auditedInteractionService) {
    fun auditContactSearch(auditRequest: ContactSearchAuditRequest) =
        audit(BusinessInteractionCode.SEARCH_CONTACTS, auditRequest.dateTime, auditRequest.username) { audit ->
            with(auditRequest.search) {
                audit["crn"] = crn
                audit["offenderId"] = personRepository.findByCrn(crn)!!.id
                query?.also { audit["query"] = it }
                audit["matchAllTerms"] = matchAllTerms
            }
            with(auditRequest.pagination) {
                audit["page"] = page
                audit["pageSize"] = pageSize
                sort?.also { audit["sortedBy"] = it }
                direction?.also { audit["sortDirection"] = it }
            }
        }
}

data class ContactSearchAuditRequest(
    val search: ContactSearchRequest,
    val username: String,
    val pagination: PageRequest,
    val dateTime: ZonedDateTime
)

data class ContactSearchRequest(
    val crn: String,
    val query: String?,
    val matchAllTerms: Boolean,
)

data class PageRequest(
    val page: Int,
    val pageSize: Int,
    val sort: String?,
    val direction: String?
)
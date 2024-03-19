package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

@Service
class SearchAuditService(auditedInteractionService: AuditedInteractionService) :
    AuditableService(auditedInteractionService) {
    fun auditContactSearch(auditRequest: ContactSearchAuditRequest) =
        audit(BusinessInteractionCode.SEARCH_CONTACTS, auditRequest.dateTime) { audit ->
            with(auditRequest.search) {
                audit["crn"] = crn
                query?.also { audit["query"] = it }
                audit["matchAllTerms"] = matchAllTerms
            }
            with(auditRequest.pagination) {
                audit["page"] = page
                audit["pageSize"] = pageSize
                sort?.also { audit["sort"] = it }
                direction?.also { audit["direction"] = it }
            }
        }
}

data class ContactSearchAuditRequest(
    val search: ContactSearchRequest,
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
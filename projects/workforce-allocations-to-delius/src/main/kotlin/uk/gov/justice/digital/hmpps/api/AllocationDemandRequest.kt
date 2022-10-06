package uk.gov.justice.digital.hmpps.api

data class AllocationDemandRequest(val cases: List<AllocationRequest>)

data class AllocationRequest(val crn: String, val eventNumber: String)
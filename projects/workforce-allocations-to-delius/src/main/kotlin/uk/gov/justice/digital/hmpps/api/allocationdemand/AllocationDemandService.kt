package uk.gov.justice.digital.hmpps.api.allocationdemand

import org.springframework.stereotype.Service

@Service
class AllocationDemandService(private val allocationDemandRepository: AllocationDemandRepository) {
    fun findAllocationDemand(allocationDemandRequest: AllocationDemandRequest): AllocationDemandResponse {
        return AllocationDemandResponse(
            allocationDemandRepository.findAllocationDemand(
                allocationDemandRequest.cases.map {
                    Pair(
                        it.crn,
                        it.eventNumber
                    )
                }
            )
        )
    }
}

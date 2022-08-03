package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationEvent

fun prepMessage(fileName: String, port: Int): AllocationEvent {
    val allocationEvent = ResourceLoader.allocationMessage(fileName)
    return allocationEvent.copy(
        detailUrl = allocationEvent.detailUrl.replace(
            "{wiremock.port}",
            port.toString()
        )
    )
}

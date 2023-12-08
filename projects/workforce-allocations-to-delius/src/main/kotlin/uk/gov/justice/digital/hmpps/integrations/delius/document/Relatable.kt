package uk.gov.justice.digital.hmpps.integrations.delius.document

import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocEvent

interface Relatable {
    fun findRelatedTo(): RelatedTo
}

fun DocEvent.toDocumentEvent() =
    DocumentEvent(
        if (active) EventType.CURRENT else EventType.PREVIOUS,
        eventNumber,
        mainOffence?.offence?.description ?: "",
    )

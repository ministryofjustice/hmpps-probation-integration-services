package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

interface ContactRepository : JpaRepository<Contact, Long> {
    fun findByPersonIdAndEventIdAndNsiIdAndTypeCodeAndStartTime(
        personId: Long,
        eventId: Long?,
        nsiId: Long?,
        type: String,
        startTime: ZonedDateTime
    ): Contact?
}

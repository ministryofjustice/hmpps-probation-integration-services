package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select c.date
        from Contact c
        where c.personId = :personId
        and c.eventId = :eventId
        and c.type.code in :initialAppointmentTypes
        and c.softDeleted = false
        order by c.date, c.startTime
        """
    )
    fun getInitialAppointmentDate(
        personId: Long,
        eventId: Long,
        initialAppointmentTypes: List<String> = listOf(
            ContactTypeCode.INITIAL_APPOINTMENT_IN_OFFICE.value,
            ContactTypeCode.INITIAL_APPOINTMENT_ON_DOORSTEP.value,
            ContactTypeCode.INITIAL_APPOINTMENT_HOME_VISIT.value,
            ContactTypeCode.INITIAL_APPOINTMENT_BY_VIDEO.value,
        ),
        page: PageRequest = PageRequest.of(0, 1)
    ): LocalDate?
}

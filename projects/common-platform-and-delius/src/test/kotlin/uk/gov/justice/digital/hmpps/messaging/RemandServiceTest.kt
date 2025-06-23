package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.dto.InsertEventResult
import uk.gov.justice.digital.hmpps.dto.InsertPersonResult
import uk.gov.justice.digital.hmpps.dto.InsertRemandDTO
import uk.gov.justice.digital.hmpps.service.EventService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.service.RemandService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class RemandServiceTest {

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var personService: PersonService

    @Mock
    lateinit var eventService: EventService

    @InjectMocks
    lateinit var remandService: RemandService

    @Test
    fun `telemetry called when person on remand is inserted successfully`() {
        whenever(personService.insertPerson(any(), any())).thenReturn(
            InsertPersonResult(
                person = PersonGenerator.DEFAULT,
                personManager = PersonManagerGenerator.DEFAULT,
                equality = EqualityGenerator.DEFAULT,
                address = PersonAddressGenerator.MAIN_ADDRESS
            )
        )

        whenever(eventService.insertEvent(any(), any(), any(), any(), any(), any())).thenReturn(
            InsertEventResult(
                event = EventGenerator.DEFAULT,
                mainOffence = MainOffenceGenerator.DEFAULT,
                courtAppearance = CourtAppearanceGenerator.TRIAL_ADJOURNMENT,
                contact = ContactGenerator.EAPP,
                orderManager = OrderManagerGenerator.DEFAULT
            )
        )

        remandService.insertPersonOnRemand(validInsertRemandDTO())

        verify(personService).insertPerson(any(), any())
        verify(eventService).insertEvent(any(), any(), any(), any(), any(), any())
        verify(telemetryService).trackEvent(eq("PersonCreated"), anyMap(), anyMap())
        verify(telemetryService).trackEvent(eq("EventCreated"), anyMap(), anyMap())
    }

    private fun validInsertRemandDTO(): InsertRemandDTO {
        val hearing = HearingGenerator.DEFAULT

        return InsertRemandDTO(
            defendant = hearing.prosecutionCases[0].defendants[0],
            courtCode = hearing.courtCentre.code,
            hearingOffence = hearing.prosecutionCases[0].defendants[0].offences[0],
            sittingDay = hearing.hearingDays[0].sittingDay,
            caseUrn = hearing.prosecutionCases[0].prosecutionCaseIdentifier.caseURN,
            hearingId = hearing.id
        )
    }
}
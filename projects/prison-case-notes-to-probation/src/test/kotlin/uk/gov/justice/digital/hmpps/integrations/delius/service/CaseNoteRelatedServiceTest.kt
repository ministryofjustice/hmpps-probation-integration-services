package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.repository.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.NsiRepository

@ExtendWith(MockitoExtension::class)
internal class CaseNoteRelatedServiceTest {

    @Mock
    private lateinit var eventRepository: EventRepository

    @Mock
    private lateinit var nsiRepository: NsiRepository

    @Mock
    private lateinit var featureFlags: FeatureFlags

    @InjectMocks
    private lateinit var caseNoteRelatedService: CaseNoteRelatedService

    @Test
    fun `when no nsi and exactly one custodial event`() {
        val offenderId = OffenderGenerator.DEFAULT.id
        val nomType = NomisNsiTypeGenerator.DEFAULT.caseNoteType
        val eventId = EventGenerator.CUSTODIAL_EVENT.id

        whenever(nsiRepository.findCaseNoteRelatedNsis(offenderId, nomType)).thenReturn(listOf())
        whenever(eventRepository.findActiveCustodialEvents(offenderId)).thenReturn(listOf(eventId))

        val res = caseNoteRelatedService.findRelatedCaseNoteIds(offenderId, nomType)

        assertThat(res.eventId, equalTo(eventId))
        assertNull(res.nsiId)
    }

    @Test
    fun `when no nsi and more than one custodial event exists`() {
        val offenderId = OffenderGenerator.DEFAULT.id
        val nomType = NomisNsiTypeGenerator.DEFAULT.caseNoteType

        whenever(nsiRepository.findCaseNoteRelatedNsis(offenderId, nomType)).thenReturn(listOf())
        whenever(eventRepository.findActiveCustodialEvents(offenderId)).thenReturn(listOf(1L, 2L))

        val res = caseNoteRelatedService.findRelatedCaseNoteIds(offenderId, nomType)

        assertNull(res.eventId)
        assertNull(res.nsiId)
    }

    @Test
    fun `when at least one nsi exists at event level`() {
        val offenderId = OffenderGenerator.DEFAULT.id
        val nomType = NomisNsiTypeGenerator.DEFAULT.caseNoteType
        val nsi = NsiGenerator.EVENT_CASE_NOTE_NSI

        whenever(nsiRepository.findCaseNoteRelatedNsis(offenderId, nomType)).thenReturn(listOf(nsi))

        val res = caseNoteRelatedService.findRelatedCaseNoteIds(offenderId, nomType)

        verify(eventRepository, times(0)).findActiveCustodialEvents(anyLong())

        assertThat(res.eventId, equalTo(nsi.eventId))
        assertThat(res.nsiId, equalTo(nsi.id))
    }

    @Test
    fun `when at least one nsi exists at offender level`() {
        val offenderId = OffenderGenerator.DEFAULT.id
        val nomType = NomisNsiTypeGenerator.DEFAULT.caseNoteType
        val nsi = NsiGenerator.generate(offenderId)

        whenever(nsiRepository.findCaseNoteRelatedNsis(offenderId, nomType)).thenReturn(listOf(nsi))

        val res = caseNoteRelatedService.findRelatedCaseNoteIds(offenderId, nomType)

        verify(eventRepository, times(0)).findActiveCustodialEvents(anyLong())

        assertNull(res.eventId)
        assertThat(res.nsiId, equalTo(nsi.id))
    }

    @Test
    fun `alert contacts are always set to person-level`() {
        val offenderId = OffenderGenerator.DEFAULT.id
        val nomType = CaseNoteNomisTypeGenerator.ALERT.nomisCode

        val res = caseNoteRelatedService.findRelatedCaseNoteIds(offenderId, nomType)

        assertNull(res.eventId)
        assertNull(res.nsiId)
    }
}

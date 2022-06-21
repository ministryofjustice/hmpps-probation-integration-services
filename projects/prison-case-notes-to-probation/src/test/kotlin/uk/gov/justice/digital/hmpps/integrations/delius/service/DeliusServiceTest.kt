package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteNomisTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class DeliusServiceTest {

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var caseNoteRepository: CaseNoteRepository

    @Mock
    lateinit var nomisTypeRepository: CaseNoteNomisTypeRepository

    @Mock
    lateinit var offenderRepository: OffenderRepository

    @InjectMocks
    lateinit var deliusService: DeliusService

    private val now = ZonedDateTime.now()
    private val user = UserGenerator.APPLICATION_USER
    private val caseNote = CaseNoteGenerator.EXISTING
    private val caseNoteNomisType = CaseNoteNomisTypeGenerator.DEFAULT
    private val deliusCaseNote = DeliusCaseNote(
        CaseNoteHeader("GA1234", 12345),
        CaseNoteBody("type", "subType", "Note text", now, now, "bob smith", "EST1")
    )


    @Test
    fun `successfully merges with existing case note`() {

        whenever(userService.findServiceUser()).thenReturn(user)
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(caseNote)

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith(caseNote.notes))
        assertThat(saved.notes, stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content))
    }

    @Test
    fun `successfully add new case note`() {
        val offender = OffenderGenerator.DEFAULT

        whenever(userService.findServiceUser()).thenReturn(user)
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.type)).thenReturn(Optional.of(caseNoteNomisType))
        whenever(offenderRepository.findByNomsId(deliusCaseNote.header.nomisId)).thenReturn(offender)

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith(deliusCaseNote.body.type +" "+ deliusCaseNote.body.subType))
        assertThat(saved.notes, stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content))
    }

    @Test
    fun `add new case note offender not found`() {
        whenever(userService.findServiceUser()).thenReturn(user)
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.type)).thenReturn(Optional.of(caseNoteNomisType))
        whenever(offenderRepository.findByNomsId(deliusCaseNote.header.nomisId)).thenReturn(null)

        assertThrows<OffenderNotFoundException>{
            deliusService.mergeCaseNote(deliusCaseNote)
        }
    }
}
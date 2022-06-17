package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import java.time.ZonedDateTime

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

    @Test
    fun `successfully merges with existing case note`() {

        val now = ZonedDateTime.now()
        val deliusCaseNote = DeliusCaseNote(
            CaseNoteHeader("GA1234", 12345),
            CaseNoteBody("type", "subType", "Note text", now, now, "bob smith", "EST1")
        )

        val user = User(1, "case-notes-to-probation")
        val caseNote = CaseNote(
            1,
            123,
            12345,
            CaseNoteType(2, "CaseNote", "A case note from nomis", false),
            "A Case Note from Nomis",
            now,
            now,
            now,
            user.id,
            user.id,
            now,
            0
        )


        whenever(userService.findServiceUser()).thenReturn(user)
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(caseNote)

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith(caseNote.notes))
        assertThat(saved.notes, stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content))
    }
}
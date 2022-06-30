package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
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
import uk.gov.justice.digital.hmpps.data.generator.PrisonCaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exceptions.CaseNoteTypeNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteNomisType
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DeliusServiceTest {

    @Mock
    lateinit var caseNoteRepository: CaseNoteRepository

    @Mock
    lateinit var nomisTypeRepository: CaseNoteNomisTypeRepository

    @Mock
    lateinit var offenderRepository: OffenderRepository

    @Mock
    lateinit var assignmentService: AssignmentService

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @InjectMocks
    lateinit var deliusService: DeliusService

    private val caseNote = CaseNoteGenerator.EXISTING
    private val caseNoteNomisType = CaseNoteNomisTypeGenerator.NEG
    private val nomisCaseNote = PrisonCaseNoteGenerator.EXISTING_IN_BOTH
    private val deliusCaseNote = DeliusCaseNote(
        CaseNoteHeader(OffenderGenerator.DEFAULT.nomsId, nomisCaseNote.eventId),
        CaseNoteBody(
            nomisCaseNote.type,
            nomisCaseNote.subType,
            "Note text",
            nomisCaseNote.occurrenceDateTime,
            StaffName("bob", "smith"),
            "EST1"
        )
    )
    private val probationArea = ProbationAreaGenerator.DEFAULT
    private val team = TeamGenerator.DEFAULT
    private val staff = StaffGenerator.DEFAULT

    @Test
    fun `successfully merges with existing case note`() {
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(caseNote)

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith(caseNote.notes))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content)
        )
    }

    @Test
    fun `successfully add new case note`() {
        val offender = OffenderGenerator.DEFAULT
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(
            Optional.of(
                caseNoteNomisType
            )
        )
        whenever(offenderRepository.findByNomsId(deliusCaseNote.header.nomisId)).thenReturn(offender)
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content)
        )
    }

    @Test
    fun `add new case note offender not found`() {
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(
            Optional.of(
                caseNoteNomisType
            )
        )
        whenever(offenderRepository.findByNomsId(deliusCaseNote.header.nomisId)).thenReturn(null)

        assertThrows<OffenderNotFoundException> {
            deliusService.mergeCaseNote(deliusCaseNote)
        }
    }

    @Test
    fun `add new case note case note type not found`() {
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(Optional.empty())
        whenever(nomisTypeRepository.findById(CaseNoteNomisType.DEFAULT_CODE)).thenReturn(Optional.empty())

        assertThrows<CaseNoteTypeNotFoundException> {
            deliusService.mergeCaseNote(deliusCaseNote)
        }
    }

    @Test
    fun `successfully add new case note with default type when not found`() {
        val offender = OffenderGenerator.DEFAULT
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(Optional.empty())
        whenever(nomisTypeRepository.findById(CaseNoteNomisType.DEFAULT_CODE))
            .thenReturn(Optional.of(CaseNoteNomisTypeGenerator.DEFAULT))
        whenever(offenderRepository.findByNomsId(deliusCaseNote.header.nomisId)).thenReturn(offender)
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content)
        )
        assertThat(saved.type.code, equalTo(CaseNoteNomisTypeGenerator.DEFAULT.type.code))
    }
}

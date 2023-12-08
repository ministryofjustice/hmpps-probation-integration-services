package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteNomisTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderGenerator
import uk.gov.justice.digital.hmpps.data.generator.PrisonCaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteRelatedIds
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import java.util.Optional
import java.util.Random

@ExtendWith(MockitoExtension::class)
class DeliusServiceTest {
    @Mock
    lateinit var caseNoteRepository: CaseNoteRepository

    @Mock
    lateinit var nomisTypeRepository: CaseNoteNomisTypeRepository

    @Mock
    lateinit var caseNoteTypeRepository: CaseNoteTypeRepository

    @Mock
    lateinit var offenderRepository: OffenderRepository

    @Mock
    lateinit var assignmentService: AssignmentService

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var caseNoteRelatedService: CaseNoteRelatedService

    @InjectMocks
    lateinit var deliusService: DeliusService

    private val caseNote = CaseNoteGenerator.EXISTING
    private val caseNoteNomisType = CaseNoteNomisTypeGenerator.NEG
    private val nomisCaseNote = PrisonCaseNoteGenerator.EXISTING_IN_BOTH
    private val deliusCaseNote = nomisCaseNote.toDeliusCaseNote()
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
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content),
        )
    }

    @Test
    fun `duplicate or late messages result in no-op`() {
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(caseNote)

        deliusService.mergeCaseNote(
            deliusCaseNote.copy(
                body =
                    deliusCaseNote.body.copy(
                        systemTimestamp = caseNote.lastModifiedDateTime,
                    ),
            ),
        )

        verify(caseNoteRepository, never()).save(any())
    }

    @Test
    fun `successfully add new case note with link to event`() {
        val offender = OffenderGenerator.DEFAULT
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(
            Optional.of(
                caseNoteNomisType,
            ),
        )
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(
            offender,
        )
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))
        whenever(caseNoteRelatedService.findRelatedCaseNoteIds(offender.id, deliusCaseNote.body.typeLookup()))
            .thenReturn(CaseNoteRelatedIds(EventGenerator.CUSTODIAL_EVENT.id))

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content),
        )

        assertThat(saved.eventId, equalTo(EventGenerator.CUSTODIAL_EVENT.id))
        assertNull(saved.nsiId)
    }

    @Test
    fun `successfully add new case note with link to nsi`() {
        val offender = OffenderGenerator.DEFAULT
        val nsiId = Random().nextLong()
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(
            Optional.of(
                caseNoteNomisType,
            ),
        )
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(
            offender,
        )
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))
        whenever(caseNoteRelatedService.findRelatedCaseNoteIds(offender.id, deliusCaseNote.body.typeLookup()))
            .thenReturn(CaseNoteRelatedIds(nsiId = nsiId))

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content),
        )

        assertThat(saved.nsiId, equalTo(nsiId))
        assertNull(saved.eventId)
    }

    @Test
    fun `add new case note offender not found`() {
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(
            Optional.of(
                caseNoteNomisType,
            ),
        )
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(null)

        assertThrows<OffenderNotFoundException> { deliusService.mergeCaseNote(deliusCaseNote) }
        verify(caseNoteRepository, never()).save(any())
    }

    @Test
    fun `add new case note case note type not found`() {
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(Optional.empty())
        whenever(caseNoteTypeRepository.findByCode(CaseNoteType.DEFAULT_CODE)).thenReturn(null)

        assertThrows<NotFoundException> {
            deliusService.mergeCaseNote(deliusCaseNote)
        }
    }

    @Test
    fun `successfully add new case note with default type when not found`() {
        val offender = OffenderGenerator.DEFAULT
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(Optional.empty())
        whenever(caseNoteTypeRepository.findByCode(CaseNoteType.DEFAULT_CODE)).thenReturn(CaseNoteTypeGenerator.DEFAULT)
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(
            offender,
        )
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))
        whenever(
            caseNoteRelatedService.findRelatedCaseNoteIds(
                offender.id,
                deliusCaseNote.body.typeLookup(),
            ),
        ).thenReturn(CaseNoteRelatedIds())

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content),
        )
        assertThat(saved.type.code, equalTo(CaseNoteTypeGenerator.DEFAULT.code))
        assertNull(saved.eventId)
    }

    @Test
    fun `successfully merges shorter case notes by padding`() {
        whenever(caseNoteRepository.findByNomisId(deliusCaseNote.header.noteId)).thenReturn(caseNote)

        val newContent = "Shorter case note text"
        deliusService.mergeCaseNote(
            nomisCaseNote.copy(text = newContent).toDeliusCaseNote(),
        )

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, Mockito.times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, newContent),
        )

        assertThat(caseNote.notes.length, equalTo(saved.notes.length))
    }
}

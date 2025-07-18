package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.*
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteNomisType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteRelatedIds
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.service.AssignmentService
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeliusServiceTest {

    @Mock
    lateinit var businessInteractionRepository: BusinessInteractionRepository

    @Mock
    lateinit var auditedInteractionRepository: AuditedInteractionRepository

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
    lateinit var caseNoteRelatedService: CaseNoteRelatedService

    @InjectMocks
    lateinit var deliusService: DeliusService

    private val caseNote = CaseNoteGenerator.EXISTING
    private val caseNoteNomisType = CaseNoteNomisTypeGenerator.NEG
    private val nomisCaseNote = PrisonCaseNoteGenerator.EXISTING_IN_BOTH
    private var deliusCaseNote = nomisCaseNote.toDeliusCaseNote()
    private val probationArea = ProbationAreaGenerator.DEFAULT
    private val team = TeamGenerator.DEFAULT
    private val staff = StaffGenerator.DEFAULT

    @Test
    fun `successfully merges with existing case note`() {
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(caseNote)

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content)
        )
    }

    @Test
    fun `duplicate or late messages result in no-op`() {
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn))
            .thenReturn(caseNote.copy(externalReference = deliusCaseNote.urn))

        deliusService.mergeCaseNote(
            deliusCaseNote.copy(
                body = deliusCaseNote.body.copy(
                    systemTimestamp = caseNote.lastModifiedDateTime
                )
            )
        )

        verify(caseNoteRepository, never()).save(any())
    }

    @Test
    fun `successfully add new case note with link to event`() {
        val offender = OffenderGenerator.DEFAULT
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(
            Optional.of(
                caseNoteNomisType
            )
        )
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(
            offender
        )
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))
        whenever(caseNoteRelatedService.findRelatedCaseNoteIds(offender.id, deliusCaseNote.body.typeLookup()))
            .thenReturn(CaseNoteRelatedIds(EventGenerator.CUSTODIAL_EVENT.id))

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content)
        )

        assertThat(saved.eventId, equalTo(EventGenerator.CUSTODIAL_EVENT.id))
        assertNull(saved.nsiId)
    }

    @Test
    fun `successfully add new case note with link to nsi`() {
        val offender = OffenderGenerator.DEFAULT
        val nsiId = Random().nextLong()
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(
            Optional.of(
                caseNoteNomisType
            )
        )
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(
            offender
        )
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))
        whenever(caseNoteRelatedService.findRelatedCaseNoteIds(offender.id, deliusCaseNote.body.typeLookup()))
            .thenReturn(CaseNoteRelatedIds(nsiId = nsiId))

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content)
        )

        assertThat(saved.nsiId, equalTo(nsiId))
        assertNull(saved.eventId)
    }

    @Test
    fun `add new case note offender not found`() {
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId))
            .thenReturn(OffenderGenerator.DEFAULT)
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup()))
            .thenReturn(Optional.of(caseNoteNomisType))
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(null)

        assertThrows<OffenderNotFoundException> { deliusService.mergeCaseNote(deliusCaseNote) }
        verify(caseNoteRepository, never()).save(any())
    }

    @Test
    fun `add new case note case note type not found`() {
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId))
            .thenReturn(OffenderGenerator.DEFAULT)
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(Optional.empty())
        whenever(caseNoteTypeRepository.findByCode(CaseNoteType.DEFAULT_CODE)).thenReturn(null)

        assertThrows<NotFoundException> {
            deliusService.mergeCaseNote(deliusCaseNote)
        }
    }

    @Test
    fun `successfully add new case note with default type when not found`() {
        val offender = OffenderGenerator.DEFAULT
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(Optional.empty())
        whenever(caseNoteTypeRepository.findByCode(CaseNoteType.DEFAULT_CODE)).thenReturn(CaseNoteTypeGenerator.DEFAULT)
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId)).thenReturn(
            offender
        )
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))
        whenever(
            caseNoteRelatedService.findRelatedCaseNoteIds(
                offender.id,
                deliusCaseNote.body.typeLookup()
            )
        ).thenReturn(CaseNoteRelatedIds())

        deliusService.mergeCaseNote(deliusCaseNote)

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(saved.notes, startsWith("${deliusCaseNote.body.type} ${deliusCaseNote.body.subType}"))
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, deliusCaseNote.body.content)
        )
        assertThat(saved.type.code, equalTo(CaseNoteTypeGenerator.DEFAULT.code))
        assertNull(saved.eventId)
    }

    @Test
    fun `successfully merges shorter case notes by padding`() {
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(caseNote)

        val newContent = "Shorter case note text"
        deliusService.mergeCaseNote(
            nomisCaseNote.copy(text = newContent).toDeliusCaseNote()
        )

        val caseNoteCaptor = ArgumentCaptor.forClass(CaseNote::class.java)

        verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture())

        val saved = caseNoteCaptor.value
        assertThat(
            saved.notes,
            stringContainsInOrder(deliusCaseNote.body.type, deliusCaseNote.body.subType, newContent)
        )

        assertThat(caseNote.notes.length, equalTo(saved.notes.length))
    }

    @Test
    fun `sets description for new case note with default type`() {
        givenNewCaseNote()

        deliusService.mergeCaseNote(deliusCaseNote)

        verify(caseNoteRepository).save(check {
            assertThat(
                it.description,
                equalTo("NOMIS Case Note - ${deliusCaseNote.body.type} - ${deliusCaseNote.body.subType}")
            )
        })
    }

    @Test
    fun `sets short description for new case note with alert type`() {
        deliusCaseNote = deliusCaseNote.copy(
            body = deliusCaseNote.body.copy(
                type = "ALERT",
                subType = "ACTIVE",
                content = "123",
                alertDescription = "123"
            )
        )
        givenNewCaseNote()

        deliusService.mergeCaseNote(deliusCaseNote)

        verify(caseNoteRepository).save(check { assertThat(it.description, equalTo("NOMIS 123")) })
    }

    @Test
    fun `sets truncated description for new case note with alert type and long text`() {
        deliusCaseNote = deliusCaseNote.copy(
            body = deliusCaseNote.body.copy(
                type = "ALERT",
                subType = "ACTIVE",
                content = List(200) { "X" }.joinToString(""),
                alertDescription = List(200) { "X" }.joinToString("")
            )
        )
        givenNewCaseNote()

        deliusService.mergeCaseNote(deliusCaseNote)

        verify(caseNoteRepository).save(check {
            assertThat(it.description!!.toByteArray().toList(), hasSize(200))
            assertThat(it.description, startsWith("NOMIS XXX"))
            assertThat(it.description, endsWith("XXX ~"))
        })
    }

    @Test
    fun `does not update description for existing case note`() {
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(caseNote)

        deliusService.mergeCaseNote(deliusCaseNote)

        verify(caseNoteRepository).save(check { assertThat(it.description, equalTo(null)) })
    }

    @Test
    fun `does not set description for new case note of mapped type`() {
        givenNewCaseNote(type = CaseNoteNomisTypeGenerator.NEG)

        deliusService.mergeCaseNote(deliusCaseNote)

        verify(caseNoteRepository).save(check { assertThat(it.description, equalTo(null)) })
    }

    private fun givenNewCaseNote(
        type: CaseNoteNomisType? = null,
        relatedIds: CaseNoteRelatedIds = CaseNoteRelatedIds()
    ) {
        val offender = OffenderGenerator.DEFAULT
        whenever(caseNoteRepository.findByExternalReference(deliusCaseNote.urn)).thenReturn(null)
        whenever(nomisTypeRepository.findById(deliusCaseNote.body.typeLookup())).thenReturn(Optional.ofNullable(type))
        whenever(caseNoteTypeRepository.findByCode(CaseNoteType.DEFAULT_CODE)).thenReturn(CaseNoteTypeGenerator.DEFAULT)
        whenever(offenderRepository.findByNomsIdAndSoftDeletedIsFalse(deliusCaseNote.header.nomisId))
            .thenReturn(offender)
        whenever(assignmentService.findAssignment(deliusCaseNote.body.establishmentCode, deliusCaseNote.body.staffName))
            .thenReturn(Triple(probationArea.id, team.id, staff.id))
        whenever(caseNoteRelatedService.findRelatedCaseNoteIds(offender.id, deliusCaseNote.body.typeLookup()))
            .thenReturn(relatedIds)
    }
}

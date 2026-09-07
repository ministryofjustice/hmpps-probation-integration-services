package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.model.DocumentUploadResponse
import uk.gov.justice.digital.hmpps.service.CommunityPaybackAppointmentsService
import uk.gov.justice.digital.hmpps.service.DocumentService
import uk.gov.justice.digital.hmpps.utils.Extensions.mapSorts
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/appointments")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class AppointmentsController(
    private val communityPaybackAppointmentsService: CommunityPaybackAppointmentsService,
    private val documentService: DocumentService
) {
    @GetMapping
    fun getAppointments(
        @RequestParam(required = true) username: String,
        @RequestParam(required = false) crn: String?,
        @RequestParam(required = false) eventNumber: String?,
        @RequestParam(required = false) fromDate: LocalDate?,
        @RequestParam(required = false) toDate: LocalDate?,
        @RequestParam(required = false) projectCodes: List<String>?,
        @RequestParam(required = false) projectTypeCodes: List<String>?,
        @RequestParam(required = false) outcomeCodes: List<String>?,
        @RequestParam(required = false) appointmentIds: List<Long>?,
        @RequestParam(required = false) references: List<String>?,
        @PageableDefault(page = 0, size = 10, sort = ["surname", "forename"]) pageable: Pageable
    ) = communityPaybackAppointmentsService.getAppointments(
        username, crn, eventNumber, fromDate, toDate,
        projectCodes, projectTypeCodes, outcomeCodes, appointmentIds, references,
        pageable.mapSorts(
            "name" to "lower(person.surname || person.forename)",
            "surname" to "lower(person.surname)",
            "forename" to "lower(person.forename)",
            "date" to "date"
        )
    )

    @DeleteMapping("/{reference:[0-9a-fA-F-]{36}}")
    fun deleteAppointment(@PathVariable reference: UUID) =
        communityPaybackAppointmentsService.deleteAppointment(reference)

    @PostMapping("/{appointmentId}/documents")
    fun uploadAppointmentDocument(
        @PathVariable appointmentId: Long,
        @RequestParam file: MultipartFile
    ): DocumentUploadResponse {
        val appointment = communityPaybackAppointmentsService.getAppointmentForDocumentUpload(appointmentId)
        val filename = file.originalFilename ?: "document"
        val document = documentService.uploadAppointmentDocument(
            appointment = appointment,
            filename = filename,
            file = file.bytes,
            userId = 0L
        )
        return DocumentUploadResponse(
            documentId = document.id,
            filename = document.name,
            alfrescoId = document.alfrescoId
        )
    }
}
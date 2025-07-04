package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
@RequestMapping("secure")
class CommunityApiController(
    private val probationRecordResource: ProbationRecordResource,
    private val convictionResource: ConvictionResource,
    private val registrationResource: RegistrationResource,
    private val documentResource: DocumentResource
) {
    @GetMapping("/offenders/crn/{crn}/all")
    fun offenderDetail(@PathVariable crn: String) =
        probationRecordResource.getOffenderDetail(crn)

    @GetMapping("/offenders/crn/{crn}")
    fun offenderSummary(@PathVariable crn: String) =
        probationRecordResource.getOffenderDetailSummary(crn)

    @GetMapping("/offenders/crn/{crn}/allOffenderManagers")
    fun offenderManagers(
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) includeProbationAreaTeams: Boolean
    ) = probationRecordResource.getAllOffenderManagers(crn, includeProbationAreaTeams)

    @GetMapping("/offenders/crn/{crn}/convictions")
    fun convictions(
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) activeOnly: Boolean
    ) = convictionResource.getConvictionsForOffenderByCrn(crn, activeOnly)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}")
    fun convictionForOffenderByCrnAndConvictionId(
        @PathVariable crn: String,
        @PathVariable convictionId: Long
    ) = convictionResource.getConvictionForOffenderByCrnAndConvictionId(crn, convictionId)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/requirements")
    fun convictionRequirements(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
        @RequestParam(required = false, defaultValue = "true") activeOnly: Boolean,
        @RequestParam(required = false, defaultValue = "true") excludeSoftDeleted: Boolean
    ) = convictionResource.getRequirementsForConviction(crn, convictionId, activeOnly, excludeSoftDeleted)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/nsis")
    fun nsisByCrnAndConvictionId(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
        @RequestParam(required = true) nsiCodes: List<String>,
    ) = convictionResource.getNsisByCrnAndConvictionId(crn, convictionId, nsiCodes)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/attendancesFilter")
    fun convictionByIdAttendances(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ) = convictionResource.getConvictionAttendances(crn, convictionId)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/nsis/{nsiId}")
    fun nsisByNisId(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
        @PathVariable nsiId: Long,
    ) = convictionResource.getNsiByNsiId(crn, convictionId, nsiId)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/pssRequirements")
    fun pssByCrnAndConvictionId(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ) = convictionResource.getPssRequirementsByConvictionId(crn, convictionId)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/courtAppearances")
    fun convictionByIdCourtAppearances(

        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ) = convictionResource.getConvictionCourtAppearances(crn, convictionId)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/courtReports")
    fun convictionByIdCourtReports(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ) = convictionResource.getConvictionCourtReports(crn, convictionId)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/licenceConditions")
    fun convictionByIdLicenceConditions(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ) = convictionResource.getConvictionLicenceConditions(crn, convictionId)

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/sentenceStatus")
    fun convictionByIdSentenceStatus(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ) = convictionResource.getConvictionSentenceStatus(crn, convictionId)

    @GetMapping("/offenders/crn/{crn}/documents/grouped")
    fun documentsGrouped(
        @PathVariable crn: String,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) subtype: String?
    ) = documentResource.getOffenderDocumentsGrouped(crn, type, subtype)

    @GetMapping("/offenders/crn/{crn}/documents/{documentId}")
    fun downloadDocument(
        @PathVariable crn: String,
        @PathVariable documentId: String
    ) = documentResource.getOffenderDocumentById(crn, documentId)

    @GetMapping("/offenders/crn/{crn}/registrations")
    fun registrations(
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) activeOnly: Boolean
    ) = registrationResource.getOffenderRegistrations(crn, activeOnly)
}


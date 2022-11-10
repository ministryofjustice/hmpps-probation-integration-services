package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.model.PersonName
import uk.gov.justice.digital.hmpps.model.StaffGrade
import uk.gov.justice.digital.hmpps.model.StaffResponse

@Service
class StaffService(
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val staffRepository: StaffRepository,
) {
    @Transactional
    fun getStaffInApprovedPremises(approvedPremisesCode: String, keyWorkersOnly: Boolean, pageable: Pageable): Page<StaffResponse> {
        if (!approvedPremisesRepository.existsByCodeCode(approvedPremisesCode))
            throw NotFoundException("Approved Premises", "code", approvedPremisesCode)

        return if (keyWorkersOnly) {
            staffRepository.findKeyWorkersLinkedToApprovedPremises(approvedPremisesCode, pageable).map {
                it.toResponse(approvedPremisesCode)
            }
        } else {
            staffRepository.findAllStaffLinkedToApprovedPremisesTeam(approvedPremisesCode, pageable).map {
                it.toResponse(approvedPremisesCode)
            }
        }
    }

    fun Staff.toResponse(approvedPremisesCode: String) = StaffResponse(
        code = code,
        name = PersonName(forename, surname, middleName),
        grade = grade?.let { grade -> StaffGrade(grade.code, grade.description) },
        keyWorker = approvedPremises.map { ap -> ap.code.code }.contains(approvedPremisesCode)
    )
}

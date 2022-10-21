package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
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
    fun getStaffInApprovedPremises(approvedPremisesCode: String, pageable: Pageable): Page<StaffResponse> {
        if (!approvedPremisesRepository.existsByCodeCode(approvedPremisesCode))
            throw NotFoundException("Approved Premises", "code", approvedPremisesCode)

        return staffRepository.findAllByApprovedPremisesCodeCode(approvedPremisesCode, pageable).map {
            StaffResponse(
                code = it.code,
                name = PersonName(it.forename, it.surname, it.middleName),
                grade = StaffGrade(
                    code = it.grade.code,
                    description = it.grade.description
                )
            )
        }
    }
}

package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.model.*

@Service
class StaffService(
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val staffRepository: StaffRepository,
    private val ldapTemplate: LdapTemplate
) {
    @Transactional
    fun getStaffInApprovedPremises(
        approvedPremisesCode: String,
        keyWorkersOnly: Boolean,
        pageable: Pageable
    ): PagedModel<StaffResponse> {
        if (!approvedPremisesRepository.existsByCodeCode(approvedPremisesCode)) {
            throw NotFoundException("Approved Premises", "code", approvedPremisesCode)
        }

        return if (keyWorkersOnly) {
            staffRepository.findKeyWorkersLinkedToApprovedPremises(approvedPremisesCode, pageable).map {
                it.toResponse(approvedPremisesCode)
            }
        } else {
            staffRepository.findAllStaffLinkedToApprovedPremisesTeam(approvedPremisesCode, pageable).map {
                it.toResponse(approvedPremisesCode)
            }
        }.let { PagedModel(it) }
    }

    fun getStaffByUsername(username: String) =
        staffRepository.findByUsername(username)?.toStaffDetail(ldapTemplate.findByUsername<LdapUser>(username))
            ?: throw NotFoundException(
                "Staff",
                "username",
                username
            )

    fun Staff.toResponse(approvedPremisesCode: String) = StaffResponse(
        code = code,
        name = PersonName(forename, surname, middleName),
        grade = grade?.let { grade -> StaffGrade(grade.code, grade.description) },
        keyWorker = approvedPremises.map { ap -> ap.code.code }.contains(approvedPremisesCode)
    )

    fun Staff.toStaffDetail(ldapUser: LdapUser?) = StaffDetail(
        telephoneNumber = ldapUser?.telephoneNumber,
        email = ldapUser?.email,
        teams = teams.map {
            Team(
                code = it.code,
                name = it.description,
                borough = Borough(code = it.district.borough.code, description = it.district.borough.description),
                ldu = Ldu(code = it.district.code, name = it.district.description),
                startDate = it.startDate,
                endDate = it.endDate
            )
        },
        username = user!!.username,
        name = PersonName(forename, surname, middleName),
        code = code,
        probationArea = ProbationArea(
            code = probationArea.code,
            description = probationArea.description
        ),
        active = isActive(),
        staffIdentifier = id
    )
}

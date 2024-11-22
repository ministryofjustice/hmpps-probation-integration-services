package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ManagedOffender
import uk.gov.justice.digital.hmpps.api.model.PDUHead
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.api.model.StaffName
import uk.gov.justice.digital.hmpps.entity.BoroughRepository
import uk.gov.justice.digital.hmpps.entity.Caseload.CaseloadRole
import uk.gov.justice.digital.hmpps.entity.CaseloadRepository
import uk.gov.justice.digital.hmpps.entity.LdapUser
import uk.gov.justice.digital.hmpps.entity.StaffRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class StaffService(
    private val ldapTemplate: LdapTemplate,
    private val staffRepository: StaffRepository,
    private val boroughRepository: BoroughRepository,
    private val caseloadRepository: CaseloadRepository,
) {
    fun findStaff(username: String): Staff = staffRepository.findByUserUsernameIgnoreCase(username)
        ?.let { ldapTemplate.populateUserDetails(it).asStaff() }
        ?: throw NotFoundException("Staff", "username", username)

    fun findStaffByCode(code: String): Staff = staffRepository.findByCode(code)
        ?.let { ldapTemplate.populateUserDetails(it).asStaff() }
        ?: throw NotFoundException("Staff", "code", code)

    fun findPDUHeads(boroughCode: String): List<PDUHead> = boroughRepository.findActiveByCode(boroughCode)?.pduHeads
        ?.map { ldapTemplate.populateUserDetails(it).asPDUHead() }
        ?: listOf()

    fun findStaffForUsernames(usernames: List<String>): List<StaffName> =
        staffRepository.findByUserUsernameInIgnoreCase(usernames).map { it.asStaffName() }

    fun getManagedOffenders(staffCode: String): List<ManagedOffender> =
        caseloadRepository.findByStaffCodeAndRoleCode(
            staffCode,
            CaseloadRole.OFFENDER_MANAGER.value
        ).map {
            it.asManagedOffender()
        }

    private fun LdapTemplate.populateUserDetails(staff: uk.gov.justice.digital.hmpps.entity.Staff) =
        staff.apply {
            user?.apply {
                ldapTemplate.findByUsername<LdapUser>(username)?.let {
                    email = it.email
                    telephoneNumber = it.telephoneNumber
                }
            }
        }
}

fun uk.gov.justice.digital.hmpps.entity.Staff.asStaff() = Staff(
    id,
    code,
    name(),
    teams?.map { it.asTeam() } ?: listOf(),
    provider.asProvider(),
    user?.username,
    user?.email,
    user?.telephoneNumber,
    isUnallocated()
)

fun uk.gov.justice.digital.hmpps.entity.Staff.asPDUHead() = PDUHead(
    name(),
    user?.email
)

fun uk.gov.justice.digital.hmpps.entity.Staff.asStaffName() = StaffName(
    id,
    name(),
    code,
    user?.username
)


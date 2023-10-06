package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.PDUHead
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.api.model.StaffName
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.BoroughRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.TeamRepository
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class StaffService(
    private val ldapTemplate: LdapTemplate,
    private val staffRepository: StaffRepository,
    private val boroughRepository: BoroughRepository,
    private val teamRepository: TeamRepository
) {
    fun findStaff(username: String): Staff =
        staffRepository.findByUserUsername(username)?.let { staff ->
            staff.user?.apply {
                email = ldapTemplate.findEmailByUsername(username)
            }
            staff.asStaff()
        } ?: throw NotFoundException("Staff", "username", username)

    fun findPDUHeads(boroughCode: String): List<PDUHead> =
        boroughRepository.findActiveByCode(boroughCode)?.pduHeads?.map {
            it.let { pduHead ->
                pduHead.user?.apply {
                    email = ldapTemplate.findEmailByUsername(username)
                }
                pduHead.asPDUHead()
            }
        } ?: listOf()

    fun findStaffForUsernames(usernames: List<String>): List<StaffName> =
        staffRepository.findByUserUsernameIn(usernames).map {
            it.asStaffName()
        }
}

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff.asStaff() = Staff(
    code,
    name(),
    teams?.map { it.asTeam() } ?: listOf(),
    user?.username,
    user?.email,
    isUnallocated()
)

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff.asPDUHead() = PDUHead(
    name(),
    user?.email
)

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff.asStaffName() = StaffName(
    name(),
    code
)

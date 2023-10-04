package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class StaffService(
    private val ldapTemplate: LdapTemplate,
    private val staffRepository: StaffRepository
) {
    fun findStaff(username: String): Staff =
        staffRepository.findByUserUsername(username)?.let { staff ->
            staff.user?.apply {
                email = ldapTemplate.findEmailByUsername(username)
            }
            staff.asStaff()
        } ?: throw NotFoundException("Staff", "username", username)
}

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff.asStaff() = Staff(
    code,
    name(),
    teams.map { it.asTeam() },
    user?.username,
    user?.email,
    isUnallocated()
)

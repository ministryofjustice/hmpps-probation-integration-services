package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsernames

@Service
class UserLocationService(
    private val staffUserRepository: StaffUserRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun getUserOfficeLocations(username: String): UserOfficeLocation {
        val user = staffUserRepository.getUser(username)

        val userLocations = staffUserRepository.findUserOfficeLocations(user.id)

        return UserOfficeLocation(
            Name(user.forename, user.forename2, user.surname),
            userLocations.map { it.toLocationDetails() }
        )
    }

    fun getStaffByTeam(code: String): StaffTeam {
        val staffInTeam = staffUserRepository.findStaffByTeam(code)
        val emailsByUsername = ldapTemplate.fetchEmailsByStaff(staffInTeam)
        return StaffTeam(staffInTeam.map { it.toUser(email = emailsByUsername[it.username]) })
    }
}

fun Location.toLocationDetails(): LocationDetails =
    LocationDetails(id, code.trim(), description, Address(buildingNumber, streetName, townCity, county, postcode))

fun StaffAndRole.toUser(email: String? = null): User =
    User(
        code, username, if (username != "Unallocated") "$forename $surname (${role})" else username,
        email = email,
        name = Name(forename, surname = surname)
    )

fun StaffUser.toUser(): User = User(
    staff!!.code, username, "$forename $surname (${staff.role!!.description})",
    email = email, name = Name(forename, forename2, surname = surname)
)

fun LdapTemplate.fetchEmailsByStaff(staff: List<StaffAndRole>): Map<String, String?> {
    val usernames = staff.map { it.username }.filter { it != "Unallocated" }
    return if (usernames.isEmpty()) emptyMap() else findEmailByUsernames(usernames)
}
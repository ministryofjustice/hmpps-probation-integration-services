package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Location
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffAndRole
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffUserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getUser

@Service
class UserLocationService(private val staffUserRepository: StaffUserRepository) {

    fun getUserOfficeLocations(username: String): UserOfficeLocation {
        val user = staffUserRepository.getUser(username)

        val userLocations = staffUserRepository.findUserOfficeLocations(user.id)

        return UserOfficeLocation(
            Name(user.forename, user.forename2, user.surname),
            userLocations.map { it.toLocationDetails() }
        )
    }

    fun getStaffByTeam(code: String): StaffTeam =
        StaffTeam(staffUserRepository.findStaffByTeam(code).map { it.toUser() })
}

fun Location.toLocationDetails(): LocationDetails =
    LocationDetails(id, code.trim(), description, Address(buildingNumber, streetName, townCity, county, postcode))

fun StaffAndRole.toUser(): User =
    User(username, if (username != "Unallocated") "$forename $surname (${role})" else username)

fun StaffUser.toUser(): User = User(username, "$forename $surname (${staff!!.role!!.description})")
package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.Address
import uk.gov.justice.digital.hmpps.api.model.sentence.LocationDetails
import uk.gov.justice.digital.hmpps.api.model.sentence.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.UserOfficeLocation
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Location
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

    fun Location.toLocationDetails(): LocationDetails =
        LocationDetails(id, description, Address(buildingNumber, streetName, townCity, county, postcode))
}
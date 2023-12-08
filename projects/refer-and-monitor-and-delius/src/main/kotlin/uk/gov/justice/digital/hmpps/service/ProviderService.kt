package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.api.model.DeliveryUnit
import uk.gov.justice.digital.hmpps.api.model.OfficeLocation
import uk.gov.justice.digital.hmpps.api.model.Region
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Location
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PduRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getCrsProvider

@Service
class ProviderService(
    private val providerRepository: ProviderRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val locationRepository: LocationRepository,
    private val pduRepository: PduRepository,
) {
    fun findActiveOfficeLocations() =
        locationRepository.findAllLocationsForProvider(providerRepository.getCrsProvider().id).map {
            OfficeLocation(it.code, it.description, it.address(), it.telephoneNumber)
        }

    fun findCrsAssignationDetails(locationCode: String?): CrsAssignation {
        val provider = providerRepository.getCrsProvider()
        val team = teamRepository.getByCode(Team.INTENDED_TEAM_CODE)
        val staff = staffRepository.getByCode(Staff.INTENDED_STAFF_CODE)
        val location = locationCode?.let { locationRepository.getByCode(it) }
        return CrsAssignation(provider, team, staff, location)
    }

    fun findSelectableDeliveryUnits() =
        pduRepository.findAllSelectable().map {
            DeliveryUnit(it.code, it.description, Region(it.region.code, it.region.description))
        }
}

data class CrsAssignation(val provider: Provider, val team: Team, val staff: Staff, val location: Location?)

fun Location.address() = Address.from(buildingName, buildingNumber, streetName, district, townCity, county, postcode)

package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.DeliveryUnit
import uk.gov.justice.digital.hmpps.api.model.Region
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*

@Service
class ProviderService(
    private val providerRepository: ProviderRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val locationRepository: LocationRepository,
    private val pduRepository: PduRepository
) {

    fun findActiveOfficeLocations() = locationRepository.findAllLocationsForProvider(
        providerRepository.getCrsProvider().id
    ).map(Location::location)

    fun findCrsAssignationDetails(locationCode: String?): CrsAssignation {
        val provider = providerRepository.getCrsProvider()
        val team = teamRepository.getByCode(Team.INTENDED_TEAM_CODE)
        val staff = staffRepository.getByCode(Staff.INTENDED_STAFF_CODE)
        val location = locationCode?.let { locationRepository.getByCode(it) }
        return CrsAssignation(provider, team, staff, location)
    }

    fun findSelectableDeliveryUnits() = pduRepository.findAllSelectable().map {
        DeliveryUnit(it.code, it.description, Region(it.region.code, it.region.description))
    }
}

data class CrsAssignation(val provider: Provider, val team: Team, val staff: Staff, val location: Location?)

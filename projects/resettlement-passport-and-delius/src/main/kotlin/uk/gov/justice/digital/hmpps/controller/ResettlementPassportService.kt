package uk.gov.justice.digital.hmpps.controller

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.NsiManagerRepository
import uk.gov.justice.digital.hmpps.entity.NsiRepository
import uk.gov.justice.digital.hmpps.entity.PersonAddress
import uk.gov.justice.digital.hmpps.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.DutyToReferNSI
import uk.gov.justice.digital.hmpps.model.MainAddress
import uk.gov.justice.digital.hmpps.model.Officer

@Service
class ResettlementPassportService(val addressRepository: PersonAddressRepository, val nsiRepository: NsiRepository, val nsiManagerRepository: NsiManagerRepository) {
    fun getDutyToReferNSI(crn: String): DutyToReferNSI {
        // get the main address for this person and also the most recent NSI type of DTR associated with this person
        val dutyToRefer = nsiRepository.findDutyToReferByCrn(crn) ?: throw NotFoundException("DTR NSI", "crn", crn)
        val nsiManager = nsiManagerRepository.getNSIManagerByNsi(dutyToRefer.id)
        val mainAddress = addressRepository.getMainAddressByCrn(crn)?.toModel()
        return DutyToReferNSI(
            dutyToRefer.subType.description,
            dutyToRefer.referralDate,
            nsiManager?.probationArea?.description,
            nsiManager?.team?.description,
            nsiManager?.staff?.officer(),
            dutyToRefer.status.description,
            dutyToRefer.actualStartDate,
            dutyToRefer.notes,
            mainAddress
        )
    }
}

private fun PersonAddress.toModel() = MainAddress(buildingName, addressNumber, streetName, district, town, county, postcode, noFixedAbode)
private fun Staff.officer() = Officer(forename, surname, middleName)

package uk.gov.justice.digital.hmpps.controller

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.ApiController.IdentifierType
import uk.gov.justice.digital.hmpps.controller.ApiController.IdentifierType.CRN
import uk.gov.justice.digital.hmpps.controller.ApiController.IdentifierType.NOMS
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
    fun getDutyToReferNSI(value: String, type: IdentifierType): DutyToReferNSI {
        // get the main address for this person and also the most recent NSI type of DTR associated with this person
        val dutyToRefer = when (type) {
            CRN -> nsiRepository.findDutyToReferByCrn(value) ?: throw NotFoundException("DTR NSI", "crn", value)
            NOMS -> nsiRepository.findDutyToReferByNoms(value) ?: throw NotFoundException("DTR NSI", "noms", value)
        }
        val nsiManager = nsiManagerRepository.getNSIManagerByNsi(dutyToRefer.id)
        val mainAddress = addressRepository.getMainAddressByPersonId(dutyToRefer.person.id)?.toModel()
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
fun PersonAddress.toModel() = MainAddress(buildingName, addressNumber, streetName, district, town, county, postcode, noFixedAbode)
fun Staff.officer() = Officer(forename, surname, middleName)

package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.*

@Service
class ProbationCaseService(
    private val personDetailRepository: PersonDetailRepository,
    private val personAddressRepository: PersonAddressRepository
) {
    fun findCase(crn: String): CaseDetails =
        personDetailRepository.getPersonDetail(crn).asCaseDetail()

    fun PersonDetail.asCaseDetail(): CaseDetails {
        val mainAddress = personAddressRepository.mainAddress(id)?.asAddress()
        return CaseDetails(
            Identifiers(crn, noms?.trim(), pnc?.trim(), cro?.trim()),
            Person(Name(surname, firstName, listOfNotNull(secondName, thirdName)), dob, gender?.codeDescription()),
            Profile.from(language?.codeDescription(), ethnicity?.codeDescription(), religion?.codeDescription()),
            ContactDetails.from(mainAddress, emailAddress, telephoneNumber, mobileNumber)
        )
    }

    fun PersonAddress.asAddress() = Address(buildingName, addressNumber, streetName, district, town, county, postcode)
}
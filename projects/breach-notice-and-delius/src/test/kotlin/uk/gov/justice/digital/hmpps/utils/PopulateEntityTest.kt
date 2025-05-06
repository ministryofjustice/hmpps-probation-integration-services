package uk.gov.justice.digital.hmpps.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.service.populateEntityType

class PopulateEntityTest {

    @ParameterizedTest
    @CsvSource(
        "CONTACT,CONTACT",
        "APPROVED_PREMISES_REFERRAL,APREFERRAL",
        "COURT_REPORT,COURTREPORT",
        "INSTITUTIONAL_REPORT,INSTITUTIONALREPORT",
        "NSI,PROCESSCONTACT",
        "PERSONAL_CIRCUMSTANCE,PERSONALCIRCUMSTANCE",
        "UPW_APPOINTMENT,UPWAPPOINTMENT"
    )
    fun `test alfresco return values`(entityType: String, expectedValue: String) {
        assertEquals(expectedValue, populateEntityType(entityType))
    }
}
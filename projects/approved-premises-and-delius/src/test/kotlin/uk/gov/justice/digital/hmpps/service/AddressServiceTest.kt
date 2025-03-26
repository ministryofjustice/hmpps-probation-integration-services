package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.MAIN_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.PREV_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class AddressServiceTest {
    @Mock
    lateinit var personAddressRepository: PersonAddressRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @InjectMocks
    lateinit var addressService: AddressService

    @Test
    fun `end-dates main address on departure if it is an approved premises address`() {
        whenever(personAddressRepository.findMainAddress(PersonGenerator.DEFAULT.id))
            .thenReturn(AddressGenerator.PERSON_AP_ADDRESS)
        whenever(referenceDataRepository.findByCodeAndDatasetCode(PREV_ADDRESS_STATUS.code, ADDRESS_STATUS.code))
            .thenReturn(PREV_ADDRESS_STATUS)

        val updatedAddress = addressService.endMainAddressOnDeparture(PersonGenerator.DEFAULT, LocalDate.of(2025, 3, 1))!!

        assertThat(updatedAddress.endDate, equalTo(LocalDate.of(2025, 3, 1)))
        assertThat(updatedAddress.status.code, equalTo(PREV_ADDRESS_STATUS.code))
    }

    @Test
    fun `does not end-date main address on departure if it isn't an approved premises address`() {
        whenever(personAddressRepository.findMainAddress(PersonGenerator.DEFAULT.id))
            .thenReturn(AddressGenerator.PERSON_ADDRESS)

        val updatedAddress = addressService.endMainAddressOnDeparture(PersonGenerator.DEFAULT, LocalDate.of(2025, 3, 1))

        assertThat(updatedAddress, nullValue())
        assertThat(AddressGenerator.PERSON_ADDRESS.endDate, nullValue())
        assertThat(AddressGenerator.PERSON_ADDRESS.status.code, equalTo(MAIN_ADDRESS_STATUS.code))
    }
}

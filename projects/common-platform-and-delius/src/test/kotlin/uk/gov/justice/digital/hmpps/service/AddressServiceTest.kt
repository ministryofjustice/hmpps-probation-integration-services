package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository

@ExtendWith(MockitoExtension::class)
class AddressServiceTest {
    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var addressRepository: PersonAddressRepository

    @InjectMocks
    private lateinit var addressService: AddressService

    @Test
    fun `successfully inserts address record`() {
        val mainAddress = AddressGenerator.MAIN_ADDRESS
        whenever(addressRepository.save(mainAddress)).thenReturn(mainAddress)

        addressService.insertAddress(mainAddress)

        verify(addressRepository).save(check {
            assertThat(it.personId, equalTo(mainAddress.personId))
            assertThat(it.start, equalTo(mainAddress.start))
            assertThat(it.status, equalTo(mainAddress.status))
            assertThat(it.type, equalTo(mainAddress.type))
        })
    }
}

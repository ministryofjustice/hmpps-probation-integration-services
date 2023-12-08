package uk.gov.justice.digital.hmpps.services

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.exceptions.StaffCodeExhaustedException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository

@ExtendWith(MockitoExtension::class)
class OfficerCodeGeneratorTest {
    @Mock
    private lateinit var staffRepository: StaffRepository

    @InjectMocks
    private lateinit var officerCodeGenerator: OfficerCodeGenerator

    private val probationAreaCode: String = ProviderGenerator.DEFAULT_PROVIDER.code

    @Test
    fun `if all possible options exhausted exception thrown`() {
        whenever(staffRepository.getLatestStaffReference(anyString()))
            .thenAnswer {
                val regex = it.arguments[0] as String
                val prefix = regex.substring(1, regex.length - 6)
                prefix + "999"
            }

        val ex =
            assertThrows<StaffCodeExhaustedException> {
                officerCodeGenerator.generateFor(probationAreaCode)
            }

        assertThat(ex.message, containsString(probationAreaCode))
    }

    @Test
    fun `if no result returned for a given probation area A001 is used`() {
        whenever(staffRepository.getLatestStaffReference(anyString())).thenReturn(null)

        val code = officerCodeGenerator.generateFor(probationAreaCode)

        assertThat(code, equalTo(probationAreaCode + "A001"))
    }

    @Test
    fun `roll over to next letter is successful`() {
        whenever(staffRepository.getLatestStaffReference(anyString()))
            .thenAnswer {
                val regex = it.arguments[0] as String
                val prefix = regex.substring(1, regex.length - 6)
                if (prefix == "${probationAreaCode}A") {
                    prefix + "999"
                } else {
                    "${probationAreaCode}B001"
                }
            }

        val code = officerCodeGenerator.generateFor(probationAreaCode)

        assertThat(code, equalTo(probationAreaCode + "B002"))
    }
}

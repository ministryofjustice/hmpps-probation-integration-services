package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.repository.PrisonStaffRepository

@ExtendWith(MockitoExtension::class)
class OfficerCodeGeneratorTest {

    @Mock
    private lateinit var staffRepository: PrisonStaffRepository

    @InjectMocks
    private lateinit var officerCodeGenerator: OfficerCodeGenerator

    private val probationAreaCode: String = ProbationAreaGenerator.DEFAULT.code

    @Test
    fun `db function is called to get next staff reference`() {
        val number = "981"
        whenever(staffRepository.getNextStaffReference(anyString()))
            .thenAnswer {
                val prefix = it.arguments[0] as String
                prefix + number
            }

        val res = officerCodeGenerator.generateFor(probationAreaCode)
        assertThat(res, equalTo(probationAreaCode + number))
    }
}

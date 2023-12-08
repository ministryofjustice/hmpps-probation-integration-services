package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.MappaDetail
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.CATEGORIES
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.LEVELS
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.generate
import uk.gov.justice.digital.hmpps.entity.Category
import uk.gov.justice.digital.hmpps.entity.Level
import uk.gov.justice.digital.hmpps.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.Registration
import uk.gov.justice.digital.hmpps.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.entity.findMappa
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class CaseDetailServiceTest {
    @Mock
    internal lateinit var personRepository: PersonRepository

    @Mock
    internal lateinit var registrationRepository: RegistrationRepository

    @Mock
    internal lateinit var personManagerRepository: PersonManagerRepository

    @InjectMocks
    internal lateinit var service: CaseDetailService

    @ParameterizedTest
    @MethodSource("mappaDetails")
    fun `mappa details are correctly mapped`(
        registration: Registration,
        mappa: MappaDetail,
    ) {
        val crn = "Z123456"
        whenever(registrationRepository.findMappa(crn)).thenReturn(registration)

        val res = service.findMappaDetail(crn)
        assertThat(res, equalTo(mappa))
    }

    @Test
    fun `service throws not found exception if no mappa details found`() {
        val nfe = assertThrows<NotFoundException> { service.findMappaDetail("N123456") }
        assertThat(nfe.message, equalTo("No MAPPA details found for N123456"))
    }

    companion object {
        private val detail =
            MappaDetail(
                null,
                null,
                null,
                null,
                LocalDate.now(),
                null,
            )

        @JvmStatic
        fun mappaDetails() =
            listOf(
                Arguments.of(generate(), detail),
                Arguments.of(
                    generate(category = CATEGORIES[Category.X9.name]),
                    detail.copy(category = 0, categoryDescription = "Description of X9"),
                ),
                Arguments.of(
                    generate(category = CATEGORIES[Category.M1.name], level = LEVELS[Level.M0.name]),
                    detail.copy(
                        category = 1,
                        categoryDescription = "Description of M1",
                        level = 0,
                        levelDescription = "Description of M0",
                    ),
                ),
                Arguments.of(
                    generate(category = CATEGORIES[Category.M2.name], level = LEVELS[Level.M1.name]),
                    detail.copy(
                        category = 2,
                        categoryDescription = "Description of M2",
                        level = 1,
                        levelDescription = "Description of M1",
                    ),
                ),
                Arguments.of(
                    generate(category = CATEGORIES[Category.M3.name], level = LEVELS[Level.M2.name]),
                    detail.copy(
                        category = 3,
                        categoryDescription = "Description of M3",
                        level = 2,
                        levelDescription = "Description of M2",
                    ),
                ),
                Arguments.of(
                    generate(category = CATEGORIES[Category.M4.name], level = LEVELS[Level.M3.name]),
                    detail.copy(
                        category = 4,
                        categoryDescription = "Description of M4",
                        level = 3,
                        levelDescription = "Description of M3",
                    ),
                ),
            )
    }
}

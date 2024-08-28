package uk.gov.justice.digital.hmpps.services.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.services.toMappa
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class MappaServiceTest {

    @Test
    fun `mappa null values `() {

        val mappa =  RegistrationGenerator.generate(
            RegistrationGenerator.TYPE_MAPPA,
            null,
            LocalDate.now().minusDays(3),
            category = null
        ).toMappa()
        assertThat(mappa.category, equalTo(null))
        assertThat(mappa.level, equalTo(null))
    }
}

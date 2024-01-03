package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.DeliveryUnit
import uk.gov.justice.digital.hmpps.api.model.Region
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryUnitResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `correctly returns all pdu with region`() {
        val deliveryUnits = mockMvc.perform(get("/probation-delivery-units").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<DeliveryUnit>>()

        assertThat(deliveryUnits.size, equalTo(1))
        val provider = ProviderGenerator.INTENDED_PROVIDER
        val pdu = ProviderGenerator.PROBATION_BOROUGH
        assertThat(
            deliveryUnits,
            hasItem(
                DeliveryUnit(
                    pdu.code,
                    pdu.description,
                    Region(provider.code, provider.description)
                )
            )
        )
    }
}

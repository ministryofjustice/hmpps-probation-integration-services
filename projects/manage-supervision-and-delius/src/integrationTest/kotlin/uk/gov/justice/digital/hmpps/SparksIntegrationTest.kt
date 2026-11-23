package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.NON_SELECTABLE_SPARK
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.SPARK_1
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.SPARK_2
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class SparksIntegrationTest : IntegrationTestBase() {
    @Test
    fun `returns selectable sparks reference data`() {
        val response = mockMvc.get("/sparks/reference-data") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<CodeAndDescription>>()

        assertThat(response).containsExactlyInAnyOrder(
            CodeAndDescription(SPARK_1.code, SPARK_1.description),
            CodeAndDescription(SPARK_2.code, SPARK_2.description),
        )
        assertThat(response).doesNotContain(
            CodeAndDescription(NON_SELECTABLE_SPARK.code, NON_SELECTABLE_SPARK.description),
        )
    }

    @Test
    fun `requires authentication`() {
        mockMvc.get("/sparks/reference-data")
            .andExpect { status { isUnauthorized() } }
    }
}

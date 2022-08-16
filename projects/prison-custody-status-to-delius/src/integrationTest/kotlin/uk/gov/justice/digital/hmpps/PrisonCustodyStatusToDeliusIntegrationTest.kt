package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("integration-test")
internal class PrisonCustodyStatusToDeliusIntegrationTest {
    @Test
    fun `the context loads`() {}
}

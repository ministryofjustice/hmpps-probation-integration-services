package uk.gov.justice.digital.hmpps.message

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.util.UUID

class HmppsMessageTest {

    @Test
    fun `attributes can be added and retrieved`() {
        val uuid = UUID.randomUUID().toString()
        val message = HmppsMessage("{}")
        message.metadata["specialId"] = MessageAttribute("String", uuid)
        assertThat(message.metadata["specialId"]?.value, equalTo(uuid))
    }
}

package uk.gov.justice.digital.hmpps.message

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.util.UUID

class NotificationTest {
    @Test
    fun `attributes can be added and retrieved`() {
        val uuid = UUID.randomUUID().toString()
        val message = Notification("{}")
        message.attributes["specialId"] = MessageAttribute("String", uuid)
        assertThat(message.attributes["specialId"]?.value, equalTo(uuid))
    }
}

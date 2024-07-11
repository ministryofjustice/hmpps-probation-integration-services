package uk.gov.justice.digital.hmpps.documentation

import com.fasterxml.jackson.databind.ObjectMapper
import org.openfolder.kotlinasyncapi.model.AsyncApi
import org.openfolder.kotlinasyncapi.model.channel.*
import org.openfolder.kotlinasyncapi.springweb.service.AsyncApiSerializer
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class AsyncApiReferencingSerializer(val objectMapper: ObjectMapper) : AsyncApiSerializer {
    override fun AsyncApi.serialize(): String = objectMapper.writeValueAsString(this.also { asyncApi ->
        asyncApi.components?.channels?.values?.forEach { value ->
            val channel = value as Channel
            listOfNotNull(channel.publish, channel.subscribe).forEach { it.replaceMessagesWithReferences() }
        }
    })

    private fun Operation.replaceMessagesWithReferences() {
        val messages = (message as OneOfReferencableMessages?)?.oneOf
        messages?.filterIsInstance<Message>()?.forEach { messages.replaceWithReference(it) }
    }

    private fun ReferencableMessagesList.replaceWithReference(message: Message) {
        val name = message.name
        if (message.name != null) {
            // If the name is populated, replace with remote schema reference
            reference { ref("https://raw.githubusercontent.com/ministryofjustice/hmpps-domain-events/main/spec/schemas/$name.yml") }
            removeIf { it is Message && it.name == name }
        } else if (message.title != null) {
            // Otherwise update the name to match the title
            message.name = message.title
        }
    }
}
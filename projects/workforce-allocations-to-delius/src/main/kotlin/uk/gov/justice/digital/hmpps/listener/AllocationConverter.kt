package uk.gov.justice.digital.hmpps.listener

// @Component
// class AllocationConverter(private val om: ObjectMapper) : MessageConverter {
//    override fun toMessage(allocationMessage: Any, session: Session): Message {
//        val message = session.createTextMessage()
//        message.text = om.writeValueAsString(AllocationMessage(om.writeValueAsString(allocationMessage)))
//        return message
//    }
//
//    override fun fromMessage(message: Message): AllocationEvent {
//        if (message is TextMessage) {
//            return om.readValue(
//                om.readValue(message.text, AllocationMessage::class.java).message,
//                AllocationEvent::class.java
//            )
//        }
//        throw IllegalArgumentException("Unable to convert $message to a AllocationEvent")
//    }
// }

// data class AllocationMessage(@JsonProperty("Message") val message: String)

package uk.gov.justice.digital.hmpps.crimeportalgateway.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser.Companion.CSCI_HDR_NS

data class MessageHeader(
    @field:NotNull
    @JacksonXmlProperty(namespace = CSCI_HDR_NS, localName = "MessageID")
    val messageID: MessageID?,
    @field:NotNull
    @JacksonXmlProperty(namespace = CSCI_HDR_NS, localName = "TimeStamp")
    val timeStamp: String?,
    @field:NotNull
    @JacksonXmlProperty(namespace = CSCI_HDR_NS, localName = "MessageType")
    val messageType: String?,
    @field:NotNull
    @JacksonXmlProperty(namespace = CSCI_HDR_NS, localName = "From")
    val from: String?,
    @field:NotNull
    @JacksonXmlProperty(namespace = CSCI_HDR_NS, localName = "To")
    val to: String?,
)

class MessageID(
    @field:NotBlank
    @JacksonXmlProperty(localName = "UUID")
    val uuid: String? = null,
    @JacksonXmlProperty(localName = "RelatesTo")
    val relatesTo: String? = null,
)

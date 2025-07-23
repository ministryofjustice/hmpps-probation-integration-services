package uk.gov.justice.digital.hmpps.crimeportalgateway.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser.Companion.CSCI_BODY_NS
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser.Companion.CSCI_HDR_NS
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser.Companion.CSC_STATUS_NS

@JacksonXmlRootElement(localName = "CSCI_Message_Type")
data class MessageType(
    @field:NotNull
    @field:Valid
    @JacksonXmlProperty(namespace = CSCI_HDR_NS, localName = "MessageHeader")
    val messageHeader: MessageHeader?,
    @field:Valid
    @field:NotNull
    @JacksonXmlProperty(namespace = CSCI_BODY_NS, localName = "MessageBody")
    val messageBody: MessageBodyType?,
    @JacksonXmlProperty(namespace = CSC_STATUS_NS, localName = "MessageStatus")
    val messageStatus: MessageStatus?,
)

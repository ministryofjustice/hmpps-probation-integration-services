package uk.gov.justice.digital.hmpps.crimeportalgateway.messaging

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class MessageParser<T>(
    @Qualifier("messageXmlMapper") private val xmlMapper: XmlMapper,
    private val validator: Validator,
) {
    @Throws(JsonProcessingException::class)
    fun parseMessage(
        xml: String?,
        type: Class<T>,
    ): T {
        val javaType: JavaType = xmlMapper.typeFactory.constructType(type)
        val message: T = xmlMapper.readValue(xml, javaType)
        validate(message)
        return message
    }

    private fun validate(messageType: T) {
        val errors = validator.validate<Any>(messageType)
        if (errors.isNotEmpty()) {
            throw ConstraintViolationException(errors)
        }
    }
}

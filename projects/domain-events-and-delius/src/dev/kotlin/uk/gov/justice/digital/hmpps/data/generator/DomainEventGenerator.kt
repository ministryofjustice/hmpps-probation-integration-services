package uk.gov.justice.digital.hmpps.data.generator

import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEvent

object DomainEventGenerator {
    fun generate(filename: String) = generate(
        messageBody = ResourceUtils.getFile("classpath:messages/$filename.json").readText(),
        messageAttributes = ResourceUtils.getFile("classpath:messages/$filename-attributes.json").readText()
    )

    fun generate(
        messageBody: String,
        messageAttributes: String
    ) = DomainEvent(
        id = IdGenerator.getAndIncrement(),
        messageBody = messageBody,
        messageAttributes = messageAttributes
    )
}

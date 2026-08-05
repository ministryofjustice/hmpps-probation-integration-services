package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ContactAlert

object ContactAlertGenerator {
    val DEFAULT_ALERT = ContactAlert(
        contact = ContactGenerator.ALERT_CONTACT,
        staff = ProviderGenerator.DEFAULT_STAFF,
        id = id(),
    )
}
package uk.gov.justice.digital.hmpps

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateUpdateService

@RestController
class KeyDateController(private val custodyDateUpdateService: CustodyDateUpdateService) {
    @PostMapping
    fun updateKeyDates(@RequestBody nomsNumbers: List<String>) {
        nomsNumbers.forEach { custodyDateUpdateService.updateCustodyKeyDates(it, true) }
    }
}

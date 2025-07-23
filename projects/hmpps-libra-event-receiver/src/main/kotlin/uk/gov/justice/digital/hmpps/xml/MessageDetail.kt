package uk.gov.justice.digital.hmpps.xml

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MessageDetail(
    val courtCode: String,
    val courtRoom: Int,
    val hearingDate: String,
) {
    fun asFileNameStem(): String {
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
        return "$hearingDate-$courtCode-$courtRoom-$currentDateTime"
    }
}

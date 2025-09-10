package uk.gov.justice.digital.hmpps.integration

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface AccreditedProgrammesClient {
    @GetExchange
    fun getStatusInfo(uri: URI): StatusInfo
}

data class StatusInfo(
    val newStatus: Status,
    val sourcedFromEntityType: EntityType,
    val sourcedFromEntityId: Long,
) {
    enum class Status(val contactTypeCode: String) {
        AWAITING_ALLOCATION("EIBB"),
        AWAITING_ASSESSMENT("EIBA"),
        BREACH("EIBI"),
        DEPRIORITISED("EIBG"),
        DEFERRED("EIBL"),
        ON_PROGRAMME("EIBE"),
        PROGRAMME_COMPLETE("EIBF"),
        RECALL("EIBJ"),
        RETURN_TO_COURT("EIBH"),
        SCHEDULED("EIBD"),
        SUITABLE_BUT_NOT_READY("EIBC"),
        WITHDRAWN("EIBK"),
    }

    enum class EntityType {
        LICENCE_CONDITION, REQUIREMENT
    }
}
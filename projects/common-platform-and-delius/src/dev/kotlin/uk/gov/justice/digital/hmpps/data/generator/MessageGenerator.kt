package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.messaging.CommonPlatformHearing
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val COMMON_PLATFORM_EVENT = ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing")
    val COMMON_PLATFORM_EVENT_VALIDATION_ERROR = ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-validation-error")
}

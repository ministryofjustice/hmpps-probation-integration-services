package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.messaging.CommonPlatformHearing
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val COMMON_PLATFORM_EVENT = ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing")
    val COMMON_PLATFORM_EVENT_WITH_CRN =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-with-crn")
    val COMMON_PLATFORM_EVENT_DOB_ERROR =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-dob-error")
    val COMMON_PLATFORM_EVENT_NO_CASES =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-no-cases")
    val COMMON_PLATFORM_EVENT_BLANK_ADDRESS =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-blank-address")
    val COMMON_PLATFORM_EVENT_NO_REMAND =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-no-remand")
    val COMMON_PLATFORM_EVENT_NULL_FIELDS =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-null-fields")
    val COMMON_PLATFORM_EVENT_FUTURE_HEARING_DATES =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-future-hearing-dates")
    val COMMON_PLATFORM_EVENT_MULTIPLE_OFFENCES =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-multiple-offences")
    val COMMON_PLATFORM_EVENT_NO_PNC_SLASH =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-no-pnc-slash")
    val COMMON_PLATFORM_EVENT_UNKNOWN_OFFENCE =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-unknown-offence")
    val COMMON_PLATFORM_EVENT_MULTIPLE_DEFENDANTS =
        ResourceLoader.message<CommonPlatformHearing>("common-platform-hearing-multiple-defendants")
}

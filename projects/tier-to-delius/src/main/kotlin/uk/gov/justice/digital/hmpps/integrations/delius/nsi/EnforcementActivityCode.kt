package uk.gov.justice.digital.hmpps.integrations.delius.nsi

enum class EnforcementActivityCode(val value: String) {
    BRE01("BRE01"),
    BRE02("BRE02"),
    BRE03("BRE03"),
    BRE04("BRE04"),
    BRE05("BRE05"),
    BRE06("BRE06"),
    BRE07("BRE07"),
    BRE08("BRE08"),
    BRE10("BRE10"),
    BRE13("BRE13"),
    BRE14("BRE14"),
    BRE16("BRE16"),
    REC01("REC01"),
    REC02("REC02"),
    ;

    companion object {
        val stringValues = values().map { it.value }
    }
}

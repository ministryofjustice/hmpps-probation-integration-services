package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

enum class ApprovedPremisesCategoryCode(
    val value: String,
    val categoryMappings: CategoryMappings
) {
    BAIL_ASSESSMENT("A", CategoryMappings(SentenceType.BailPlacement to ReleaseType.BailAssessment)),
    BAIL_PLACEMENT("B", CategoryMappings(SentenceType.BailPlacement to ReleaseType.BailSentence)),
    COMMUNITY_ORDER(
        "C",
        CategoryMappings(
            SentenceType.CommunityOrder to ReleaseType.RiskManagement,
            SentenceType.ExtendedDeterminate to ReleaseType.RiskManagement,
            SentenceType.SuspendedSentence to ReleaseType.RiskManagement
        )
    ),
    HDC("H", CategoryMappings(SentenceType.StandardDeterminate to ReleaseType.HomeDetentionCurfew)),
    LIFE("J", CategoryMappings(SentenceType.LifeSentence to ReleaseType.Licence)),
    IPP("K", CategoryMappings(SentenceType.IndeterminatePublicProtection to ReleaseType.Licence)),
    LICENCE("L", CategoryMappings(SentenceType.StandardDeterminate to ReleaseType.Licence)),
    PAROLE_DIRECTED(
        "M",
        CategoryMappings(
            SentenceType.ExtendedDeterminate to ReleaseType.ParoleDirectedLicence,
            SentenceType.StandardDeterminate to ReleaseType.ParoleDirectedLicence
        )
    ),
    VOLUNTARY_MAPPA(
        "MAP",
        CategoryMappings(*ReleaseType.entries.map { SentenceType.NonStatutory to it }.toTypedArray())
    ),
    TEMPORARY_LICENCE(
        "N",
        CategoryMappings(
            SentenceType.ExtendedDeterminate to ReleaseType.TemporaryLicence,
            SentenceType.IndeterminatePublicProtection to ReleaseType.TemporaryLicence,
            SentenceType.LifeSentence to ReleaseType.TemporaryLicence,
            SentenceType.StandardDeterminate to ReleaseType.TemporaryLicence
        )
    ),
    OTHER("O", CategoryMappings()),
    POST_RECALL("S", CategoryMappings(SentenceType.StandardDeterminate to ReleaseType.ReReleasedPostRecall)),
    ORA_PSS("U", CategoryMappings(SentenceType.StandardDeterminate to ReleaseType.PostSentenceSupervision)),
    RESIDENCY_REQUIREMENT(
        "X",
        CategoryMappings(
            SentenceType.CommunityOrder to ReleaseType.ResidencyManagement,
            SentenceType.SuspendedSentence to ReleaseType.ResidencyManagement
        )
    ),
    EXTENDED_DETERMINATE(
        "Y", CategoryMappings(
            SentenceType.ExtendedDeterminate to ReleaseType.Licence,
            SentenceType.ExtendedDeterminate to ReleaseType.ExtendedDeterminateLicence
        )
    );

    companion object {
        fun from(sentenceType: SentenceType, releaseType: ReleaseType) =
            entries.firstOrNull { it.categoryMappings.matches(sentenceType, releaseType) }
                ?: OTHER
    }
}

enum class SentenceType(val value: String) {
    BailPlacement("bailPlacement"),
    CommunityOrder("communityOrder"),
    ExtendedDeterminate("extendedDeterminate"),
    IndeterminatePublicProtection("ipp"),
    LifeSentence("life"),
    NonStatutory("nonStatutory"),
    StandardDeterminate("standardDeterminate"),
    SuspendedSentence("suspendedSentence"),
    Unknown("unknown");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.value == value } ?: Unknown
    }
}

enum class ReleaseType(val value: String) {
    BailAssessment("bailAssessment"),
    BailSentence("bailSentence"),
    ExtendedDeterminateLicence("extendedDeterminateLicence"),
    HomeDetentionCurfew("hdc"),
    Licence("licence"),
    ParoleDirectedLicence("paroleDirectedLicence"),
    PostSentenceSupervision("pss"),
    ResidencyManagement("residencyManagement"),
    RiskManagement("riskManagement"),
    TemporaryLicence("rotl"),
    ReReleasedPostRecall("reReleasedPostRecall"),
    NotApplicable("not_applicable");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.value == value } ?: NotApplicable
    }
}

class CategoryMappings(vararg sentenceTypeReleaseTypes: Pair<SentenceType, ReleaseType>) {
    private val mappings = sentenceTypeReleaseTypes.toList()
    fun matches(sentenceType: SentenceType?, releaseType: ReleaseType?) =
        mappings.any { it.first == sentenceType && it.second == releaseType }
}

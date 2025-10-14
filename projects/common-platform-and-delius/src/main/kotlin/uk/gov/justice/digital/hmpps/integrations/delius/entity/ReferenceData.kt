package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,

    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,

    @Column(name = "reference_data_master_id", nullable = false)
    val datasetId: Long,

    @Column(name = "code_description", length = 500, nullable = false)
    val description: String,
) {
    enum class GenderCode(val commonPlatformValue: String, val deliusValue: String) {
        MALE("MALE", "M"),
        FEMALE("FEMALE", "F"),
        OTHER("OTHER", "O"),
        NOT_KNOWN("NOT_KNOWN", "N"),
        NOT_SPECIFIED("NOT_SPECIFIED", "N")
    }

    enum class StandardRefDataCode(val code: String) {
        INITIAL_ALLOCATION("IN1"),
        TRIAL_ADJOURNMENT_APPEARANCE("T"),
        ADDRESS_MAIN_STATUS("M"),
        AWAITING_ASSESSMENT("A16"),
        REMANDED_IN_CUSTODY_STATUS("Y"),
        REMANDED_IN_CUSTODY_OUTCOME("112"),
        GUILTY("G"),
        NOT_GUILTY("N"),
        NOT_KNOWN_PLEA("Q")
    }

    enum class NationalityCode(val commonPlatformValue: String) {
        AFGA("AFG"),
        ALBA("ALB"),
        ALGE("DZA"),
        AMER("USA"),
        ASM("ASM"),
        ANDO("AND"),
        ANGOL("AGO"),
        AG("AG"),
        ANTIG("ATG"),
        ARGEN("ARG"),
        ARME("ARM"),
        AB("AB"),
        AUSI("AUS"),
        AUST("AUT"),
        AZERB("AZE"),
        BAHA("BHS"),
        BAHR("BHR"),
        BANGL("BGD"),
        BARB("BRB"),
        BELA("BLR"),
        BELG("BEL"),
        BELI("BLZ"),
        BENI("BEN"),
        BM("BM"),
        BHUT("BTN"),
        BOLI("BOL"),
        BOSNI("BIH"),
        MOTS("BWA"),
        BRAZ("BRA"),
        BRIT("GBR"),
        VG("VG"),
        BRUN("BRN"),
        BULG("BGR"),
        HV("BFA"),
        BURM("MMR"),
        BURU("BDI"),
        CAMB("KHM"),
        CAMER("CMR"),
        CANA("CAN"),
        CAVER("CPV"),
        KY("KY"),
        CF("CAF"),
        CHAD("TCD"),
        CHIL("CHL"),
        CHINA("CHN"),
        CXR("CXR"),
        CGB("GNB"),
        CDR("DOM"),
        CCK("CCK"),
        COLO("COL"),
        COMO("COM"),
        COND("COG"),
        CONR("COD"),
        CONG("CONG"),
        COK("COK"),
        COSRI("CRI"),
        CROAT("HRV"),
        CUBA("CUB"),
        CYPR("CYP"),
        CZEC("CZE"),
        DANE("DNK"),
        DJIB("DJI"),
        DOMI("DMA"),
        DUTCH("NLD"),
        AN("AN"),
        EATIM("TLS"),
        ECUA("ECU"),
        EGYP("EGY"),
        EMIR("ARE"),
        EQUATO("GNQ"),
        ERI("ERI"),
        ESTO("EST"),
        ETHI("ETH"),
        FO("FO"),
        FIJI("FJI"),
        FILIP("PHL"),
        FINN("FIN"),
        FREN("FRA"),
        FG("FG"),
        PYF("PYF"),
        GABO("GAB"),
        GAMB("GMB"),
        GE("GEO"),
        GERM("DEU"),
        GHAN("GHA"),
        GI("GI"),
        GREE("GRC"),
        GL("GL"),
        GREN("GRD"),
        GLP("GLP"),
        GU("GU"),
        GUAT("GTM"),
        GRN("GRN"),
        GUIN("GIN"),
        GUYA("GUY"),
        HAIT("HTI"),
        HOND("HND"),
        HKG("HKG"),
        HUNG("HUN"),
        ICE("ISL"),
        CT("KIR"),
        INDI("IND"),
        INDO("IDN"),
        IRAN("IRN"),
        IRAQ("IRQ"),
        IRISH("IRL"),
        ISRA("ISR"),
        ITAL("ITA"),
        IVOR("CIV"),
        JAM("JAM"),
        JAP("JPN"),
        JSM("JSM"),
        JORD("JOR"),
        KAZA("KAZ"),
        KENY("KEN"),
        SW("SW"),
        KOS("XK"),
        KUWA("KWT"),
        KN("KGZ"),
        LA("LAO"),
        LATV("LVA"),
        LEBA("LBN"),
        LIBE("LBR"),
        LIBY("LBY"),
        LIEC("LIE"),
        LITHU("LTU"),
        LUX("LUX"),
        MAC("MAC"),
        MACE("MKD"),
        MO("MO"),
        MG("MDG"),
        MLAW("MWI"),
        MALA("MYS"),
        MALD("MDV"),
        ML("MLI"),
        MALT("MLT"),
        MAN("MAN"),
        MAR("MHL"),
        MTQ("MTQ"),
        MARI("MRT"),
        MAUR("MUS"),
        MEXI("MEX"),
        MICR("FSM"),
        MOLD("MDA"),
        MONA("MCO"),
        MONGO("MNG"),
        MNE("MNE"),
        MS("MS"),
        MORO("MAR"),
        LS("LSO"),
        MOZA("MOZ"),
        NAMI("NAM"),
        NAUR("NRU"),
        NEPA("NPL"),
        NCL("NCL"),
        NZEA("NZL"),
        NICA("NIC"),
        NIGERIA("NGA"),
        NIGER("NER"),
        NIU("NIU"),
        VY("VUT"),
        NFK("NFK"),
        NKOR("PRK"),
        NORW("NOR"),
        OMAN("OMN"),
        PAKN("PAK"),
        PALA("PLW"),
        PSE("PSE"),
        PANA("PAN"),
        PNGU("PNG"),
        PARA("PRY"),
        PERU("PER"),
        PCN("PCN"),
        POLE("POL"),
        PORTU("PRT"),
        PRI("PRI"),
        QUAT("QAT"),
        REF("REF"),
        REU("REU"),
        RID("RID"),
        ROMA("ROU"),
        RUSS("RUS"),
        RWAN("RWA"),
        WA("WA"),
        SH("SH"),
        STLU("STLU"),
        SALV("SLV"),
        SAMO("WSM"),
        SANM("SMR"),
        STP("STP"),
        SRK("SRK"),
        SAARA("SAU"),
        SENE("SEN"),
        SECR("SECR"),
        SEYC("SYC"),
        SRB("SRB"),
        SILE("SLE"),
        SING("SGP"),
        SLOV("SVK"),
        SLENE("SVN"),
        SOLO("SLB"),
        SOMA("SOM"),
        SOAFR("ZAF"),
        SKOR("KOR"),
        SSUDAN("SSD"),
        SPAN("ESP"),
        SRIL("LKA"),
        STATE("STATE"),
        SUDAN("SDN"),
        SURIN("SURIN"),
        SWAZI("SWZ"),
        SWEDE("SWE"),
        SWIS("CHE"),
        SYRI("SYR"),
        TAIW("TAIW"),
        TA("TJK"),
        TANZ("TZA"),
        THAI("THA"),
        TOGO("TGO"),
        TKL("TKL"),
        TNGA("TON"),
        TRIN("TTO"),
        TRS("TRS"),
        TUNI("TUN"),
        TURK("TUR"),
        TU("TKM"),
        TCI("TCI"),
        TV("TUV"),
        UGAN("UGA"),
        UKRA("UKR"),
        UNKNOWN("UNKNOWN"),
        URUG("URY"),
        UZBE("UZB"),
        VTC("VAT"),
        VENE("VEN"),
        VIET("VNM"),
        VC("VC"),
        WAL("WAL"),
        YEMIN("YEMIN"),
        ZAM("ZMB"),
        ZIM("ZWE"),
    }

    enum class EthnicityCode {
        A1,
        A2,
        A3,
        A4,
        A9,
        B1,
        B2,
        B9,
        M1,
        M2,
        M3,
        M9,
        NS,
        O2,
        O9,
        W1,
        W2,
        W3,
        W4,
        W5,
        W9,
    }
}

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Convert(converter = DatasetCodeConverter::class)
    @Column(name = "code_set_name", nullable = false)
    val code: DatasetCode
)

enum class DatasetCode(val value: String) {
    OM_ALLOCATION_REASON("OM ALLOCATION REASON"),
    ORDER_ALLOCATION_REASON("ORDER ALLOCATION REASON"),
    COURT_APPEARANCE_TYPE("COURT APPEARANCE TYPE"),
    ADDRESS_STATUS("ADDRESS STATUS"),
    ADDRESS_TYPE("ADDRESS TYPE"),
    REMAND_STATUS("REMAND STATUS"),
    PLEA("PLEA"),
    COURT_APPEARANCE_OUTCOME("COURT APPEARANCE OUTCOME"),
    GENDER("GENDER"),
    NATIONALITY("NATIONALITY"),
    ETHNICITY("ETHNICITY");

    companion object {
        private val index = DatasetCode.entries.associateBy { it.value }
        fun fromString(value: String): DatasetCode =
            index[value] ?: throw IllegalArgumentException("Invalid DatasetCode")
    }
}

@Converter
class DatasetCodeConverter : AttributeConverter<DatasetCode, String> {
    override fun convertToDatabaseColumn(attribute: DatasetCode): String = attribute.value

    override fun convertToEntityAttribute(dbData: String): DatasetCode = DatasetCode.fromString(dbData)
}

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @Column
    val description: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column
    val endDate: LocalDate? = null
)

@Entity
@Immutable
@Table(name = "court")
class Court(
    @Id
    @Column(name = "court_id", nullable = false)
    val id: Long,

    @Column(columnDefinition = "char(6)", nullable = false)
    val code: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column(name = "court_name", length = 80)
    val name: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column(name = "court_ou_code")
    val ouCode: String?
)

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: DatasetCode): ReferenceData?
}

fun ReferenceDataRepository.initialOmAllocationReason() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
        DatasetCode.OM_ALLOCATION_REASON
    )
        ?: throw NotFoundException(
            "Allocation Reason",
            "code",
            ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code
        )

fun ReferenceDataRepository.initialOrderAllocationReason() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
        DatasetCode.ORDER_ALLOCATION_REASON
    )
        ?: throw NotFoundException(
            "Allocation Reason",
            "code",
            ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code
        )

fun ReferenceDataRepository.trialAdjournmentAppearanceType() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.TRIAL_ADJOURNMENT_APPEARANCE.code,
        DatasetCode.COURT_APPEARANCE_TYPE
    )
        ?: throw NotFoundException(
            "Court Appearance Type",
            "code",
            ReferenceData.StandardRefDataCode.TRIAL_ADJOURNMENT_APPEARANCE.code
        )

fun ReferenceDataRepository.remandedInCustodyOutcome() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_OUTCOME.code,
        DatasetCode.COURT_APPEARANCE_OUTCOME
    )
        ?: throw NotFoundException(
            "Court Appearance Outcome",
            "code",
            ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_OUTCOME.code
        )

fun ReferenceDataRepository.remandedInCustodyStatus() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_STATUS.code,
        DatasetCode.REMAND_STATUS
    )
        ?: throw NotFoundException(
            "Remand Status",
            "code",
            ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_STATUS.code
        )

fun ReferenceDataRepository.mainAddressStatus() =
    findByCodeAndDatasetCode(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code, DatasetCode.ADDRESS_STATUS)
        ?: throw NotFoundException("Address Status", "code", ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code)

fun ReferenceDataRepository.awaitingAssessmentAddressType() =
    findByCodeAndDatasetCode(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code, DatasetCode.ADDRESS_TYPE)
        ?: throw NotFoundException("Address Type", "code", ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code)

interface CourtRepository : JpaRepository<Court, Long> {
    fun findByOuCode(ouCode: String): Court?
}

fun CourtRepository.getByOuCode(ouCode: String) =
    findByOuCode(ouCode) ?: throw NotFoundException("Court", "ouCode", ouCode)

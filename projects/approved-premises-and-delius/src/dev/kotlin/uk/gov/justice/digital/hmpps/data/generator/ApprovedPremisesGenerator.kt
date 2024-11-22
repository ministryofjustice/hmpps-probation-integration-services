package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.Address
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.location.OfficeLocation
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApGroupLink
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApGroupLinkId
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object ApprovedPremisesGenerator {
    val DEFAULT = generate(ReferenceDataGenerator.NHC_Q001, AddressGenerator.Q001)
    val NO_STAFF = generate(ReferenceDataGenerator.NHC_Q002, AddressGenerator.Q002)
    val AP_GROUP_LINKS = listOf(generateGroupLink(DEFAULT), generateGroupLink(NO_STAFF))

    val AP_Q005 = generate(ReferenceDataGenerator.NHC_Q005, AddressGenerator.Q005)
    val AP_Q049 = generate(ReferenceDataGenerator.NHC_Q049, AddressGenerator.Q049)
    val AP_Q095 = generate(ReferenceDataGenerator.NHC_Q095, AddressGenerator.Q095)
    val AP_Q701 = generate(ReferenceDataGenerator.NHC_Q701, AddressGenerator.Q701)
    val AP_Q702 = generate(ReferenceDataGenerator.NHC_Q702, AddressGenerator.Q702)
    val AP_Q703 = generate(ReferenceDataGenerator.NHC_Q703, AddressGenerator.Q703)
    val AP_Q704 = generate(ReferenceDataGenerator.NHC_Q704, AddressGenerator.Q704)
    val AP_Q705 = generate(ReferenceDataGenerator.NHC_Q705, AddressGenerator.Q705)
    val AP_Q706 = generate(ReferenceDataGenerator.NHC_Q706, AddressGenerator.Q706)
    val AP_Q707 = generate(ReferenceDataGenerator.NHC_Q707, AddressGenerator.Q707)
    val AP_Q708 = generate(ReferenceDataGenerator.NHC_Q708, AddressGenerator.Q708)
    val AP_Q709 = generate(ReferenceDataGenerator.NHC_Q709, AddressGenerator.Q709)
    val AP_Q710 = generate(ReferenceDataGenerator.NHC_Q710, AddressGenerator.Q710)
    val AP_Q711 = generate(ReferenceDataGenerator.NHC_Q711, AddressGenerator.Q711)
    val AP_Q712 = generate(ReferenceDataGenerator.NHC_Q712, AddressGenerator.Q712)
    val AP_Q713 = generate(ReferenceDataGenerator.NHC_Q713, AddressGenerator.Q713)
    val AP_Q714 = generate(ReferenceDataGenerator.NHC_Q714, AddressGenerator.Q714)
    val AP_Q715 = generate(ReferenceDataGenerator.NHC_Q715, AddressGenerator.Q715)
    val AP_Q716 = generate(ReferenceDataGenerator.NHC_Q716, AddressGenerator.Q716)

    val ALL_STAFFED_APS = listOf(
        DEFAULT, AP_Q005, AP_Q049, AP_Q095,
        AP_Q701, AP_Q702, AP_Q703, AP_Q704, AP_Q705, AP_Q706, AP_Q707, AP_Q708, AP_Q709, AP_Q710,
        AP_Q711, AP_Q712, AP_Q713, AP_Q714, AP_Q715, AP_Q716
    )

    val ALL_APS = listOf(
        NO_STAFF
    ) + ALL_STAFFED_APS

    fun generate(
        code: ReferenceData,
        address: Address,
        pa: ProbationArea = ProbationAreaGenerator.DEFAULT,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ApprovedPremises(id, code, address, pa, selectable)

    fun generateGroupLink(
        approvedPremises: ApprovedPremises,
        apGroup: ReferenceData = ReferenceDataGenerator.REFERRAL_GROUP
    ): ApGroupLink = ApGroupLink(ApGroupLinkId(approvedPremises.id, apGroup.id))
}

object OfficeLocationGenerator {
    val DEFAULT = generate(ProbationAreaGenerator.DEFAULT.code + ApprovedPremisesGenerator.DEFAULT.code.code)
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = OfficeLocation(id, code)
}

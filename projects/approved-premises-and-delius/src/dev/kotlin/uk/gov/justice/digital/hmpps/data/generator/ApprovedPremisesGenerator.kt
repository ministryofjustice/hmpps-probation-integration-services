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
    val E2E_TEST = generate(ReferenceDataGenerator.NHC_Q710, AddressGenerator.Q710)
    val AP_GROUP_LINKS = listOf(generateGroupLink(DEFAULT), generateGroupLink(NO_STAFF))

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

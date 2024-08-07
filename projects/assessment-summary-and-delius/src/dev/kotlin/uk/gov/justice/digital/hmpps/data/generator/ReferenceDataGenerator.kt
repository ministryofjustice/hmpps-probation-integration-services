package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.ZonedDateTime

object ReferenceDataGenerator {
    val BUSINESS_INTERACTIONS = BusinessInteractionCode.entries.map {
        BusinessInteraction(
            IdGenerator.getAndIncrement(),
            it.code,
            ZonedDateTime.now()
        )
    }
    val FLAG_DATASET = generateDataset(Dataset.Code.REGISTER_TYPE_FLAG.value)
    val DEFAULT_FLAG = generateReferenceData("1", dataset = FLAG_DATASET)
    val OFFENCES = listOf("80400", "00857").map { generateOffence(it) }
    val COURTS = listOf("CRT150", "LVRPCC").map { generateCourt(it) }
    val REQ_MAIN_CATS = listOf("RM38").map { generateReqMainCat(it) }
    val DOMAIN_EVENT_TYPE_DATASET = generateDataset(Dataset.Code.DOMAIN_EVENT_TYPE.value)
    val DOMAIN_EVENT_TYPES = listOf(ReferenceData.Code.REGISTRATION_ADDED, ReferenceData.Code.REGISTRATION_DEREGISTERED)
        .map { generateReferenceData(it.value, dataset = DOMAIN_EVENT_TYPE_DATASET) }
    val DISPOSAL_TYPE = generateDisposalType("NC")

    fun generateReferenceData(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, dataset.id, id)

    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)

    fun generateCourt(code: String, selectable: Boolean = true, id: Long = IdGenerator.getAndIncrement()) =
        Court(code, selectable, id)

    fun generateOffence(code: String, id: Long = IdGenerator.getAndIncrement()) = Offence(code, id)

    fun generateReqMainCat(code: String, id: Long = IdGenerator.getAndIncrement()) = RequirementMainCategory(code, id)

    fun generateDisposalType(sentenceType: String, id: Long = IdGenerator.getAndIncrement()) =
        DisposalType(sentenceType, id)
}
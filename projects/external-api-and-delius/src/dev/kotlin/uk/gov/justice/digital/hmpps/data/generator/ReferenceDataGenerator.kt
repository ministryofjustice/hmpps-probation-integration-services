package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.integration.delius.entity.Dataset

object ReferenceDataGenerator {
    val DATASET_TYPE_GENDER = Dataset(IdGenerator.getAndIncrement(), "GENDER")
    val DATASET_TYPE_OTHER = Dataset(IdGenerator.getAndIncrement(), "OTHER")
    val DATASET_TYPE_ETHNICITY = Dataset(IdGenerator.getAndIncrement(), "ETHNICITY")
    val DATASET_TYPE_ADDRESS_STATUS = Dataset(IdGenerator.getAndIncrement(), "ADDRESS STATUS")
    val RD_MALE = generateReferenceData("M", description = "MALE", dataset = DATASET_TYPE_GENDER)
    val RD_FEMALE = generateReferenceData("F", description = "FEMALE", dataset = DATASET_TYPE_GENDER)
    val RD_RELIGION = generateReferenceData("REL", description = "RELIGION")
    val RD_NATIONALITY = generateReferenceData("NAT", description = "NATIONALITY")
    val RD_ETHNICITY = generateReferenceData("ETH", description = "ETHNICITY", dataset = DATASET_TYPE_ETHNICITY)
    val AI_PREVIOUS_CRN = generateReferenceData("MFCRN", description = "PREVIOUS CRN")
    val RD_DISABILITY_TYPE = generateReferenceData("DIST", description = "DISABILITY TYPE")
    val RD_DISABILITY_CONDITION = generateReferenceData("DISC", description = "DISABILITY CONDITION")
    val RD_ADDRESS_STATUS =
        generateReferenceData("ADST", description = "ADDRESS STATUS", dataset = DATASET_TYPE_ADDRESS_STATUS)
}
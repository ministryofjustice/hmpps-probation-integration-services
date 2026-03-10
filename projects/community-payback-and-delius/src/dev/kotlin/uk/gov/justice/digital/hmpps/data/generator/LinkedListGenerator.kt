package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.unpaidwork.LinkedList
import uk.gov.justice.digital.hmpps.entity.unpaidwork.LinkedListId

object LinkedListGenerator {
    val ELEARNING_ETE_LINKED_LIST = LinkedList(
        id = LinkedListId(
            data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE.id,
            data2 = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE.id,
        ),
        data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE,
        data2 = ReferenceDataGenerator.ELEARNING_PROJECT_TYPE,
    )

    val TRAINING_ETE_LINKED_LIST = LinkedList(
        id = LinkedListId(
            data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE.id,
            data2 = ReferenceDataGenerator.TRAINING_PROJECT_TYPE.id,
        ),
        data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE,
        data2 = ReferenceDataGenerator.TRAINING_PROJECT_TYPE,
    )
}
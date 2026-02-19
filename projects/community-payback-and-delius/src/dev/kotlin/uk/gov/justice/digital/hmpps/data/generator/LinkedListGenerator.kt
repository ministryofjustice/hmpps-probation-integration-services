package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.unpaidwork.LinkedList
import uk.gov.justice.digital.hmpps.entity.unpaidwork.LinkedListId

object LinkedListGenerator {
    val GROUP_PLACEMENT_ETE_LINKED_LIST = LinkedList(
        id = LinkedListId(
            data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE.id,
            data2 = ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE.id,
        ),
        data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE,
        data2 = ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE,
    )

    val INDIVIDUAL_PLACEMENT_ETE_LINKED_LIST = LinkedList(
        id = LinkedListId(
            data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE.id,
            data2 = ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE.id,
        ),
        data1 = ReferenceDataGenerator.ETE_PROJECT_TYPE,
        data2 = ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE,
    )
}
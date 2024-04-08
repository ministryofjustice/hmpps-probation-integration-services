package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import java.io.Serializable

@Entity
@Table(name = "r_linked_list")
@Immutable
class ReferenceDataLinkedList(
    @Id
    val id: ReferenceDataLinkedListId
)

@Embeddable
class ReferenceDataLinkedListId(
    @Column(name = "STANDARD_REFERENCE_DATA1")
    val data1: String,

    @Column(name = "STANDARD_REFERENCE_DATA2")
    val data2: String
) : Serializable
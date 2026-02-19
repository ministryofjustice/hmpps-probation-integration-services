package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.io.Serializable

@Embeddable
data class LinkedListId(
    @Column(name = "standard_reference_data1")
    val data1: Long = 0,
    @Column(name = "standard_reference_data2")
    val data2: Long = 0,
) : Serializable

@Entity
@Table(name = "r_linked_list")
class LinkedList(
    @EmbeddedId
    val id: LinkedListId,

    @OneToOne
    @JoinColumn(name = "standard_reference_data1", insertable = false, updatable = false)
    val data1: ReferenceData,

    @OneToOne
    @JoinColumn(name = "standard_reference_data2", insertable = false, updatable = false)
    val data2: ReferenceData,
)

interface LinkedListRepository : JpaRepository<LinkedList, LinkedListId> {
    fun findLinkedListsByData1_Code(data1Code: String): MutableList<LinkedList>
}
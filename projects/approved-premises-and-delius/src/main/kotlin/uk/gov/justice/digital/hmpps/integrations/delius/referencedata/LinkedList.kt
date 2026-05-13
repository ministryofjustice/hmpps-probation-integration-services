package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.Immutable
import java.io.Serializable

@Immutable
@Entity
@Table(name = "r_linked_list")
class ReferenceDataLink(
    @EmbeddedId
    val id: ReferenceDataLinkId,

    @ManyToOne
    @JoinColumn(name = "standard_reference_data1", insertable = false, updatable = false)
    val data1: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "standard_reference_data2", insertable = false, updatable = false)
    val data2: ReferenceData
)

@Embeddable
class ReferenceDataLinkId(
    @Column(name = "standard_reference_data1") val data1Id: Long,
    @Column(name = "standard_reference_data2") val data2Id: Long
) : Serializable

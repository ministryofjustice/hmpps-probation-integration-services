package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,
    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,
    @Column(name = "code_description", length = 500, nullable = false)
    val description: String,
    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val set: ReferenceDataSet,
    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class ReferenceDataSet(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,
    @Column(name = "code_set_name")
    val name: String,
)

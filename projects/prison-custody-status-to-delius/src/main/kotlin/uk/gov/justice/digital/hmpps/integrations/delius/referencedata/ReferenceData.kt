package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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

    @Column(nullable = false)
    @Type(type = "yes_no")
    val selectable: Boolean,

    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val set: ReferenceDataSet
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

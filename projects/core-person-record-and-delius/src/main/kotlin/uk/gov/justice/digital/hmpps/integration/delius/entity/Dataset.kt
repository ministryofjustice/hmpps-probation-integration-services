package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_reference_data_master")
class Dataset(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Column(name = "code_set_name")
    val code: String
) {
    companion object {
        const val ADDRESS_TYPE = "ADDRESS TYPE"
        const val ADDRESS_STATUS = "ADDRESS STATUS"
    }
}

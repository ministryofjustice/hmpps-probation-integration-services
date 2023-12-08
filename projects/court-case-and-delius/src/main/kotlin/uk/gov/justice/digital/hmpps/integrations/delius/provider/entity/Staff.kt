package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    @Column
    val forename: String,
    @Column
    val forename2: String?,
    @Column
    val surname: String,
    @Id
    @Column(name = "staff_id")
    val id: Long,
) {
    fun isUnallocated(): Boolean {
        return code.endsWith("U")
    }
}

@Immutable
@Table(name = "team")
@Entity
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,
    @Column(columnDefinition = "char(6)")
    val code: String,
    @Column
    val description: String,
    @Column
    val telephone: String? = null,
    @Column
    val emailAddress: String? = null,
    @ManyToOne
    @JoinColumn(name = "local_delivery_unit_id")
    val ldu: LocalDeliveryUnit,
    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,
)

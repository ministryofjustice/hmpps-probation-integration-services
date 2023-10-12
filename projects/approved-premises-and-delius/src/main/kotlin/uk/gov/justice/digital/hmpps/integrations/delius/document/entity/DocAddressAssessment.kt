package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class AddressAssessment(
    @Id
    @Column(name = "address_assessment_id")
    var id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_address_id", insertable = false, updatable = false)
    val personAddress: DocPersonAddress?
)

@Entity
@Table(name = "offender_address")
@Immutable
class DocPersonAddress(
    @Id
    @Column(name = "offender_address_id")
    var id: Long,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?
)

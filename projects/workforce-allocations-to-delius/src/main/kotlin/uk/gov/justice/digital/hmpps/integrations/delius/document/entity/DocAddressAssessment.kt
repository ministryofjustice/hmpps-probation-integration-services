package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Immutable
class AddressAssessment(
    @Id @Column(name = "address_assessment_id")
    var id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_address_id", insertable = false, updatable = false)
    val personAddress: PersonAddress?,
)

@Entity
@Table(name = "offender_address")
@Immutable
class PersonAddress(
    @Id @Column(name = "offender_address_id")
    var id: Long,

    @Column(name = "building_name")
    val buildingName: String?,

    @Column(name = "address_number")
    val addressNumber: String?,

    @Column(name = "street_name")
    val streetName: String?,
)

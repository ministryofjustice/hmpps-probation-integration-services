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
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "local_delivery_unit_id")
    val ldu: LocalDeliveryUnit?,

    @Id
    @Column(name = "team_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "local_delivery_unit")
class LocalDeliveryUnit(

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "local_delivery_unit_id")
    val id: Long
)

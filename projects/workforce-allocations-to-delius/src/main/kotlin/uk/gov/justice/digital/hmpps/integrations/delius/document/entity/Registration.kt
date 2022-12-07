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
class Registration(
    @Id @Column(name = "registration_id")
    var id: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id", updatable = false)
    val type: RegistrationType,
)

@Entity
@Table(name = "r_register_type")
@Immutable
class RegistrationType(
    @Id @Column(name = "REGISTER_TYPE_ID")
    var id: Long,

    @Column
    val description: String,
)

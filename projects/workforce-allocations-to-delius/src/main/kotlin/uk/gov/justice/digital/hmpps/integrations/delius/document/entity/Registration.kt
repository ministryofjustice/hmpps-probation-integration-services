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

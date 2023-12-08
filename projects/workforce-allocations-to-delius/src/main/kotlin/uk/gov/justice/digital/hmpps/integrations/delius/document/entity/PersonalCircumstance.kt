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
class PersonalCircumstance(
    @Id
    @Column(name = "personal_circumstance_id")
    var id: Long,
    @ManyToOne
    @JoinColumn(name = "circumstance_type_id", updatable = false)
    val type: PersonalCircumstanceType,
    @ManyToOne
    @JoinColumn(name = "circumstance_sub_type_id", updatable = false)
    val subType: PersonalCircumstanceSubType?,
)

@Entity
@Table(name = "r_circumstance_type")
@Immutable
class PersonalCircumstanceType(
    @Id
    @Column(name = "circumstance_type_id")
    var id: Long,
    @Column(name = "code_description")
    val description: String,
)

@Entity
@Table(name = "r_circumstance_sub_type")
@Immutable
class PersonalCircumstanceSubType(
    @Id
    @Column(name = "circumstance_sub_type_id")
    var id: Long,
    @Column(name = "code_description")
    val description: String,
)

package uk.gov.justice.digital.hmpps.integrations.common.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Table(name = "r_circumstance_type")
@Immutable
class PersonalCircumstanceType(
    @Id
    @Column(name = "circumstance_type_id")
    val id: Long,
    @Column(name = "code_value")
    val code: String,
    @Column(name = "code_description")
    val description: String,
)

@Entity
@Table(name = "r_circumstance_sub_type")
@Immutable
class PersonalCircumstanceSubType(
    @Id
    @Column(name = "circumstance_sub_type_id")
    val id: Long,
    @Column(name = "code_value")
    val code: String,
    @Column(name = "code_description")
    val description: String,
)

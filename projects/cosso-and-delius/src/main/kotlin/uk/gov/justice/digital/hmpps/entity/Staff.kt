package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "staff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,
    val forename: String,
    @Column(name = "forename2")
    val middleName: String?,
    val surname: String,
)



package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*

@Entity
@Table(name = "staff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,
    val forename: String,
    @Column(name = "forename2")
    val middleName: String? = null,
    val surname: String,
    @OneToOne(mappedBy = "staff")
    val user: User? = null,
)
package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    val forename: String,

    val surname: String,

    @OneToOne(mappedBy = "staff")
    val user: User?,
)
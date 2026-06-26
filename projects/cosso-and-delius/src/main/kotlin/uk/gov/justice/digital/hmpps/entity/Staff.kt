package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import uk.gov.justice.digital.hmpps.model.Name

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
    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,
    @OneToOne(mappedBy = "staff")
    val user: User?,
)

fun Staff.name() = Name(forename, middleName, surname)



package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.model.Name

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,

    val firstName: String,

    @Column(name = "forename2")
    val middleName: String?,

    val surname: String,

    @OneToOne(mappedBy = "staff")
    val user: User?,
)

fun Staff.name() = Name(firstName, middleName, surname)


interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByUserUsername(username: String): Staff?
}
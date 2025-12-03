package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.Name

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Column(columnDefinition = "char(3)")
    val code: String,

    val description: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "team")
class Team(
    @Column(columnDefinition = "char(6)")
    val code: String,
    val description: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val ldu: Ldu,

    @Id
    @Column(name = "team_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "district")
class Ldu(
    val code: String,
    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val pdu: Pdu,

    @Id
    @Column(name = "district_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "borough")
class Pdu(
    val code: String,
    val description: String,

    @Id
    @Column(name = "borough_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser?,

    @Id
    @Column(name = "staff_id")
    val id: Long,
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Id
    @Column(name = "user_id")
    val id: Long
)

fun Staff.name() = Name(forename, surname)
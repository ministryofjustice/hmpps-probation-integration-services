package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long = 0,

    @Column(name = "staff_id")
    val staffId: Long? = null,

    @Column(name = "distinguished_name")
    val distinguishedName: String?,

    @OneToMany(mappedBy = "user")
    val staffTeams: List<StaffTeam> = emptyList(),
)



package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
class District(
    @Id
    @Column(name = "district_id")
    val id: Long,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Column
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @OneToMany(mappedBy = "district")
    val teams: List<Team> = listOf()
)

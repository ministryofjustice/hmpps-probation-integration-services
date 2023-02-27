package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.springframework.data.jpa.repository.JpaRepository

interface ManagementTierEventRepository : JpaRepository<ManagementTierEvent, Long>
class ManagementTierEvent(

)
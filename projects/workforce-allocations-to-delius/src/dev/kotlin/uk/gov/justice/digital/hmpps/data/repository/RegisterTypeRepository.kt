package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.registration.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementMainCategory

interface RegisterTypeRepository : JpaRepository<RegisterType, Long>

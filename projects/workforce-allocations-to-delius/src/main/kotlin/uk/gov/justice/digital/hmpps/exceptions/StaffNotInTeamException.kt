package uk.gov.justice.digital.hmpps.exceptions

class StaffNotInTeamException(staffCode: String, teamCode: String) :
    RuntimeException("Staff $staffCode not in Team $teamCode")

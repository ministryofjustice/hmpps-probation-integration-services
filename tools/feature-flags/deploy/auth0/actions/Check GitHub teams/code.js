const ALLOWED_TEAMS = [
  "ministryofjustice:probation-integration",
  "ministryofjustice:ndst"
]

/**
 * Handler that will be called during the execution of a PostLogin flow.
 *
 * @param {Event} event - Details about the user and the context in which they are logging in.
 * @param {PostLoginAPI} api - Interface whose methods can be used to change the behavior of the login.
 */
exports.onExecutePostLogin = async (event, api) => {
  const _ = require('lodash');
  const axios = require("axios");
  const ManagementClient = require('auth0').ManagementClient;

  // Apply to 'github' connections only
  if (event.connection.strategy !== 'github') {
    api.access.deny('User must be authenticated via GitHub');
    return;
  }

  // Get user
  try {
    var authUser = await new ManagementClient({
      domain: event.secrets.domain,
      clientId: event.secrets.clientId,
      clientSecret: event.secrets.clientSecret,
    }).getUser({ id : event.user.user_id })
  } catch (e) {
    console.log(e);
    api.access.deny('Failure to get Auth0 user data');
    return;
  }

  // Get Github teams
  const githubIdentity = _.find(authUser.identities, { connection: 'github' });
  const githubResponse = await axios.get('https://api.github.com/user/teams', {
    headers: { 'Authorization': `token ${githubIdentity.access_token}` }
  });
  if (githubResponse.status !== 200) {
    console.log(githubResponse);
    api.access.deny(`Failure to get GitHub teams: ${githubResponse.status}`);
  }
  const teams = githubResponse.data;

  // Check if user is in one of the allowed teams
  for (const team of teams) {
    if (ALLOWED_TEAMS.includes(`${team.organization.login}:${team.slug}`)) {
      console.log("Success!");
      return;
    }
  }
  api.access.deny('User must be a member of one of the following teams: ' + ALLOWED_TEAMS.join(", "));
};

# 0005 - Manage Secrets Using GitHub Environments

2022-10-17

## Status

Accepted

## Context

Our projects are dependent on a number of variable values that are read from
the deployment environment. Some of these values are tokens that give access
to protected resources and should not be made available to unauthorised people
and should not be shared in an insecure manner. Each deployment environment
has different values for these secrets and the team must manage setting the
correct values in the correct variables in each environment for each service.

As we are using [GitHub Actions](./0001-use-github-as-project-home.md) for our
deployment workflow we are able to define named environments in the GitHub
project. It is possible to define environment variables for each environment
and set the values of these variables as part of the GitHub project UI and
also via the GitHub API. A GitHub Action can be configured the use a specific
environment and is then able to access the configured secrets.

Once set the secret values are only readable via GitHub Actions and are not
available in the GitHub UI. This should ensure that secrets are not shared
beyond the steps necessary to initially set them.

## Decision

- Define GitHub environments for each HMPPS deployment environment (e.g. test,
  preprod, prod)
- Define any project secrets as 'Environment secrets' in the specific GitHub
  Environment
- Manage setting secret values either via the GitHub UI or if possible using
  integration with the GitHub API (e.g. via Terraform) to ensure minimal
  exposure of the secret values

## Consequences

The GitHub Environment UI gives us a useful tool for managing secrets in our
environments, viewing the variable names we are using and indicating when
these were last updated.

Once set, a secret is only available to GitHub Actions workflows and therefore
is not generally available to team members.

Any GitHub Actions workflows that originate from non-team members need to be
subject to approval before running to ensure that no unauthorised person is
able to run a workflow that exposes the secret values. This is a configuration
value on the environment.

As we run multiple services from the GitHub repository it is necessary to
prefix the secrets with the name of the project in each environment to
identify which service the secret is relevant to. Using a convention for this
means the overhead is not too great, however, it does mean the secrets UI is
slightly less usable overall.

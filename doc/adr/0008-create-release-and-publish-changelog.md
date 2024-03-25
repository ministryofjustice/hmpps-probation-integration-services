# 0008 - Create Releases and Publish a Change Log Using Git and Jira

## Status

Accepted

## Context

Publishing a change log increases the visibility of the changes we make to our
services. The team is already providing information on the detail of changes
in both Git and Jira. Using a combination of the existing commit messages in
the repository and linking to the relevant Jira tickets we have the
information available to build a reasonable change log for our services.

Publishing the change log to our Slack channels would increase visibility of
changes around the organisation.

A GitHub repository has the facility to create versioned releases, which are
included in a release list in the UI displaying release notes. This also
creates a permanent link to a zipped tarball of the project. Additionally,
creating the release will generate a release notification message which can be
subscribed by clients such as Slack channels.

Creating the release and change log should not create extra admin overhead for
the team beyond simple actions when undertaking normal activities. Using
available GitHub Actions to automate the steps to create and publish a release
and generate a change log means the overhead can be minimised.

## Decision

- Use the [Release Changelog Builder Action](<https://github.com/mikepenz/release-changelog-builder-action>) to create a release changelog using pull request labels
- Use the [GitHub Release Action](<https://github.com/softprops/action-gh-release>) to create a draft release for publication
- Subscribe to release notifications via MoJ Slack channels

## Consequences

Publishing a change log should help team around the organisation better
understand the work we are doing and give information about the progress of
shared project work.

Using Git commit messages to build a release change log means that the quality
of the change log depends on the quality of the commit messages.

Developers may also include any relevant Jira references in their commit
messages to include the link to the business context of a change. If commits
lack the Jira reference it may not be clear why a particular change was made.

Not all commits are suitable for inclusion in the change log and it is
expected that we may edit the full list to include only changes with
descriptive messages and Jira references. The creation of a draft change log
supports manual editing of the commit list before release publication.

The team must label their pull requests with the correct labels if the commit
messages are to be automatically classified in the change log. Commits that
are not labelled will be put into a general unclassified change log.

There is a small amount of manual overhead to curating the change log for each
new release.

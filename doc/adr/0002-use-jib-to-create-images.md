# 0002 - Use Jib to Create Application Images

2022-08-09

## Status

Accepted

## Context

Each sub-project of Probation Integration Services is packaged as a Docker
image for deployment in a managed container service. We would like to reduce
both the size of these images and the overhead of managing any dependencies in
the deployment container. We would also like to have a consistent container
specification across all of our sub-projects so this is something we don't
have to consider for each new activity. As all of the sub-projects within this
repository are JVM-based Kotlin applications it is possible to choose a tool
that is focused solely on building JVM container images in a consistent
manner without the need for configuration.

Google's [Jib](https://github.com/GoogleContainerTools/jib) tool provides a
way of building images that is integrated into the Gradle build process and
produces small and consistent images for deployment.

## Decision

- Use [Jib](https://github.com/GoogleContainerTools/jib) to build our Docker
  images
- Integrate the image build into our Gradle build process using a custom
  plugin that could be shared more widely
- Base the image build on the `eclipse-temurin:17-jre-alpine` image

## Consequences

Using a standard tool for building images means we do not have to think about
this step for any new sub-project. We are able to build consistent images to
deploy each of our sub-projects without needing any per-project configuration.

Wrapping this process in a Gradle plugin means that we are able to change the
image build process for all projects in one place, meaning changing the small
number of things that can be configured, such as upgrading the base image, can
be done for all projects in one place. It would be possible to integrate
this plugin with the wider HMPPS technical tools if other teams are interested
in building container images in the same way.

The images produced by [Jib](https://github.com/GoogleContainerTools/jib) are
small, the layers are documented and inspecting using a tool like
[Dive](https://github.com/wagoodman/dive) is possible as with any other Docker
image. The use of an Alpine-based image reduces the system-level dependencies
in the deployed image lowering the ongoing dependency management overhead for
the team.

The Jib build process does not need a Docker daemon process to be running as
it builds and pushes directly from the Gradle JVM process. This is both easier
and faster to run in our CI pipeline. Additionally, as the Gradle build is run
directly as a CI stage rather than within a `docker build` process we are able
to take advantage of any build caching provided by the CI system.

As Jib is focused on JVM-based applications this tool is not appropriate when
building images for other runtimes that we may need to produce within HMPPS,
such as Typescript. If we have a requirement to do this we would need to
investigate the options for using Jib to package general filesystem content,
or investigate other tools that may be better suit other contexts.

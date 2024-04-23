# Ingress testing

A dummy API server and client used to investigate Kubernetes ingress issues on MOJ Cloud Platform.

More information: https://dsdmoj.atlassian.net/wiki/spaces/PINT/pages/4823351340/Handle+499+ingress+errors

## Usage

### API Server

To build and deploy the API server to the `hmpps-probation-integration` namespace:

```shell
cd api-server
docker login ghcr.io -u YOUR_GITHUB_USERNAME
docker build -t ghcr.io/ministryofjustice/hmpps-probation-integration-services/ingress-test:latest .
docker push ghcr.io/ministryofjustice/hmpps-probation-integration-services/ingress-test:latest

helm dependency update deploy
helm upgrade ingress-test deploy \
    --namespace hmpps-probation-integration \
    --install --reset-values --timeout 10m \
    --values <(curl "$(gh api '/repos/ministryofjustice/hmpps-ip-allowlists/contents/ip-allowlist-groups.yaml' | jq -r '.download_url')") \
    --values deploy/values.yaml \
    --wait
```

### API Client

Then, to build the API client and run a load test (e.g. with 5000 requests over 10 connections):

```shell
cd api-client
N=5000 C=10 ./gradlew bootRun
```

You should see some Spring startup logs followed by the test results, for example:

```
api-client ❯ N=5000 C=10 ./gradlew bootRun

> Task :bootRun

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2024-04-22T10:44:21.863+01:00  INFO 99173 --- [           main] uk.gov.justice.digital.hmpps.AppKt       : Starting AppKt using Java 21.0.1 with PID 99173 (/home/marcus/IdeaProjects/hmpps-probation-integration-services/tools/ingress-testing/api-client/build/classes/kotlin/main started by marcus in /home/marcus/IdeaProjects/hmpps-probation-integration-services/tools/ingress-testing/api-client)
2024-04-22T10:44:21.868+01:00  INFO 99173 --- [           main] uk.gov.justice.digital.hmpps.AppKt       : No active profile set, falling back to 1 default profile: "default"
2024-04-22T10:44:24.212+01:00  INFO 99173 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-04-22T10:44:24.232+01:00  INFO 99173 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-04-22T10:44:24.233+01:00  INFO 99173 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.19]
2024-04-22T10:44:24.294+01:00  INFO 99173 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-04-22T10:44:24.295+01:00  INFO 99173 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2318 ms
2024-04-22T10:44:25.534+01:00  INFO 99173 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoint(s) beneath base path ''
2024-04-22T10:44:25.618+01:00  INFO 99173 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-04-22T10:44:25.642+01:00  INFO 99173 --- [           main] uk.gov.justice.digital.hmpps.AppKt       : Started AppKt in 4.537 seconds (process running for 5.311)
2024-04-22T10:44:41.630+01:00  INFO 99173 --- [           main] u.g.j.digital.hmpps.test.TestHarness     : Results: {
  "200 OK" : 4963,
  "I/O error on GET request for \"https://ingress-test.probation-integration.service.justice.gov.uk/test/0\": /172.17.48.213:36854: GOAWAY received" : 10,
  "I/O error on GET request for \"https://ingress-test.probation-integration.service.justice.gov.uk/test/0\": /172.17.48.213:36946: GOAWAY received" : 8,
  "I/O error on GET request for \"https://ingress-test.probation-integration.service.justice.gov.uk/test/0\": /172.17.48.213:56914: GOAWAY received" : 10,
  "I/O error on GET request for \"https://ingress-test.probation-integration.service.justice.gov.uk/test/0\": /172.17.48.213:57048: GOAWAY received" : 9
}
2024-04-22T10:44:41.634+01:00  INFO 99173 --- [           main] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2024-04-22T10:44:41.637+01:00  INFO 99173 --- [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete

BUILD SUCCESSFUL in 28s
5 actionable tasks: 4 executed, 1 up-to-date
```

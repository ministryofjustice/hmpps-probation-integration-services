dependencyResolutionManagement {
    versionCatalogs {
        create("build") {
            plugin("jib", "com.google.cloud.tools.jib").version("3.4.5")
            library("jib", "com.google.cloud.tools:jib-gradle-plugin:3.4.5")

            plugin("sonarqube", "org.sonarqube").version("6.3.1.5724")
            library("sonarqube", "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:6.3.1.5724")
        }
    }
}
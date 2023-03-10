import org.slf4j.Logger
import org.slf4j.LoggerFactory

val imageName = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}"
val dockerDir = File(buildDir, "docker")
if (!dockerDir.exists()) dockerDir.mkdirs()
val buildFile = File("$buildDir/docker", "build")
val pushLatestFile = File("$buildDir/docker", "push-latest")
val pushVersionFile = File("$buildDir/docker", "push-version")

// build and tag image
val dockerBuild = tasks.create<Exec>("dockerBuild") {
    doFirst {
        commandLine =
            listOf("docker", "build", "-t", "$imageName:latest", "-t", "$imageName:${project.version}", "container")
    }
    workingDir = projectDir
    inputs.dir("container")
    inputs.dir("deploy")

    // commandLine cannot be empty at declaration but the actual command is run in doFirst to avoid cache invalidation for version changes
    commandLine = listOf("echo", "dockerBuild $imageName")

    outputs.file(buildFile)
    outputs.cacheIf { true }

    doLast {
        if (!buildFile.exists()) buildFile.createNewFile()
        buildFile.writeBytes("${project.version}".toByteArray())

        val dir = File("${project.rootDir}/changed")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, project.name)
        if (!file.exists()) file.createNewFile()
    }
}

fun createPushSubTask(tag: String, pushFile: File): Exec {
    val taskName = if (tag == "latest") "latest" else "version"
    return tasks.create<Exec>("dockerPush-$taskName") {
        doFirst {
            commandLine = listOf("docker", "push", "$imageName:$tag")
        }
        dependsOn(dockerBuild)
        workingDir = projectDir
        inputs.file(buildFile)
        commandLine = listOf("echo", "dockerPush $imageName")
        outputs.file(pushFile)
        outputs.cacheIf { true }
        doLast {
            if (!pushFile.exists()) pushFile.createNewFile()
            pushFile.writeBytes("${project.version}".toByteArray())
        }
    }
}

val pushLatest = createPushSubTask("latest", pushLatestFile)
val pushVersion = createPushSubTask("${project.version}", pushVersionFile)

val log: Logger = LoggerFactory.getLogger(this::class.java)
val dockerPush = tasks.create("dockerPush") {
    dependsOn(pushLatest, pushVersion)
    inputs.files(buildFile, pushVersionFile, pushLatestFile)
    actions.add { log.info("Built and Pushed ${project.name}:${project.version}") }
    outputs.file(File(dockerDir, "push"))
    outputs.cacheIf { true }
}

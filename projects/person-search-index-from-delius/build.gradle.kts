import org.slf4j.Logger
import org.slf4j.LoggerFactory

val imageName = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}"
val dockerDir = File(buildDir, "docker")
if (!dockerDir.exists()) dockerDir.mkdirs()
val buildFile = File("$buildDir/docker", "build")

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

val log: Logger = LoggerFactory.getLogger(this::class.java)
val dockerPush = tasks.create("dockerPush") {
    inputs.files(buildFile, File("$buildDir/docker", "push-latest"))
    actions.add { log.info("Built and Pushed ${project.name}:${project.version}") }
    outputs.file(File(dockerDir, "push"))
    outputs.cacheIf { true }
    doLast {
        // delete any previous push files - if this task has run for current version
        dockerDir.listFiles().filter {
            it.isFile && it.name.startsWith("push-") && it.name != "push-${project.version}"
        }.forEach(File::delete)
    }
}

listOf("latest", "${project.version}").forEach {
    val pushFile = File("$buildDir/docker", "push-$it")
    val subTask = tasks.create<Exec>("dockerPush-$it") {
        doFirst {
            commandLine = listOf("docker", "push", "$imageName:$it")
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
    dockerPush.dependsOn(subTask)
}

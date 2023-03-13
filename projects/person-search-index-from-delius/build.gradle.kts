val imageName = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}"
val dockerDir = File(buildDir, "docker")
if (!dockerDir.exists()) dockerDir.mkdirs()
val buildFile = File("$buildDir/docker", "build")
val pushFile = File("$buildDir/docker", "push")

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

val dockerPush = tasks.create("dockerPush") {
    doFirst {
        exec {
            workingDir = projectDir
            commandLine = listOf("docker", "push", "$imageName:latest")
        }
        exec {
            workingDir = projectDir
            commandLine = listOf("docker", "push", "$imageName:${project.version}")
        }
    }
    dependsOn(dockerBuild)
    inputs.file(buildFile)
    outputs.file(pushFile)
    outputs.cacheIf { true }
    doLast {
        if (!pushFile.exists()) pushFile.createNewFile()
        pushFile.writeBytes("${project.version}".toByteArray())
    }
}

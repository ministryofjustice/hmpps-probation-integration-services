val imageName = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}"
val buildFile = File("${buildDir}/docker", "build")
val tagFile = File("${buildDir}/docker", "tag")

val dockerBuild = tasks.create<Exec>("dockerBuild") {
    doFirst {
        if (!buildFile.exists()) buildFile.createNewFile()
    }
    workingDir = projectDir
    inputs.dir("container")
    inputs.dir("deploy")

    commandLine = listOf("docker", "build", "-t", "${imageName}:latest", "container")

    outputs.file(buildFile)
    outputs.cacheIf { true }

    doLast {
        buildFile.writeBytes("${project.version}".toByteArray())

        val dir = File("${project.rootDir}/changed")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, project.name)
        if (!file.exists()) file.createNewFile()
    }
}

val dockerTag = tasks.create<Exec>("dockerTag") {
    dependsOn(dockerBuild)
    doFirst {
        if (!tagFile.exists()) tagFile.createNewFile()
        commandLine = listOf("docker", "tag", imageName, "$imageName:${project.version}")
    }
    workingDir = projectDir
    commandLine = listOf("docker", "tag", imageName, "${imageName}:latest")
    inputs.file(buildFile)
    outputs.file(tagFile)
    outputs.cacheIf { true }
}

val dockerPush = tasks.create("dockerPush") {
    inputs.file(tagFile)
    outputs.cacheIf { true }
}

listOf("latest", "${project.version}").forEach {
    val pushFile = File("${buildDir}/docker", "push-$it")
    val subTask = tasks.create<Exec>("dockerPush-$it") {
        doFirst {
            if (!pushFile.exists()) pushFile.createNewFile()
        }
        dependsOn(dockerTag)
        workingDir = projectDir
        inputs.file(tagFile)
        commandLine = listOf("docker", "push", "$imageName:latest")
        outputs.file(pushFile)
        outputs.cacheIf { true }
    }
    dockerPush.dependsOn(subTask)
    dockerPush.outputs.files + pushFile
}
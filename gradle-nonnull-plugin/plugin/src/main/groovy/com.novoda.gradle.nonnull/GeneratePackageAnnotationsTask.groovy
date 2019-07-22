package com.novoda.gradle.nonnull

import groovy.transform.Memoized
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class GeneratePackageAnnotationsTask extends DefaultTask {

    @Input
    Set<String> packages;

    @OutputDirectory
    File outputDirectory

    def sourceSets

    @TaskAction
    void generatePackageAnnotations() {
        description = "Annotates the source packages with @ParametersAreNonnullByDefault"
        outputDirectory.deleteDir()

        Set<String> packages = getPackages()
        packages.each { packagePath ->
            def dir = new File(outputDirectory, packagePath)
            dir.mkdirs()

            def file = new File(dir, 'package-info.java')
            if (file.createNewFile()) {
                def packageName = packagePath.replaceAll('/', '.')
                file.write(createAnnotationDefinition(packageName))
            }
        }
    }

    @Memoized
    Set<String> getPackages() {
        Set<String> packages = []

        sourceSets.each { sourceSet ->
            sourceSet.java.srcDirs.findAll {
                it.exists()
            }.each { File srcDir ->
                project.file(srcDir).eachDirRecurse { dir ->
                    def packageInfo = new File(dir, 'package-info.java')
                    if (isEmptyDir(dir) && !packageInfo.isFile()) {
                        def packagePath = srcDir.toPath().relativize(dir.toPath()).toString()
                        packages << packagePath
                    }
                }
            }
        }
        packages
    }

    static isEmptyDir(File dir) {
        dir.list().length > 0
    }

    static def createAnnotationDefinition(packageName) {
        """ |/**
            | *
            | * Make all method parameters @NonNull by default.
            | *
            | * We assume that all method parameters are NON-NULL by default.
            | *
            | * e.g.
            | *
            | * void setValue(String value) {
            | *     this.value = value;
            | * }
            | *
            | * is equal to:
            | *
            | * void setValue(@NonNull String value) {
            | *     this.value = value;
            | * }
            | *
            | */
            |@ParametersAreNonnullByDefault
            |package ${packageName};
            |
            |import javax.annotation.ParametersAreNonnullByDefault;
            |""".stripMargin('|')
    }
}

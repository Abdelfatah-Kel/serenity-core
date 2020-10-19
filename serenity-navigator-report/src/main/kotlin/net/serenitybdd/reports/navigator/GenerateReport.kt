package net.serenitybdd.reports.navigator

import net.serenitybdd.core.environment.EnvironmentSpecificConfiguration
import net.serenitybdd.reports.io.testOutcomesIn
import net.thucydides.core.guice.Injectors
import net.thucydides.core.reports.ExtendedReport
import net.thucydides.core.util.EnvironmentVariables
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Consumer
import kotlin.streams.toList


/**
 * Generate an HTML summary report from a set of Serenity test outcomes in a given directory.
 */
class GenerateReport(

    val environmentVariables: EnvironmentVariables,
    private var sourceDirectory: Path = sourceDirectoryDefinedIn(environmentVariables),
    private var outputDirectory: Path = outputDirectoryDefinedIn(environmentVariables)) : ExtendedReport {


  constructor() : this(Injectors.getInjector().getProvider<EnvironmentVariables>(EnvironmentVariables::class.java).get())

  override fun getName(): String = "navigator"
  override fun getDescription(): String = "report as single page js application"


  override fun setSourceDirectory(sourceDirectory: Path) {
    this.sourceDirectory = sourceDirectory
  }

  override fun setOutputDirectory(outputDirectory: Path) {
    this.outputDirectory = outputDirectory
  }

  override fun generateReport(): Path {
    val userDir = EnvironmentSpecificConfiguration.from(environmentVariables)
        .getProperty("user.dir")
    val tmpDir = Files.createTempDirectory("serenity")
    val templateArchive: Path = findTemplateArchive(listOf(userDir, "$userDir/src/test/resources"))
    extract(templateArchive, tmpDir)
    fillTemplateAndWriteToReportDirectory(tmpDir)
    copyAllOtherNavigatorResources(tmpDir)
    if (!tmpDir.toFile().deleteRecursively()) {
      logging.error("could not delete $tmpDir")
    }
    return outputDirectory.resolve(Paths.get("navigator", "index.html"))
  }

  private fun copyAllOtherNavigatorResources(templateDir: Path) {
    Files.walk(templateDir, Int.MAX_VALUE)
        .filter { it != templateDir }
        .filter { it.fileName.toString() != "index.html" }
        .forEach(copyToOutputDirectory(templateDir))
  }

  private fun copyToOutputDirectory(templateDir: Path): Consumer<Path> {
    return Consumer {
      val navigatorRoot = outputDirectory.resolve("navigator")
      val outputPath = navigatorRoot.resolve(templateDir.relativize(it))

      if (it.toFile().isDirectory) {
        outputPath.toFile().mkdirs()
      }
      if (it.toFile().isFile) {
        IOUtils.copy(Files.newInputStream(it), Files.newOutputStream(outputPath))
      }
    }
  }

  private fun fillTemplateAndWriteToReportDirectory(templateDirectory: Path) {
    val indexHtml = templateDirectory.resolve("index.html")
    val lines = Files.lines(indexHtml).toList()

    if (lines.count() > 1) {
      throw RuntimeException("template index.html is supposed to be a minified into a single line")
    }

    val content = lines[0]
    val split = content.split("<script type=\"text/javascript\"></script>")

    val navigatorRoot = outputDirectory.resolve("navigator")
    navigatorRoot.toFile().mkdirs()
    val writer = FileWriter(navigatorRoot.resolve("index.html").toFile())
    writer.write(split[0])
    writer.write("<script type=\"text/javascript\">window.outcomes=[")

    // Fetch the test outcomes
    val testOutcomes = testOutcomesIn(sourceDirectory)
    testOutcomes.outcomes
        .map { it.toJson() }
        .joinToString(",")
        .let { writer.write(it) }

    writer.write("];</script>")
    writer.write(split[1])

    writer.flush()
    writer.close()
  }

  private fun extract(targGzArchive: Path, outputDir: Path) {
    try {
      val i = TarArchiveInputStream(
          GzipCompressorInputStream(
              BufferedInputStream(
                  FileInputStream(targGzArchive.toFile()))))

      var entry: TarArchiveEntry? = null

      while ({ entry = i.nextEntry as TarArchiveEntry?; entry }() != null) {
        if (!i.canReadEntryData(entry)) {
          logging.error("can't read " + entry!!.name)
          continue
        }

        val name: String = outputDir.toAbsolutePath().resolve(entry!!.name).toString()
        val f = File(name)
        if (entry!!.isDirectory) {
          if (!f.isDirectory && !f.mkdirs()) {
            throw IOException("failed to create directory $f")
          }
        } else {
          val parent = f.parentFile
          if (!parent.isDirectory && !parent.mkdirs()) {
            throw IOException("failed to create directory $parent")
          }
          Files.newOutputStream(f.toPath()).use { o -> IOUtils.copy(i, o) }
        }

      }

    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  private fun findTemplateArchive(searchDirs: List<String>): Path {
    val templateArchive: Path

    try {
      val searchFiles = searchDirs
          .map { Files.list(Paths.get(it)).toList() }
          .flatten()

      templateArchive = searchFiles
          .filter { it.fileName.toString().startsWith("serenity-report-navigator_v") }
          .first { it.fileName.toString().endsWith(".tar.gz") }

    } catch (e: NoSuchElementException) {
      throw FileNotFoundException("no 'serenity-report-navigator_vX.X.X.tar.gz' found in " + searchDirs.joinToString(", "))
    }
    return templateArchive
  }


  companion object {
    val logging: Log = LogFactory.getLog(GenerateReport::class.java)
  }

}

fun outputDirectoryDefinedIn(environmentVariables: EnvironmentVariables): Path = ReportNavigator.outputDirectory().configuredIn(environmentVariables)

fun sourceDirectoryDefinedIn(environmentVariables: EnvironmentVariables): Path = ReportNavigator.outputDirectory().configuredIn(environmentVariables)
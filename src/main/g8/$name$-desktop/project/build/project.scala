import sbt._

import scala.io.Source
import java.net.URL
import java.util.regex.Pattern
import java.io.{ FileNotFoundException, FileOutputStream }

class MyProject(info: ProjectInfo) extends DefaultProject(info) {
  // Force gdx app into a separate thread.
  override def fork = forkRun

  lazy val baseRepo = "http://libgdx.l33tlabs.org/"
  lazy val gdxName = "libgdx-nightly-%s" format(dateString(today))

  lazy val updateGdx = task {
    dependencyPath / gdxName exists match {
      case true => 
        log.info("Already have %s" format(gdxName))
        None
      case false =>
        // Clean up any existing libs.
        log.info("Cleaning older versions of nightly")
        val previousVersions = dependencyPath * "%s*".format("libgdx-nightly") 
        FileUtilities.clean(previousVersions.get, log)

        // Fetch the file.
        log.info("Pulling %s" format(gdxName))
        log.warn("This may take a few minutes...")
        val zip = "%s.zip" format(gdxName) 
        val dest = dependencyPath / gdxName
        
        // Unzip the file.
        val zipFile = new java.io.File(zip)
        val url = new URL("%s/%s" format(baseRepo, zip))
        FileUtilities.download(url, zipFile, log) 

        // Extract only needed jars.
        val filter = new ExactFilter("gdx.jar") |
                     new ExactFilter("gdx-natives.jar") |
                     new ExactFilter("gdx-backend-lwjgl.jar") |
                     new ExactFilter("gdx-backend-lwjgl-natives.jar")
        
        FileUtilities.unzip(zipFile, dest, filter, log)

        // Destroy the zip.
        zipFile.delete
        log.info("Complete")
        None
    } 
  } describedAs "Pulls libgdx desktop dependencies from nightly build."

  override def updateAction = 
    super.updateAction dependsOn updateGdx

  def today = new java.util.Date()
  def dateString(when: java.util.Date) = {
    val sdf = new java.text.SimpleDateFormat("yyyyMMdd")
    sdf.format(when)
  }
}

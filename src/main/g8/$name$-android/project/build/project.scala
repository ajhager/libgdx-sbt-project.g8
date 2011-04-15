import sbt._
import Process._
import scala.io.Source
import java.net.URL
import java.util.regex.Pattern
import java.io.{ FileNotFoundException, FileOutputStream }

class MyAndroidProject(info: ProjectInfo) extends AndroidProject(info) with MarketPublish {
  override def androidPlatformName = "android-$android_api_level$"
  override val keyalias  = "change-me"

  def nativeLibsPath = "lib"
  override def packageTask(signPackage: Boolean) = execTask {<x>
    {apkbuilderPath.absolutePath}
    {packageApkPath.absolutePath}
    {if (signPackage) "" else "-u"}
    -z {resourcesApkPath.absolutePath}
    -f {classesDexPath.absolutePath}
    -nf {nativeLibsPath.absolutePath }
    {proguardInJars.get.map(" -rj " + _.absolutePath)}
  </x>} dependsOn(cleanApk)

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
                     new ExactFilter("gdx-backend-android.jar") |
                     new ExactFilter("armeabi") |
                     new ExactFilter("armeabi-v7a")
        
        FileUtilities.unzip(zipFile, dest, filter, log)

        // Destroy the zip.
        zipFile.delete
        log.info("Complete")
        None
    } 
  } describedAs "Pulls libgdx android dependencies from nightly build."

  override def updateAction = 
    super.updateAction dependsOn updateGdx

  def today = new java.util.Date()
  def dateString(when: java.util.Date) = {
    val sdf = new java.text.SimpleDateFormat("yyyyMMdd")
    sdf.format(when)
  }
}

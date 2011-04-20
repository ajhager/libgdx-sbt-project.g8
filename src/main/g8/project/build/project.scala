import sbt._
import Process._
import java.io._
import java.net.URL

class GameProject(info: ProjectInfo) extends ParentProject(info) {
  lazy val common = project("common", "Common", new DefaultProject(_))
  lazy val desktop = project("desktop", "Desktop", new Desktop(_), common)
  lazy val android = project("android", "Android", new Android(_), common)

  class Desktop(info: ProjectInfo) extends DefaultProject(info) {
    override def fork = forkRun
    override def mainScalaSourcePath = "src" / "$desktop_backend$"
  }
  
  class Android(info: ProjectInfo) extends AndroidProject(info) {
    override def androidPlatformName = "android-$android_api_level$"
    override def mainAssetsPath = common.mainSourcePath / "resources"

    override def packageTask(signPackage: Boolean) = execTask {<x>
      {apkbuilderPath.absolutePath}
      {packageApkPath.absolutePath}
      {if (signPackage) "" else "-u"}
      -z {resourcesApkPath.absolutePath}
      -f {classesDexPath.absolutePath}
      -nf {dependencyPath.absolutePath }
      {proguardInJars.get.map(" -rj " + _.absolutePath)}
    </x>} dependsOn(cleanApk)
  }

  override def updateAction = super.updateAction dependsOn updateGdx
  lazy val updateGdx = task {
    val repo = "$libgdx_version$"
    var baseRepo = ""
    var gdxName = "" 
    // Declare the url and file name.
    if (repo == "nightly") {
      baseRepo = "http://libgdx.l33tlabs.org/"
      gdxName = "libgdx-nightly-%s" format {
        (new java.text.SimpleDateFormat("yyyyMMdd")).format(new java.util.Date())
      }
      None
    } else {
      baseRepo = "http://libgdx.googlecode.com/files/"
      gdxName = "libgdx-%s" format(repo)
      None
    }

    // Fetch the file.
    log.info("Pulling %s" format(gdxName))
    log.warn("This may take a few minutes...")
    val zip = "%s.zip" format(gdxName) 
    
    // Unzip the file.
    val zipFile = new java.io.File(zip)
    val url = new URL("%s/%s" format(baseRepo, zip))
    FileUtilities.download(url, zipFile, log) 
    
    // Extract jars into their respective lib folders.
    val commonDest = common.dependencyPath
    val commonFilter = new ExactFilter("gdx.jar")
    FileUtilities.unzip(zipFile, commonDest, commonFilter, log)
    
    val desktopDest = desktop.dependencyPath
    val desktopFilter = new ExactFilter("gdx-natives.jar") |
    new ExactFilter("gdx-backend-$desktop_backend$.jar") |
    new ExactFilter("gdx-backend-$desktop_backend$-natives.jar")
    FileUtilities.unzip(zipFile, desktopDest, desktopFilter, log)
    
    val androidDest = android.dependencyPath
    val androidFilter = new ExactFilter("gdx-backend-android.jar") |
    new ExactFilter("armeabi/libgdx.so") |
    new ExactFilter("armeabi/libandroidgl20.so") |
    new ExactFilter("armeabi-v7a/libgdx.so") |
    new ExactFilter("armeabi-v7a/libandroidgl20.so")
    FileUtilities.unzip(zipFile, androidDest, androidFilter, log)
    
    // Destroy the file.
    zipFile.delete
    log.info("Complete")
    None
  } describedAs "Pull and distribute the libgdx nightly libraries."
}

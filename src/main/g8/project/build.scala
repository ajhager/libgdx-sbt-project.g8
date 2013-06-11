import sbt._

import Keys._
import org.scalasbt.androidplugin._
import org.scalasbt.androidplugin.AndroidKeys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Settings {
  lazy val scalameter = new TestFramework("org.scalameter.ScalaMeterFramework")

  lazy val common = Defaults.defaultSettings ++ Seq (
    version := "0.1",
    scalaVersion := "$scala_version$",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    libraryDependencies += "com.github.axel22" %% "scalameter" % "0.3" % "test",
    testFrameworks += scalameter,
    testOptions += Tests.Argument(scalameter, "-preJDK7")
   )

  lazy val desktop = Settings.common ++ assemblySettings ++ Seq (
    unmanagedResourceDirectories in Compile += file("common/assets"),
    fork in Compile := true
  )

  lazy val android = Settings.common ++
    AndroidProject.androidSettings ++
    AndroidMarketPublish.settings ++ Seq (
      name := "$name$",
      platformName in Android := "android-$api_level$",
      keyalias in Android := "change-me",
      mainAssetsPath in Android := file("common/assets"),
      unmanagedBase <<= baseDirectory( _ /"src/main/libs" ),
      proguardOption in Android <<= (baseDirectory) {
        (b) => scala.io.Source.fromFile(b / "src/main/proguard.cfg").getLines.map(_.takeWhile(_!='#')).filter(_!="").mkString("\n")
      }
    )

  val updateLibgdx = TaskKey[Unit]("update-gdx", "Updates libgdx")

  val updateLibgdxTask = updateLibgdx <<= streams map { (s: TaskStreams) =>
    import Process._
    import java.io._
    import java.net.URL
    import java.util.regex.Pattern

    // Setup
    val version = "$libgdx_version$"

    // Declare names
    val baseUrl = if (version == "nightly") "http://libgdx.badlogicgames.com/nightlies" else "http://libgdx.googlecode.com/files"
    val gdxName = if (version == "nightly") "libgdx-nightly-latest" else "libgdx-"+version

    // Fetch the file.
    s.log.info("Pulling %s" format(gdxName))
    s.log.warn("This may take a few minutes...")
    val zipName = "%s.zip" format(gdxName)
    val zipFile = new java.io.File(zipName)
    val url = new URL("%s/%s" format(baseUrl, zipName))
    IO.download(url, zipFile)

    // Extract jars into their respective lib folders.
    s.log.info("Extracting common libs")
    val commonDest = file("common/lib")
    val commonFilter = new ExactFilter("gdx.jar")
    IO.unzip(zipFile, commonDest, commonFilter)

    s.log.info("Extracting desktop libs")
    val desktopDest = file("desktop/lib")
    val desktopFilter = new ExactFilter("gdx-natives.jar") |
    new ExactFilter("gdx-backend-lwjgl.jar") |
    new ExactFilter("gdx-backend-lwjgl-natives.jar")
    IO.unzip(zipFile, desktopDest, desktopFilter)

    s.log.info("Extracting ios libs")
    val iosDest = file("ios/libs")
    val iosFilter = GlobFilter("ios/*")
    IO.unzip(zipFile, iosDest, iosFilter)

    s.log.info("Extracting android libs")
    val androidDest = file("android/src/main/libs")
    val androidFilter = new ExactFilter("gdx-backend-android.jar") |
    new ExactFilter("armeabi/libgdx.so") |
    new ExactFilter("armeabi/libandroidgl20.so") |
    new ExactFilter("armeabi-v7a/libgdx.so") |
    new ExactFilter("armeabi-v7a/libandroidgl20.so")
    IO.unzip(zipFile, androidDest, androidFilter)

    // Destroy the file.
    zipFile.delete
    s.log.info("Update complete")
  }
}

object LibgdxBuild extends Build {
  val common = Project (
    "common",
    file("common"),
    settings = Settings.common
  )

  lazy val desktop = Project (
    "desktop",
    file("desktop"),
    settings = Settings.desktop
  ) dependsOn(common % "compile->compile;test->test") settings(
    mainClass in assembly := Some("$package$.Main"),
    AssemblyKeys.jarName in assembly := "$name;format="norm"$-0.1.jar"
  )

  lazy val android = Project (
    "android",
    file("android"),
    settings = Settings.android
  ) dependsOn(common % "compile->compile;test->test")

  lazy val all = Project (
    "all-platforms",
    file("."),
    settings = Settings.common :+ Settings.updateLibgdxTask
  ) aggregate(common, desktop, android)

  lazy val tests = Project (
    "android-tests",
    file("android-tests"),
    settings = Settings.android ++
               AndroidTest.androidSettings ++
               Seq ( name := "$name$ Tests" )
  ) dependsOn android
}

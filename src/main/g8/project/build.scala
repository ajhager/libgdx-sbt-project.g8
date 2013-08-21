import sbt._
import Keys._
import Defaults._

import sbtandroid.AndroidPlugin._

import sbtassembly.Plugin._
import AssemblyKeys._

object Settings {
  lazy val scalameter = new TestFramework("org.scalameter.ScalaMeterFramework")

  lazy val common = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    scalaVersion := "$scala_version$",
    javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),
    scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature"),
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
      "com.github.axel22" %% "scalameter" % "0.3" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test"
    ),
    parallelExecution in Test := false,
    testFrameworks in Test += scalameter,
    testOptions in Test ++= Seq(
      Tests.Argument(scalameter, "-preJDK7"),
      Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports")
    )
  )

  lazy val desktop = common ++ assemblySettings ++ Seq(
    name := "$name$",
    unmanagedResourceDirectories in Compile += file("common/assets"),
    fork in Compile := true
  )

  lazy val android = common ++ Seq(
    name := "$name$",
    versionCode := 0,
    keyalias := "change-me",
    platformName := "android-$api_level$",
    mainAssetsPath := file("common/assets"),
    unmanagedBase <<= baseDirectory(_/"lib"),
    unmanagedClasspath in Compile <+= (libraryJarPath) map (Attributed.blank(_)),
    proguardOptions <<= (baseDirectory) { (b) => Seq(
      scala.io.Source.fromFile(b/"src/main/proguard.cfg").getLines.map(_.takeWhile(_!='#')).filter(_!="").mkString("\n")
    )}
  )

  lazy val assemblyOverrides = Seq(
    mainClass in assembly := Some("$package$.Main"),
    AssemblyKeys.jarName in assembly := "$name;format="norm"$-0.1.jar"
  )

  lazy val androidTestOverrides = Seq(
    name := "$name$ Tests"
  )
}

object Tasks {
  lazy val updateLibgdxKey = TaskKey[Unit]("update-gdx", "Updates libgdx")

  lazy val updateLibgdx = updateLibgdxKey <<= streams map { (s: TaskStreams) =>
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
    val androidDest = file("android/lib")
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
  lazy val common = Project(
    "common",
    file("common"),
    settings = Settings.common)

  lazy val desktop = Project(
    "desktop",
    file("desktop"),
    settings = Settings.desktop)
    .dependsOn(common)
    .settings(Settings.assemblyOverrides: _*)

  lazy val android = AndroidProject(
    "android",
    file("android"),
    settings = Settings.android)
    .dependsOn(common)

  lazy val all = Project(
    "all-platforms",
    file("."),
    settings = Settings.common :+ Tasks.updateLibgdx
  ) aggregate(common, desktop, android)

  lazy val tests = AndroidTestProject(
    "android-tests",
    file("android-tests"),
    settings = Settings.android)
    .dependsOn(android % "provided")
    .settings(Settings.androidTestOverrides: _*)
}


import sbt._
import Keys._
import Defaults._

import sbtandroid.AndroidPlugin._
import sbtrobovm.RobovmPlugin._

object Settings {
  lazy val desktopJarName = SettingKey[String]("desktop-jar-name", "name of JAR file for desktop")

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
    ),
    unmanagedBase <<= baseDirectory(_/"libs"),
    proguardOptions <<= (baseDirectory) { (b) => Seq(
      scala.io.Source.fromFile(file("common/src/main/proguard.cfg")).getLines.map(_.takeWhile(_!='#')).filter(_!="").mkString("\n"), {
        val path = b/"src/main/proguard.cfg"
        if (path.exists()) {
          scala.io.Source.fromFile(b/"src/main/proguard.cfg").getLines.map(_.takeWhile(_!='#')).filter(_!="").mkString("\n")
        } else {
          ""
        }
      }
    )}
  )

  lazy val desktop = common ++ Seq(
    unmanagedResourceDirectories in Compile += file("common/assets"),
    libraryDependencies ++= Seq("net.sf.proguard" % "proguard-base" % "4.8"), // keep in sync with sbt-android version
    Tasks.assembly,
    desktopJarName := "$name;format="norm"$",
    fork in Compile := true
  )

  lazy val android = common ++ Seq(
    versionCode := 0,
    keyalias := "change-me",
    platformName := "android-$api_level$",
    mainAssetsPath in Compile := file("common/assets"),
    unmanagedJars in Compile <+= (libraryJarPath) (p => Attributed.blank(p)) map( x=> x)
  )

  lazy val ios = common ++ Seq(
    unmanagedResources in Compile <++= (baseDirectory) map { _ =>
      (file("common/assets") ** "*").get
    },
    forceLinkClasses := Seq("com.badlogic.gdx.scenes.scene2d.ui.*"),
    skipPngCrush := true,
    iosInfoPlist <<= (sourceDirectory in Compile){ sd => Some(sd / "Info.plist") },
    frameworks := Seq("UIKit", "OpenGLES", "QuartzCore", "CoreGraphics", "OpenAL", "AudioToolbox", "AVFoundation"),
    nativePath <<= (baseDirectory){ bd => Seq(bd / "lib") }
  )
}

object Tasks {
  import java.io.{File => JFile}
  import Settings.desktopJarName

  lazy val assemblyKey = TaskKey[Unit]("assembly", "Assembly desktop version using Proguard")

  lazy val assembly = assemblyKey <<= (compile in Compile,       // dependency to make sure compile finished
      target, desktopJarName, version,                           // data for output jar name
      proguardOptions,                                           // merged proguard.cfg from common and desktop
      javaOptions in Compile, managedClasspath in Compile,       // java options and classpath
      classDirectory in Compile, dependencyClasspath in Compile, // classes and jars to proguard
      streams) map { (c, target, name, ver, proguardOptions, options, cp, cd, dependencies, s) =>
    val exclusions = Seq("!META-INF/MANIFEST.MF", "!library.properties").mkString(",")
    val withoutProguard = dependencies.filterNot(cpe => cpe.data.absolutePath contains "proguard-base")
    val inJars = withoutProguard.map("\"" + _.data.absolutePath + "\"("+exclusions+")").mkString(JFile.pathSeparator)
    val outfile = "\"" + (target/"%s-%s.jar".format(name, ver)).absolutePath + "\""
    val classfiles = "\"" + cd.absolutePath + "\""
    val manifest = "\"" + file("desktop/src/main/manifest").absolutePath + "\""
    val proguard = options ++ Seq("-cp", Path.makeString(cp.files), "proguard.ProGuard") ++ proguardOptions ++ Seq(
      "-injars", classfiles,
      "-injars", inJars,
      "-injars", manifest,
      "-outjars", outfile)
   
    s.log.info("preparing proguarded assembly")
    s.log.debug("Proguard command:")
    s.log.debug("java "+proguard.mkString(" "))
    val exitCode = Process("java", proguard) ! s.log
    if (exitCode != 0) {
      sys.error("Proguard failed with exit code [%s]" format exitCode)
    } else {
      s.log.info("Output file: "+outfile)
    }
  }

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
    val commonDest = file("common/libs")
    val commonFilter = new ExactFilter("gdx.jar")
    IO.unzip(zipFile, commonDest, commonFilter)

    s.log.info("Extracting desktop libs")
    val desktopDest = file("desktop/libs")
    val desktopFilter = new ExactFilter("gdx-natives.jar") |
    new ExactFilter("gdx-backend-lwjgl.jar") |
    new ExactFilter("gdx-backend-lwjgl-natives.jar")
    IO.unzip(zipFile, desktopDest, desktopFilter)

    s.log.info("Extracting android libs")
    val androidDestJar = file("android/libs")
    val androidFilterJar = new ExactFilter("gdx-backend-android.jar")
    val androidDestSo = file("android/lib")
    val androidFilterSo = new ExactFilter("armeabi/libgdx.so") |
    new ExactFilter("armeabi/libandroidgl20.so") |
    new ExactFilter("armeabi-v7a/libgdx.so") |
    new ExactFilter("armeabi-v7a/libandroidgl20.so")
    IO.unzip(zipFile, androidDestJar, androidFilterJar)
    IO.unzip(zipFile, androidDestSo, androidFilterSo)

    s.log.info("Extracting ios libs")
    val iosDestJar = file("ios/libs")
    val iosFilterJar = new ExactFilter("gdx-backend-robovm.jar")
    val iosDestSo = file("ios/lib")
    val iosFilterSo = new ExactFilter("ios/libgdx.a") |
    new ExactFilter("ios/libObjectAL.a")
    IO.unzip(zipFile, iosDestJar, iosFilterJar)
    IO.unzip(zipFile, iosDestSo, iosFilterSo)
    IO.move(file("ios/lib/ios/libgdx.a"), file("ios/lib/libgdx.a"))
    IO.move(file("ios/lib/ios/libObjectAL.a"), file("ios/lib/libObjectAL.a"))
    IO.delete(file("ios/lib/ios"))

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

  lazy val android = AndroidProject(
    "android",
    file("android"),
    settings = Settings.android)
    .dependsOn(common)

  lazy val ios = RobovmProject(
    "ios",
    file("ios"),
    settings = Settings.ios)
    .dependsOn(common)

  lazy val all = Project(
    "all-platforms",
    file("."),
    settings = Settings.common :+ Tasks.updateLibgdx)
    .aggregate(common, desktop, android, ios)
}


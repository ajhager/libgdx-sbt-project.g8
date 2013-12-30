import sbt._
import Keys._
import Defaults._

import sbtandroid.AndroidPlugin._
import sbtrobovm.RobovmPlugin._

object Settings {
  lazy val desktopJarName = SettingKey[String]("desktop-jar-name", "name of JAR file for desktop")

  lazy val nativeExtractions = SettingKey[Seq[(String, NameFilter, File)]]("native-extractions", "(jar name partial, sbt.NameFilter of files to extract, destination directory)")

  lazy val common = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    scalaVersion := "$scala_version$",
    javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),
    scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature"),
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" % "gdx" % "$libgdx_version$"
    ),
    cancelable := true,
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
    fork in Compile := true,
    libraryDependencies ++= Seq(
      "net.sf.proguard" % "proguard-base" % "4.8" % "provided",
      "com.badlogicgames.gdx" % "gdx-backend-lwjgl" % "$libgdx_version$",
      "com.badlogicgames.gdx" % "gdx-platform" % "$libgdx_version$" classifier "natives-desktop"
    ),
    Tasks.assembly,
    desktopJarName := "$name;format="norm"$"
  )

  lazy val android = common ++ Tasks.natives ++ Seq(
    versionCode := 0,
    keyalias := "change-me",
    platformName := "android-$api_level$",
    mainAssetsPath in Compile := file("common/assets"),
    unmanagedJars in Compile <+= (libraryJarPath) (p => Attributed.blank(p)) map( x=> x),
    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" % "gdx-backend-android" % "$libgdx_version$",
      "com.badlogicgames.gdx" % "gdx-platform" % "$libgdx_version$" % "natives" classifier "natives-armeabi",
      "com.badlogicgames.gdx" % "gdx-platform" % "$libgdx_version$" % "natives" classifier "natives-armeabi-v7a"
    ),
    nativeExtractions <<= (baseDirectory) { base => Seq(
      ("natives-armeabi.jar", new ExactFilter("libgdx.so"), base / "lib" / "armeabi"),
      ("natives-armeabi-v7a.jar", new ExactFilter("libgdx.so"), base / "lib" / "armeabi-v7a")
    )}
  )

  lazy val ios = common ++ Tasks.natives ++ Seq(
    unmanagedResources in Compile <++= (baseDirectory) map { _ =>
      (file("common/assets") ** "*").get
    },
    forceLinkClasses := Seq("com.badlogic.gdx.scenes.scene2d.ui.*"),
    skipPngCrush := true,
    iosInfoPlist <<= (sourceDirectory in Compile){ sd => Some(sd / "Info.plist") },
    frameworks := Seq("UIKit", "OpenGLES", "QuartzCore", "CoreGraphics", "OpenAL", "AudioToolbox", "AVFoundation"),
    nativePath <<= (baseDirectory){ bd => Seq(bd / "lib") },
    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" % "gdx-backend-robovm" % "$libgdx_version$",
      "com.badlogicgames.gdx" % "gdx-platform" % "$libgdx_version$" % "natives" classifier "natives-ios"
    ),
    nativeExtractions <<= (baseDirectory) { base => Seq(
      ("natives-ios.jar", new ExactFilter("libgdx.a") | new ExactFilter("libObjectAL.a"), base / "lib")
    )}
  )
}

object Tasks {
  import java.io.{File => JFile}
  import Settings.desktopJarName
  import Settings.nativeExtractions

  lazy val extractNatives = TaskKey[Unit]("extract-natives", "Extracts native files")

  lazy val natives = Seq(
    ivyConfigurations += config("natives"),
    nativeExtractions := Seq.empty,
    extractNatives <<= (nativeExtractions, update) map { (ne, up) =>
      val jars = up.select(configurationFilter("natives"))
      ne foreach { case (jarName, fileFilter, outputPath) =>
        jars find(_.getName.contains(jarName)) map { jar =>
            IO.unzip(jar, outputPath, fileFilter)
        }
      }
    },
    compile in Compile <<= (compile in Compile) dependsOn (extractNatives)
  )

  lazy val assemblyKey = TaskKey[Unit]("assembly", "Assembly desktop using Proguard")

  lazy val assembly = assemblyKey <<= (fullClasspath in Runtime, // dependency to make sure compile finished
      target, desktopJarName, version, // data for output jar name
      proguardOptions, // merged proguard.cfg from common and desktop
      javaOptions in Compile, managedClasspath in Compile, // java options and classpath
      classDirectory in Compile, dependencyClasspath in Compile, update in Compile, // classes and jars to proguard
      streams) map { (c, target, name, ver, proguardOptions, options, cp, cd, dependencies, up, s) =>
    val provided = Set(up.select(configurationFilter("provided")):_*)
    val compile = Set(up.select(configurationFilter("compile")):_*)
    val runtime = Set(up.select(configurationFilter("runtime")):_*)
    val optional = Set(up.select(configurationFilter("optional")):_*)
    val onlyProvidedNames = provided -- compile -- runtime -- optional
    val (onlyProvided, withoutProvided) = dependencies.partition(cpe => onlyProvidedNames contains cpe.data)
    val exclusions = Seq("!META-INF/MANIFEST.MF", "!library.properties").mkString(",")
    val inJars = withoutProvided.map("\""+_.data.absolutePath+"\"("+exclusions+")").mkString(JFile.pathSeparator)
    val libraryJars = onlyProvided.map("\""+_.data.absolutePath+"\"").mkString(JFile.pathSeparator)
    val outfile = "\""+(target/"%s-%s.jar".format(name, ver)).absolutePath+"\""
    val classfiles = "\"" + cd.absolutePath + "\""
    val manifest = "\"" + file("desktop/src/main/manifest").absolutePath + "\""
    val proguard = options ++ Seq("-cp", Path.makeString(cp.files), "proguard.ProGuard") ++ proguardOptions ++ Seq(
      "-injars", classfiles,
      "-injars", inJars,
      "-injars", manifest,
      "-libraryjars", libraryJars,
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
    settings = Settings.common)
    .aggregate(common, desktop, android, ios)
}

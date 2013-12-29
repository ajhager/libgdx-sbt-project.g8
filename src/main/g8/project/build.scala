import sbt._
import Keys._
import Defaults._

import sbtandroid.AndroidPlugin._
import sbtrobovm.RobovmPlugin._

import sbtassembly.Plugin._
import AssemblyKeys._

object Settings {
  lazy val common = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    scalaVersion := "$scala_version$",
    javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),
    scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature"),
    unmanagedBase <<= baseDirectory(_/"libs"),
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" % "gdx" % "$libgdx_version$"
    ),
    cancelable := true
  )

  lazy val desktop = common ++ assemblySettings ++ Seq(
    unmanagedResourceDirectories in Compile += file("common/assets"),
    fork in Compile := true,
    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" % "gdx-backend-lwjgl" % "$libgdx_version$",
      "com.badlogicgames.gdx" % "gdx-platform" % "$libgdx_version$" classifier "natives-desktop"
    )
  )

  lazy val android = common ++ natives ++ Seq(
    versionCode := 0,
    keyalias := "change-me",
    platformName := "android-$api_level$",
    mainAssetsPath in Compile := file("common/assets"),
    unmanagedJars in Compile <+= (libraryJarPath) (p => Attributed.blank(p)) map( x=> x),
    proguardOptions <<= (baseDirectory) { (b) => Seq(
      scala.io.Source.fromFile(b/"src/main/proguard.cfg").getLines.map(_.takeWhile(_!='#')).filter(_!="").mkString("\n")
    )},
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

  lazy val ios = common ++ natives ++ Seq(
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

  lazy val assemblyOverrides = Seq(
    mainClass in assembly := Some("$package$.Main"),
    AssemblyKeys.jarName in assembly := "$name;format="norm"$-0.1.jar"
  )

  lazy val nativeExtractions = SettingKey[Seq[(String, NameFilter, File)]]("native-extractions", "(jar name partial, sbt.NameFilter of files to extract, destination directory)")
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

  lazy val ios = RobovmProject(
    "ios",
    file("ios"),
    settings = Settings.ios)
    .dependsOn(common)

  lazy val all = Project(
    "all-platforms",
    file("."),
    settings = Settings.common
  ) aggregate(common, desktop, android, ios)
}

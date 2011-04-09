import sbt._
import Process._

class MyAndroidProject(info: ProjectInfo) extends AndroidProject(info) {
  override def androidPlatformName = "android-$platform$"

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
}

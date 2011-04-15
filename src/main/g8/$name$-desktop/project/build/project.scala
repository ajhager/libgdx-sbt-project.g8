import sbt._

class MyProject(info: ProjectInfo) extends DefaultProject(info) {
  override def fork = forkRun
}

import sbt._

class TemplateProject(info: ProjectInfo)
    extends DefaultProject(info) with giter8.Template {
  override def disableCrossPaths = true
}

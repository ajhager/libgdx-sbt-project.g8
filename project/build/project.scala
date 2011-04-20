import sbt._

class TemplateProject(info: ProjectInfo) extends DefaultProject(info) with giter8.Template {
  override def disableCrossPaths = true

  def capitalize(s: String) = {
    s(0).toUpperCase + s.substring(1, s.length).toLowerCase
  }
  val props = myReadProps(defaultProperties)
  props.setProperty("desktop_backend_cap",
                    capitalize(props.getProperty("desktop_backend")))

  override lazy val writeTemplates = applyTemplates(
    templateSources,
    templateOutput,
    props
  ) describedAs "Apply default parameters to input templates and write to " + 
    templateOutput

  def myReadProps(f: Path) = {
    val p = new java.util.Properties
    FileUtilities.readStream(f.asFile, log) { stm =>
      p.load(stm)
      None
    }
    p
  }
}

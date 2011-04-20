import sbt._

class TemplateProject(info: ProjectInfo) extends DefaultProject(info) with giter8.Template {
  override def disableCrossPaths = true

  def capitalize(s: String) = {
    s(0).toUpperCase + s.substring(1, s.length).toLowerCase
  }

  val p = myReadProps(defaultProperties)
  val c = capitialize(p.getProperty("desktop_backend"))

  p.setProperty("desktop_backend_cap", c)

  override lazy val writeTemplates = applyTemplates(
    templateSources,
    templateOutput,
    p
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

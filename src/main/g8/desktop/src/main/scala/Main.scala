package $package$

object Main {
  def main(args: Array[String]): Unit = {
    "$desktop_backend$" match {
      case "jogl" => {
        import com.badlogic.gdx.backends.jogl.JoglApplication
        val App = JoglApplication
      }
      case _ => {
        import com.badlogic.gdx.backends.lwjgl.LwjglApplication
        val App = LwjglApplication
      }
    }

    new App(new MyGame(), "$name$", 480, 320, false);
  }
}

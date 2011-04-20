package $package$

import com.badlogic.gdx.ApplicationListener

object Main {
  def run(appName: String, game: ApplicationListener, name: String,
          width: Int, height: Int, useGL2: Boolean) {
      val app = Class.forName(appName)
      val con = app.getConstructor(classOf[ApplicationListener],
                                   classOf[String],
                                   classOf[Int],
                                   classOf[Int],
                                   classOf[Boolean])
      con.newInstance(game, name, width.asInstanceOf[AnyRef],
                      height.asInstanceOf[AnyRef],
                      useGL2.asInstanceOf[AnyRef])
  }

  def main(args: Array[String]): Unit = {
    var appName = "com.badlogic.gdx.backends"
    if ("$desktop_backend$" == "lwjgl")
      appName += ".lwjgl.LwjglApplication"
    else
      appName += ".jogl.JoglApplication"

    run(appName, new MyGame(), "Game4", 480, 320, false)
  }
}

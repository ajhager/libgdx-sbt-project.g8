package $package$

import com.badlogic.gdx.backends.lwjgl.LwjglApplication._

object Main {
  def main(args: Array[String]): Unit = {
    val cfg = new LwjglApplicationConfiguration()
    cfg.title = "$name$"
    cfg.height = 480
    cfg.width = 320
    cfg.useGL20 = true
    new LwjglApplication(new MyLibgdxGame(), cfg)
  }
}

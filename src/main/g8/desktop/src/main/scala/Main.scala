package $package$

import com.badlogic.gdx.backends.lwjgl._

object Main extends App {
    val cfg = new LwjglApplicationConfiguration()
    cfg.title = "$name$"
    cfg.height = 480
    cfg.width = 320
    cfg.useGL20 = true
    new LwjglApplication(new $main_class$(), cfg)
}

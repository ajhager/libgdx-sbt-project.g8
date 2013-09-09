package $package$

import com.badlogic.gdx.backends.lwjgl._

object Main extends App {
    val cfg = new LwjglApplicationConfiguration()
    cfg.title = "$name$"
    cfg.width = 320
    cfg.height = 480
    cfg.useGL20 = false
    cfg.forceExit = false
    new LwjglApplication(new $main_class$(), cfg)
}

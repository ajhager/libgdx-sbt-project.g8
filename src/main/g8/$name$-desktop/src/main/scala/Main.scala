package $package$

import com.badlogic.gdx.backends.lwjgl.LwjglApplication

object Main {
  def main(args: Array[String]): Unit = {
    new LwjglApplication(new BaseGame(), "Base", 800, 480, false);
  }
}

package $package$

import com.badlogic.gdx.backends.lwjgl.LwjglApplication

object Main {
  def main(args: Array[String]): Unit = {
    new LwjglApplication(new MyGame(), "Base", 480, 320, false);
  }
}

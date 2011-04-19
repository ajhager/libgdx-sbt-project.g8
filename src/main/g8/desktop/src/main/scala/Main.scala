package $package$

import com.badlogic.gdx.backends.$desktop_backend$._

object Main {
  def main(args: Array[String]): Unit = {
    new $desktop_backend$Application(new MyGame(), "$name$", 480, 320, false);
  }
}

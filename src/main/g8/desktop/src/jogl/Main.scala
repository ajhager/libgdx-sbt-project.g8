package $package$

import com.badlogic.gdx.backends.jogl.JoglApplication

object Main {
  def main(args: Array[String]): Unit = {
    new JoglApplication(new MyGame(), "$name$", 480, 320, false)
  }
}

package $package$

import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication

import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx

class Main extends IOSApplication.Delegate {
  override protected def createApplication(): IOSApplication = {
    val config = new IOSApplicationConfiguration
    new IOSApplication(new $name;format="Camel"$, config)
  }
}

object Main {
  def main(args: Array[String]) {
      val pool = new NSAutoreleasePool
      UIApplication.main(args, null, classOf[Main])
      pool.drain()
  }
}

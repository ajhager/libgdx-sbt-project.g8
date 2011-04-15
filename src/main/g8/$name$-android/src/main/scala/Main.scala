package $package$

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.ApplicationListener

class MyGame extends ApplicationListener {
  override def create() {
  }

  override def dispose() {
  }

  override def pause() {
  }

  override def render() {
  }

  override def resize(width: Int, height: Int) {
  }

  override def resume() {
  }
}

class Main extends AndroidApplication {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    initialize(new MyGame(), false)
  }
}

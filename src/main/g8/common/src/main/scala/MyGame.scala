package $package$

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL10

class MyGame extends ApplicationListener {
  var fps: FpsDisplay = null

  override def create() {
    fps = new FpsDisplay()    
  }

  override def dispose() {
  }

  override def pause() {
  }

  override def render() {
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    if (fps != null)
      fps.draw()
  }

  override def resize(width: Int, height: Int) {
  }

  override def resume() {
  }
}


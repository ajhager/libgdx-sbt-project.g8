package $package$

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2

class FpsDisplay {
  val batch = new SpriteBatch()
  val font = new BitmapFont(Gdx.files.internal("arial48.fnt"),
                            Gdx.files.internal("arial48.png"),
                            false)
  font.setColor(1f, 1f, 1f, 0.3f)
  val fontP = new Vector2(0, font.getLineHeight() + 3)

  def draw() {
    val fps = Gdx.graphics.getFramesPerSecond().toString()
    batch.begin()
    font.draw(batch, fps, fontP.x, fontP.y)
    batch.end()
  }
}

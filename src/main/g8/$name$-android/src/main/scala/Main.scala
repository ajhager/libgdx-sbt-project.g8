package $package$

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class Main extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new TextView(this) {
      setText("hello, world")
    })
  }
}

//import com.badlogic.gdx.backends.android.AndroidApplication

// class BaseAndroid extends AndroidApplication {
//   override def onCreate(savedInstanceState: Bundle) {
//     super.onCreate(savedInstanceState)
//     initialize(new BaseGame(), true)
//   }
// }

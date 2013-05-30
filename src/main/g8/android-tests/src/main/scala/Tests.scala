package $package$.tests

import $package$._
import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class AndroidTests extends AndroidTestCase {
  def testPackageIsCorrect() {
    assertEquals("$package$", getContext.getPackageName)
  }
}

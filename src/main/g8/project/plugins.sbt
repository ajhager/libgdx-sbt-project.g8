resolvers += Resolver.url("scalasbt snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.hanhuy.sbt" % "android-sdk-plugin" % "1.3.6")

addSbtPlugin("org.roboscala" % "sbt-robovm" % "1.0.0-alpha-04-SNAPSHOT")

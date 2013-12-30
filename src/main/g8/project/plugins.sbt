resolvers += Resolver.url("scalasbt snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.scala-sbt" % "sbt-android" % "0.7")

addSbtPlugin("com.hagerbot" % "sbt-robovm" % "0.1.0-SNAPSHOT")

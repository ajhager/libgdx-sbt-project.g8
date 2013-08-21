# libgdx sbt project

Configure and generate a Scala project for [libgdx](http://libgdx.badlogicgames.com/) using [g8](http://github.com/n8han/giter8) and [sbt](https://github.com/sbt/sbt) 0.12.

## Setting up a new project

To use this template, you will need to install g8 first.
Consult g8's [readme](http://github.com/n8han/giter8#readme) for more information.

Then, in your favorite shell, type the command:

    $ g8 ajhager/libgdx-sbt-project

After filling in some information about your project, you can start placing your game's source files and assets in common/src/main/scala and common/src/main/resources, respectively.

## Managing your project

Update to the lastest libgdx nightlies (You will need to do this at least once):

    $ sbt
    > update-gdx 

Run the desktop project:

    > desktop/run

Package the desktop project into single jar:

    > assembly

Run the android project on a device:
  
    > android/start

Visit [android-plugin](https://github.com/jberkel/android-plugin) for a more in-depth guide to android configuration and usage.

## Using unit and instrumentation tests

Run all unit tests from desktop, android and common (subdirectories src/test/scala):

    > test

Run specific set of unit tests:

    > common/test

Use instrumentation tests (directory android-tests):

    > android/emulator-start <tab>
    > android/install
    > android-tests/test

## iOS Support (not currently working)

Open the ios solution in Xamarin Studio and build.

Every time you make changes to your scala files, you will need to clean and build since Xamarin cannot pick up changes to files it does not track.

## Using with popular IDEs

In most cases you will be able to open and edit each sub-project (like common, android or desktop), but you still need to use SBT to build the project though.

### Using with Eclipse

Add following line to your project/plugins.sbt file:

    addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.2.0")

Make sure you have Eclipse 3.7 and the latest ScalaIDE plugin, then type:

    $ sbt eclipse

### Using with Intellij Idea

Add following line to your project/plugins.sbt file:

    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0") 

Make sure you have Intellij Idea 12 and the latest Scala plugin, then type:

    $ sbt "project common" gen-idea

You can now open the project, then create an "Android Application" build config using the android module and an "Application" build config using the desktop module.

### Using with Netbeans

Add following line to your project/plugins.sbt file:

    addSbtPlugin("org.netbeans.nbsbt" % "nbsbt-plugin" % "1.0.2")

Make sure you have Netbeans 7.2 and the latest NBScala plugin, then type:

    $ sbt netbeans

### Using with Emacs/Sublime Text

Add following line to your project/plugins.sbt file:

    addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.0")

Make sure you have EMACS 22 or Sublime Text Editor 2 with Sublime-ENSIME integration and the latest ENSIME plugin (0.9+), then type:

    $ sbt "ensime generate"

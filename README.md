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

    > desktop/assembly

Run the android project on a device:
  
    > android/start

Visit [android-plugin](https://github.com/jberkel/android-plugin) for a more in-depth guide to android configuration and usage.

Run the ios project on a device:

    > ios/device

Visit [sbt-robovm](https://github.com/ajhager/sbt-robovm) for a more in-depth guide to ios configuration and usage.

## Using unit tests

Run all unit tests from desktop, android and common (subdirectories src/test/scala):

    > test

Run specific set of unit tests:

    > common/test

## Using with popular IDEs

In most cases you will be able to open and edit each sub-project (like common, android or desktop), but you still need to use SBT to build the project.

See [here](https://github.com/ajhager/libgdx-sbt-project.g8/wiki/IDE-Plugins) for details about sbt plugins for each editor.

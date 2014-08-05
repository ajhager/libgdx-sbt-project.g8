# libgdx sbt project

Configure and generate a Scala project for [libgdx](http://libgdx.badlogicgames.com/) using [g8](http://github.com/n8han/giter8) and [sbt](http://www.scala-sbt.org/) 0.13.5+.

## Setting up a new project

To use this template, you will need to install g8 first.
Consult g8's [readme](http://github.com/n8han/giter8#readme) for more information.

Then, in your favorite shell, type the command:

    $ g8 ajhager/libgdx-sbt-project

After filling in some information about your project, you can start placing your game's source files and assets in core/src/main/scala and android/assets respectively.

## Managing your project

Start `sbt` in project root:

    $ sbt

Run the desktop project:

    > desktop/run

Package the desktop project into single jar:

    > assembly

Create Android package in debug mode:
  
    > android:package-debug

Visit [android-sdk-plugin](https://github.com/pfn/android-sdk-plugin) for a more in-depth guide to android configuration and usage.

## Using with popular IDEs

In most cases you will be able to open and edit each sub-project (like common, android or desktop), but you still need to use SBT to build the project.

See [here](https://github.com/ajhager/libgdx-sbt-project.g8/wiki/IDE-Plugins) for details about sbt plugins for each editor.

# libgdx sbt project

A [g8](http://github.com/n8han/giter8) template to get up and running with Scala and [libgdx](http://code.google.com/p/libgdx/).

## Setting up a new project

To use this template, you will need to install g8 first.
Read g8's [readme](http://github.com/n8han/giter8#readme) for more information.

Then, in your favorite shell, type the command:

    $ g8 ajhager/libgdx-sbt-project

After answering a few simple questions, cd into your project directory and type:

    $ sbt update
    $ sbt update-gdx

Put your game's source files and assets in common/src/main/scala and common/src/main/resources, respectively.

## Where to go from here

You now have a few commands for managing your project.

Update to the lastest libgdx nightlies:

   $ sbt update-gdx 

Run the desktop project:

   $ sbt "project desktop" run

Run the android project on a device:
  
   $ sbt "project android" start-device

Then visit [android-plugin](https://github.com/jberkel/android-plugin) for a more in-depth guide to android configuration and usage.

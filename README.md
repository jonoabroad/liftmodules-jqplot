# JqPlot Lift Module version 0.13.0

Built using sbt 0.13.0

Add graphs via [JqPlot]( jqplot.com )  into a [Lift](http://www.liftweb.net) application.


## Using this module

Add the following repository to your SBT project file:

  lazy val liftModulesRelease = "liftmodules repository" at "http://repository-liftmodules.forge.cloudbees.com/release/"

And then include this dependency:

  "net.liftmodules.jqplot" %% "jqplot" % (liftVersion+"-0.13.0")

In your application's Boot.boot code:

  net.liftmodules.jqlot.JqPlot.init

## Supported Scala and Lift versions

Scala  2.10.3: from Lift 2.5 and onwards

## Tested on 

Chrome  13.0.X
Opera   11.50
IE      8.0
FF      6.0
Safari  5.1

## To build from source:

    $ git clone git://github.com/jonoabroad/liftmodules-jqplot.git
    $ cd liftmodules-jqplot
    $ sbt
    > update
    > publish-local

## TODO

  * Test 2.5 
  * Tidy up & finish options. 
  * Only add header information once if multiple graphs are on the same page.
  * Add ajax data & options 
   
   

name := "JqPlot"
 
scalaVersion := "2.10.3" 

version := "2.5-0.14.0"

organization := "net.liftmodules"

scalacOptions ++= Seq("-unchecked", "-deprecation")
 
// If using JRebel
//jettyScanDirs := Nil

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

libraryDependencies ++= {
  val liftVersion = "2.5" 
  Seq("net.liftweb" %% "lift-webkit" % liftVersion % "compile->default")
}

// Customize any further dependencies as desired
libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "2.2.3"  % "test",
    "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
    "ch.qos.logback" % "logback-classic" % "0.9.29" % "compile->default" 
  )

 // To publish to the Cloudbees repos:

publishTo := Some("liftmodules repository" at "https://repository-liftmodules.forge.cloudbees.com/release/")
 
credentials += Credentials( file("/private/liftmodules/cloudbees.credentials") )

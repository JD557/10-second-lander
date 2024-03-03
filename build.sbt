import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.scalanative.build._

name := "10 Second Lunar Lander"

version := "1.1"

ThisBuild / scalaVersion := "3.3.3"

lazy val root =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .in(file("."))
    .settings(
      Seq(
        libraryDependencies ++= List(
          "eu.joaocosta" %%% "minart" % "0.6.0-M3"
        )
      )
    )
    .jsSettings(
      Seq(
        scalaJSUseMainModuleInitializer := true
      )
    )
    .nativeSettings(
      Seq(
        nativeConfig ~= {
          _
          .withLinkStubs(true)
          .withMode(Mode.releaseFull)
          .withLTO(LTO.thin)
          .withGC(GC.commix)
          .withEmbedResources(true)
        }
      )
    )
    .settings(name := "10 Second Lunar Lander Root")

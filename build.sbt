import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "10 Second Lunar Lander"

version := "1.0"

ThisBuild / scalaVersion := "3.2.2"

lazy val root =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .in(file("."))
    .settings(
      Seq(
        libraryDependencies ++= List(
          "eu.joaocosta" %%% "minart" % "0.5.1"
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
        nativeLinkStubs := true,
        nativeMode      := "release",
        nativeLTO       := "thin",
        nativeGC        := "commix",
        nativeConfig ~= {
          _.withEmbedResources(true)
        }
      )
    )
    .settings(name := "10 Second Lunar Lander Root")

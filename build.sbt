name := "github-sync"
version := "0.1"
scalaVersion := "2.13.3"

scalacOptions ++= Seq(
  "-target:11",
  "-language:higherKinds"
)

enablePlugins(NativeImagePlugin)

val nativeImageSettings = Seq(
  "--allow-incomplete-classpath",
  "--no-fallback",
  "--initialize-at-build-time",
  "--enable-http",
  "--enable-https",
  "--enable-all-security-services",
  "--initialize-at-build-time=scala.runtime.Statics$VM"
)

nativeImageReady := { () =>
  ()
}
nativeImageOptions ++= nativeImageSettings

libraryDependencies ++= Seq(
  "org.http4s"             %% "http4s-blaze-client"            % "0.21.7",
  "org.http4s"             %% "http4s-circe"                   % "0.21.7",
  "io.circe"               %% "circe-core"                     % "0.13.0",
  "org.typelevel"          %% "cats-effect"                    % "2.2.0",
  "org.typelevel"          %% "cats-core"                      % "2.2.0",
  "com.monovore"           %% "decline"                        % "1.3.0",
  "com.monovore"           %% "decline-effect"                 % "1.3.0",
  "ch.qos.logback"         % "logback-classic"                 % "1.2.3",
  "org.scalatest"          %% "scalatest"                      % "3.2.2" % Test,
  "org.scalatestplus"      %% "scalacheck-1-14"                % "3.2.2.0" % Test,
  "org.scalacheck"         %% "scalacheck"                     % "1.14.3" % Test,
  "com.danielasfregola"    %% "random-data-generator-magnolia" % "2.9" % Test,
  "com.github.tomakehurst" % "wiremock-jre8-standalone"        % "2.27.2" % Test
)

lazy val staticNativeImage =
  taskKey[File]("Build a standalone Linux executable using GraalVM Native Image")

staticNativeImage := {
  import sbt.Keys.streams
  import scala.sys.process._
  val assemblyFatJar     = assembly.value
  val assemblyFatJarPath = assemblyFatJar.getParent
  val assemblyFatJarName = assemblyFatJar.getName
  val outputPath         = (baseDirectory.value / "target" / "native-image").getAbsolutePath
  val outputName         = name.value
  val nativeImageDocker  = "glyderj/native-image:20.2.0-java11"

  val cmd =
    s"""docker run
       | --volume $assemblyFatJarPath:/opt/assembly
       | --volume $outputPath:/opt/native-image
       | $nativeImageDocker
       | --static
       | --libc=musl
       | ${nativeImageSettings.mkString(" ")}
       | -jar /opt/assembly/$assemblyFatJarName
       | $outputName.out""".stripMargin.filter(_ != '\n')

  val log = streams.value.log
  log.info(s"Building native image from $assemblyFatJarName")
  log.info(cmd)

  val result = cmd ! log
  if (result == 0) file(s"$outputPath/$outputName")
  else {
    log.error(s"Native image command failed:\n $cmd")
    throw new Exception("Native image command failed")
  }
}

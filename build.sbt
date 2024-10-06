name := "github-sync"
version := "0.1"
scalaVersion := "2.13.3"

enablePlugins(NativeImagePlugin)

nativeImageReady := { () =>
  ()
}
nativeImageOptions ++= Seq(
  "--allow-incomplete-classpath",
  "--no-fallback",
  "--initialize-at-build-time",
  "--enable-http",
  "--enable-https",
  "--enable-all-security-services",
  // "--static" not supported on mac
)

libraryDependencies ++= Seq(
  "org.http4s"             %% "http4s-blaze-client"            % "0.21.7",
  "org.http4s"             %% "http4s-circe"                   % "0.21.7",
  "io.circe"               %% "circe-core"                     % "0.13.0",
  "org.typelevel"          %% "cats-effect"                    % "2.2.0",
  "org.typelevel"          %% "cats-core"                      % "2.2.0",
  "org.typelevel"          %% "case-insensitive"               % "1.4.0",
  "org.typelevel"          %% "case-insensitive-testing"       % "1.4.0",
  "com.monovore"           %% "decline"                        % "1.3.0",
  "com.monovore"           %% "decline-effect"                 % "1.3.0",
  "ch.qos.logback"         % "logback-classic"                 % "1.2.3",
  "org.scalatest"          %% "scalatest"                      % "3.2.2" % Test,
  "org.scalatestplus"      %% "scalacheck-1-14"                % "3.2.2.0" % Test,
  "org.scalacheck"         %% "scalacheck"                     % "1.14.3" % Test,
  "com.danielasfregola"    %% "random-data-generator-magnolia" % "2.9" % Test,
  "com.github.tomakehurst" % "wiremock-jre8-standalone"        % "2.27.2" % Test
)

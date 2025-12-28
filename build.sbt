name := "github-sync"
version := "0.2"
scalaVersion := "2.13.18"

enablePlugins(NativeImagePlugin)

nativeImageReady := { () =>
  ()
}
nativeImageVersion := "22.3.1"
nativeImageOptions ++= Seq(
  "--no-fallback",
  "--enable-http",
  "--enable-https"
  // "--static" not supported on mac
)

libraryDependencies ++= Seq(
  "org.http4s"            %% "http4s-ember-client"            % "0.23.33",
  "org.http4s"            %% "http4s-circe"                   % "0.23.33",
  "io.circe"              %% "circe-core"                     % "0.14.15",
  "org.typelevel"         %% "cats-effect"                    % "3.6.3",
  "org.typelevel"         %% "cats-core"                      % "2.13.0",
  "org.typelevel"         %% "case-insensitive"               % "1.5.0",
  "org.typelevel"         %% "case-insensitive-testing"       % "1.5.0",
  "com.monovore"          %% "decline"                        % "2.5.0",
  "com.monovore"          %% "decline-effect"                 % "2.5.0",
  "ch.qos.logback"         % "logback-classic"                % "1.5.23",
  "org.scalatest"         %% "scalatest"                      % "3.2.19"  % Test,
  "org.scalatestplus"     %% "scalacheck-1-14"                % "3.2.2.0" % Test,
  "org.scalacheck"        %% "scalacheck"                     % "1.19.0"  % Test,
  "com.danielasfregola"   %% "random-data-generator-magnolia" % "2.9"     % Test,
  "com.github.tomakehurst" % "wiremock-jre8-standalone"       % "3.0.1"   % Test
)

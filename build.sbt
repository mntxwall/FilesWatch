name := "FilesWatch"
version := "0.4.2"
scalaVersion := "2.12.8"

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.4.10")
enablePlugins(JavaAppPackaging)

    name := "FilesWatch"
      version := "0.1"
    scalaVersion := "2.12.8"
    maintainer := "wei.com"
    mainClass in Compile := Some("FilesWatch")

mappings in Universal += {
  ((resourceDirectory in Compile).value / "application.conf") -> "conf/app.conf"
}
bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/app.conf""""
libraryDependencies += "com.typesafe" % "config" % "1.3.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"





name := "BigData_dz"
version := "1.0"
scalaVersion := "3.8.3"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
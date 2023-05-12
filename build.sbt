enablePlugins(GatlingPlugin)

scalaVersion := "2.13.10"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-11", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

val gatlingVersion = "3.9.0"
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test,it"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.4.0"
libraryDependencies += "ru.tinkoff" %% "gatling-kafka-plugin" % "0.11.0"
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "3.4.0"
libraryDependencies += "org.apache.avro" % "avro" % "1.11.1"
libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "4.1.0"
libraryDependencies += "io.confluent" % "kafka-streams-avro-serde" % "7.3.0"








//    schemaRegistryUrl := "http://test-schema-registry:8081",
resolvers ++= Seq(
  "Confluent" at "https://packages.confluent.io/maven/",
)
resolvers ++= Resolver.sonatypeOssRepos("public")

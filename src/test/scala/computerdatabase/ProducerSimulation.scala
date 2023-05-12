package computerdatabase


import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.apache.kafka.clients.producer.ProducerConfig
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol

import scala.concurrent.duration.DurationInt

class ProducerSimulation extends Simulation {

  val kafkaConf: KafkaProtocol = kafka
    .topic("my-topic")
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG                   -> "1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "51.77.132.116:9092",
      )
    )

  val scn = scenario("Kafka Simulation")
    .exec(
      kafka("Kafka Request")
        .send[String]("Hello, World!")
    )
    .pause(1 second)
    .exec(
      http("HTTP Request")
        .get("https://reqres.in/api/users?page=2")
    )

  setUp(scn.inject(atOnceUsers(10)).protocols(kafkaConf))
}


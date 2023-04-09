package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.apache.kafka.clients.producer.ProducerConfig
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol

import scala.concurrent.duration.DurationInt

class ProducerSimulation2 extends Simulation {

  val httpConf = http.baseUrl("https://reqres.in/api")

  val kafkaProducerConf: KafkaProtocol =
    kafka
      .topic("SUCCESS")
      .properties(
        Map(
          ProducerConfig.ACKS_CONFIG                   -> "1",
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> "org.apache.kafka.common.serialization.StringSerializer",
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "51.77.132.116:9092",
        ),
      )


  val scn1 = scenario("SEND MESSAGE")
    // .exec(
    // kafka("Send message")
    // .send[String]("Hello, world!. L'état du code d'état est OK")
    //)
    .pause(1 second)
    .exec(
      http("Send message to consumer")
        .get("/users?page=2")
        .check(status.is(200).extract(_.response.status.intValue()).saveAs("status"))
    )
    .exec(
      session => {
        if (session("status").as[Int] == 200) {
          kafka("Send message success ")
            //.using(kafkaSuccessProducerConf)
            .send[String]("The test succeeded!")
        }
        session
      }
    )

  setUp(scn1.inject(atOnceUsers(5))).protocols(httpConf, kafkaProducerConf)
}

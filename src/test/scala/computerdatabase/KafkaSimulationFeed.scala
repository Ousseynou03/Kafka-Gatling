package computerdatabase

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import akka.util.Helpers.Requiring
import io.gatling.commons.validation.SuccessWrapper
import io.gatling.core.Predef._
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration._


import scala.language.postfixOps

class KafkaSimulationFeed extends Simulation{

  private val VersionAppli: String = System.getProperty("VersionApp", "Vxx.xx.xx")
  private val TpsMonteEnCharge: Int = System.getProperty("tpsMonte", "5").toInt
  private val TpsPalier: Int = System.getProperty("tpsPalier", (2 * TpsMonteEnCharge).toString).toInt
  private val TpsPause: Int = System.getProperty("tpsPause", "60").toInt
  private val DureeMax: Int = System.getProperty("dureeMax", "1").toInt + 5 * (TpsMonteEnCharge + TpsPalier)

  private val tpsPaceDefault: Int = System.getProperty("tpsPace", "1000").toInt
  private val tpsPacingProducts: Int = System.getProperty("tpsPaceProducts", tpsPaceDefault.toString).toInt

  private val LeCoeff: Int = System.getProperty("coeff", "10").toInt
  private val nbVu: Int = LeCoeff * 1
  private val FichierPath: String = System.getProperty("dataDir", "data/")
  private val FichierDataKafkaId: String = "JddKafka.csv"

  val jddDataKafkaId = csv(FichierPath + FichierDataKafkaId).circular


  val kafkaTopic = "newVerifyTopic"
  val kafkaBootstrapServer = "51.77.132.116:9092"


  val kafkaConf: KafkaProtocol = kafka
    .topic("newVerifyTopic")
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> kafkaBootstrapServer,
      )
    )

  val kafkaConsumerConf = {
    val props = new java.util.Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props
  }
  val kafkaConsumer = new KafkaConsumer[String, String](kafkaConsumerConf)
  kafkaConsumer.subscribe(java.util.Collections.singletonList(kafkaTopic))

//Scenario
  val scnSendMessage = scenario("Kafka Simulation Feed")
    .exec(flushSessionCookies)
    .exec(flushHttpCache)
    .exec(flushCookieJar)

    .pace(tpsPacingProducts milliseconds)
    .feed(jddDataKafkaId)
    .exec { session =>
      println(session("Message").as[String])
      session
    }
    .exec(
      kafka("Kafka Request")
        .send[String]("${Message}")
    )
    .pause(TpsPause second)

  before {
    kafkaConsumer.poll(0)
  }

  after {
    kafkaConsumer.close()
  }

  setUp(
    scnSendMessage.inject(rampUsers(nbVu * 3) during (TpsMonteEnCharge minutes), nothingFor(TpsPalier minutes)),
  ).protocols(kafkaConf)
    .maxDuration(DureeMax minutes)




}

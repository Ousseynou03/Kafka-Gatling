package computerdatabase

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

class KafkaSimulation extends Simulation {


  val kafkaTopic = "newVerifyTopic"
  val kafkaBootstrapServer = "51.77.132.116:9092"
  val kafkaConf: KafkaProtocol = kafka
    .topic("newVerifyTopic")
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "51.77.132.116:9092",
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

  before {
    kafkaConsumer.poll(0)
  }

  after {
    kafkaConsumer.close()
  }

  val scnSendMessage = scenario("Kafka Simulation")
    .exec(
      kafka("Kafka Request")
        .send[String]("Hello, World!")
    )
    .pause(1 second)
  setUp(scnSendMessage.inject(atOnceUsers(10)).protocols(kafkaConf))


}


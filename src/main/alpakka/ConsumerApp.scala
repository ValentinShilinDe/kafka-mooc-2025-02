package alpakka

import akka.actor.ActorSystem

import scala.util.{Failure, Success}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Sink}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import akka.kafka.scaladsl.Consumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.{Level, Logger}

object  ConsumerApp extends App {
  implicit val system: ActorSystem = ActorSystem("producer")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.ERROR)
  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("akka.kafka.consumer")

  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
  val consumer = Consumer.plainSource(consumerSettings, Subscriptions.topics("test"))
    .runWith(Sink.foreach(println))
  consumer onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }


}
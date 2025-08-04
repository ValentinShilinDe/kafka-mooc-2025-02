package ru.otus.module3.zio

import zio._
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde._
import zio.stream._
import scala.util.Random
import zio.Console
import zio.durationInt    // ← вот нужный импорт

import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory

object MainApp extends ZIOAppDefault {
  LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger].setLevel(Level.ERROR)

  val producerLayer:ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped{
      Producer.make(ProducerSettings(List("localhost:29093")))
    }

  val consumerLayer:ZLayer[Any, Throwable,Consumer] =
    ZLayer.scoped {
      Consumer.make(
        ConsumerSettings(List("localhost:29093"))
          .withGroupId("group")
          .withClientId("zio-client")
      )
    }

  val producerStream:ZStream[Producer, Throwable, Nothing] =
    ZStream
      .repeatZIO(ZIO.attempt(Random.nextInt(Int.MaxValue)))
      .schedule(Schedule.spaced(1.second))
      .mapZIO{random =>
        Producer.produce[Any, Long, String](
          topic = "random",
          key= random%4,
          value=random.toString,
          keySerializer = Serde.long,
          valueSerializer = Serde.string
        )
      }.drain

  val consumerStream:ZStream[Consumer, Throwable, Nothing] =
    Consumer.plainStream(Subscription.topics("random"),
        Serde.long, Serde.string)
      .tap(record => Console.printLine(s"Consumed: ${record.value}"))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  override def run: ZIO[Any, Throwable, Unit] =
    producerStream
      .merge(consumerStream)
      .runDrain
      .provide(producerLayer, consumerLayer)
}
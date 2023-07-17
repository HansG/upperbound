import cats.effect.implicits.genSpawnOps
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.toFunctorOps
import fs2.Stream

import scala.concurrent.duration._

object TestTry extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    def job(n : Int) = IO.monotonic  <* IO.sleep(6.second) <* IO(println("do JOb "+n))

    def producer =
      Stream(job(_))
        .repeatN(5L)
        .covary[IO]
        .meteredStartImmediately(6.seconds)
        .zipWithIndex
        .evalTap( j => IO(println("emit  "+j))    )
        .evalMap( job =>   job._1(job._2.toInt)    )
        .compile
        .drain

    val res = Resource.make[IO, FiniteDuration](job(-1))(_ => IO(println("End ")))

     // res.surround(producer )
    //todo job(-10) -> res.use ( job
    job(-10).foreverM.background.surround(producer ).as(ExitCode.Success)
    //job(-10).background.use( d =>  d).as(ExitCode.Success)

  }
}

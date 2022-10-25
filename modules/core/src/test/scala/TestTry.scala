import cats.effect.implicits.genSpawnOps
import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.Stream

import scala.concurrent.duration._

object TestTry extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    def job = IO.monotonic <* IO(println("do JOb ")) <* IO.sleep(1.second)

    def producer =
      Stream(job)
        .repeatN(10L)
        .covary[IO]
        .meteredStartImmediately(0.5.seconds)
        .evalMap( job =>   job    )
        .compile
        .toVector

    val res = Resource.make[IO, FiniteDuration](IO.monotonic <* IO(println("Satrt ")))(_ => IO(println("End ")))

      res.background.surround[Vector[FiniteDuration]](producer )

  }
}

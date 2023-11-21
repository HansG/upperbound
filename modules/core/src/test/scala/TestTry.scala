import cats.effect.implicits.genSpawnOps
import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.Stream

import scala.concurrent.duration._

object TestTry extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    // Start a fiber that continuously prints "A".
    // After 10 seconds, cancel the fiber.
   val prg =  (IO.delay(println("A")).foreverM).start.flatMap { fiber =>
      IO.sleep(1.seconds) *> fiber.cancel
    }
    val prg2 = (IO.delay(println("A")).foreverM).background.surround(IO.sleep(0.5.seconds))

    for {
//      fa <- (IO.sleep(1.second) *> IO(println("A1")) *> IO(println("A2"))).start
//      fb <- (IO.sleep(2.second) *> IO(println("B1")) *> IO(println("B2"))).start
//      fae <- fa.join
//      fbe <- fb.join
//      _ <- (IO(println("Ende")) )
      _ <- prg2
    } yield ExitCode.Success


  }

  //override
   def run1(args: List[String]): IO[ExitCode] = {
    def job = IO.monotonic <* IO(println("do JOb ")) <* IO.sleep(1.second)
    def jobn(n : Int) = IO.monotonic  <* IO.sleep(6.second) <* IO(println("do JOb "+n))

    def producer =
      Stream(jobn(_))
        .repeatN(5L)
        .covary[IO]
        .meteredStartImmediately(6.seconds)
        .zipWithIndex
        .evalTap( j => IO(println("emit  "+j))    )
        .evalMap( job_n =>   job_n._1(job_n._2.toInt)    )
        .compile
        .drain

    val res = Resource.make[IO, FiniteDuration](jobn(-1))(_ => IO(println("End ")))

     // res.surround(producer )
    //todo job(-10) -> res.use ( job
    jobn(-10).foreverM.background.surround(producer ).as(ExitCode.Success)
    //job(-10).background.use( d =>  d).as(ExitCode.Success)
    //  res.surround[Vector[FiniteDuration]](producer ).as(ExitCode.Success)

  }
}

import cats.effect.IO
import cats.syntax.all._
import cats.effect._
import cats.effect.testkit.TestControl
import fs2._

import scala.concurrent.duration._



object Q  extends IOApp {
  Stream.exec(SyncIO(println("Ran"))).covaryOutput[Int].compile.toVector

  Stream(1, 2, 3).append(Stream(4, 5, 6)).mapChunks { c =>
    val ints = c.toArraySlice
    for (i <- 0 until ints.values.size) ints.values(i) = 2 * ints.values(i)
    ints
  }.toList

  def job(dur : FiniteDuration) = {
    IO.monotonic <* IO.sleep(dur) flatTap(d => IO(println(d)))
  }

  val j = job(1.seconds).start >> job(3.seconds).start >> job(2.seconds).start
  val j1 = job(1.seconds) >> job(3.seconds) >> job(2.seconds)



  override def run(args: List[String]): IO[ExitCode] = (TestControl.executeEmbed(j) >> j1 ).as(ExitCode.Success)
}
/*
for {
  fa <- ioa.start
  fb <- iob.start
  a <- fa.join
  b <- fb.join
} yield (a, b)





*/

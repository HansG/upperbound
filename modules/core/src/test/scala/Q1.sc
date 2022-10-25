import cats.effect.IO
import cats.syntax.all._
import cats.effect._
import fs2._
import scala.concurrent.duration._
import cats.effect.unsafe.implicits.global
//import cats.effect.testkit.TestControl

import scala.concurrent.duration.{FiniteDuration, _}

Stream.exec(SyncIO(println("Ran"))).covaryOutput[Int].compile.toVector.unsafeRunSync()

Stream(1, 2, 3).append(Stream(4, 5, 6)).mapChunks { c =>
  val ints = c.toArraySlice
  for (i <- 0 until ints.values.size) ints.values(i) = 2 * ints.values(i)
  ints
}.toList

def job(dur : FiniteDuration) = {
  IO.monotonic <* IO.sleep(dur) flatTap(d => IO(println(d)))
}

val j = job(1.seconds).start >> job(3.seconds).start >> job(2.seconds).start

//TestControl.executeEmbed(j).unsafeRunSync()



/*
for {
  fa <- ioa.start
  fb <- iob.start
  a <- fa.join
  b <- fb.join
} yield (a, b)



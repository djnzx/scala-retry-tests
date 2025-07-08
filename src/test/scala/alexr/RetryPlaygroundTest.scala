package alexr

import alexr.RetryPlayground._
import cats.effect._
import cats.implicits._
import munit.CatsEffectSuite

class RetryPlaygroundTest extends CatsEffectSuite {

  val sv = new RetryPlayground[IO]
  import sv.RetryOps

  test("trivial") {
    sv.trivial()
      .assert(_ == 42)
  }

  test("capture errors") {
    sv.alwaysFail()
      .attempt
      .assert(_ == ServiceError(409).asLeft)
  }

  test("sporadically - result") {
    sv.failsSporadically(11)
      .assert(_ == 11)
  }

  test("sporadically - failure") {
    sv.failsSporadically(5)
      .attempt
      .assert(_ == ServiceError(409).asLeft)
  }

  test("flaky-controlled - succeed - no retries") {
    Ref[IO].of(0)
      .flatMap(st => sv.flaky(st).retryTwiceThenFail)
      .assert(_ == "Ok")
  }

  test("flaky-controlled - succeed - 1 retry") {
    Ref[IO].of(1)
      .flatMap(st => sv.flaky(st).retryTwiceThenFail)
      .assert(_ == "Ok")
  }

  test("flaky-controlled - succeed - 2 retry") {
    Ref[IO].of(2)
      .flatMap(st => sv.flaky(st).retryTwiceThenFail)
      .assert(_ == "Ok")
  }

  test("flaky-controlled - failure - only 2 of 3 retries made") {
    Ref[IO].of(3)
      .flatMap(st => sv.flaky(st).retryTwiceThenFail)
      .attempt
      .assert(_ == ServiceError(511).asLeft)
  }

  test("flaky-controlled - failure - shouldn't be retried") {
    Ref[IO].of(5)
      .flatMap(state => sv.flakyButShouldNotBeHandled(state).retryTwiceThenFail)
      .attempt
      .assert(_ == ServiceError(400).asLeft)
  }

}

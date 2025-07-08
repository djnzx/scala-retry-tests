package alexr

import alexr.RetryPlayground._
import cats.effect._
import cats.implicits._
import retry.RetryDetails
import retry.RetryDetails.GivingUp
import retry.RetryPolicies._
import retry.implicits._
import scala.concurrent.duration.DurationInt

object RetryPlayground {
  case class ServiceError(code: Int) extends Throwable
}

class RetryPlayground[F[_]](implicit F: Async[F]) {

  def logF(s: String) =
    F.delay(println(s))

  def random(): F[Double] =
    F.delay(scala.util.Random.nextDouble())

  def trivial(): F[Int] =
    42.pure[F]

  def alwaysFail(): F[Int] =
    ServiceError(409).raiseError[F, Int]

  def failsSporadically(x: Double): F[Int] = x match {
    case x if x < 10 => ServiceError(409).raiseError[F, Int]
    case x           => x.toInt.pure[F]
  }

  // we can't modify this code,
  // but it fails in 0.0001% cases, for example inaccessibility third party service, or some I/O error, and:
  // 1. we need to handle it
  // 2. we need to write a unit test for handling
  def hiddenTreasure(): F[Int] =
    random()
      .flatMap {
        case x if x < 0.001 => ServiceError(403).raiseError[F, Int]
        case x              => (x * 100).toInt.pure[F]
      }

  def isWorthRetrying(t: Throwable): F[Boolean] = t match {
    case ServiceError(code) if code >= 500 => true.pure[F]
    case _                                 => false.pure[F]
  }

  val retryPolicy = constantDelay[F](10.millis) join limitRetries[F](2)

  def onError(t: Throwable, rd: RetryDetails): F[Unit] = rd match {
    case GivingUp(_, _) => logF("R.Giving Up") >> t.raiseError[F, Unit]
    case _              => logF("R.Retrying...")
  }

  implicit class RetryOps[A](fa: F[A]) {
    def retryTwiceThenFail: F[A] =
      fa.retryingOnSomeErrors(
        isWorthRetrying,
        retryPolicy,
        onError
      )
  }

  def hiddenTreasureHandled() =
    hiddenTreasure()
      .retryTwiceThenFail

  // we need flaky function in a controllable way
  def flaky(state: Ref[F, Int]): F[String] =
    state.modify {
      case 0 => (0, logF("f.succeed") >> "Ok".pure[F])
      case n => (n - 1, logF("f.failure") >> ServiceError(511).raiseError[F, String])
    }.flatten

  def flakyButShouldNotBeHandled(state: Ref[F, Int]): F[String] =
    state.modify {
      case 0 => (0, "Ok".pure[F])
      case n => (n - 1, ServiceError(400).raiseError[F, String])
    }.flatten

}

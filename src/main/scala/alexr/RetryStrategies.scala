package alexr

import cats.effect._
import retry.RetryPolicies._
import scala.concurrent.duration.DurationInt

object RetryStrategies {

  // 1 2 4 8 16 32 64...
  val exponentialInf = exponentialBackoff[IO](1.second)

  // 1 2 4 8 16
  val exponential5 = exponentialInf join limitRetries[IO](5)

  // 1 2 4 8 16 10 10...
  val exponential5constantInf = exponential5 followedBy constantDelay(10.seconds)

  // 1 2 4 8 16 10 10 10 10 10
  val retryPolicy2 = exponential5constantInf join limitRetries[IO](10)

}

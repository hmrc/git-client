/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.gitclient

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class ScheduledTaskSpec extends WordSpec with Matchers with MockitoSugar {

  class Dummy {
    def doSomeThing = "do something"
  }

  val dummy = mock[Dummy]

  "ScheduledExecution" should {
    "perform operation periodically at given execution conf" in {

      val latch = new CountDownLatch(2)

      val s = new ScheduledTask {

        val f = scheduledExecution({
          dummy.doSomeThing
          latch.countDown()
        })(ExecutionConfig(1, 10, TimeUnit.MILLISECONDS))

      }

      latch.await(30, TimeUnit.MILLISECONDS)

      Mockito.verify(dummy, Mockito.atLeast(2)).doSomeThing
    }
  }
}

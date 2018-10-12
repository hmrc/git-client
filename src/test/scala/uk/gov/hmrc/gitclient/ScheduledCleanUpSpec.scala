/*
 * Copyright 2016 HM Revenue & Customs
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

import java.nio.file.{Path, Paths}
import java.time.Duration
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.mockito
import org.mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class ScheduledCleanUpSpec extends WordSpec with Matchers with MockitoSugar {

  "ScheduledCleanUp" should {
    "perform cleanup periodically at given execution conf" in {

      val latch                = new CountDownLatch(1)
      val handler: FileHandler = mock[FileHandler]
      val path: Path           = Paths.get("some/path")

      val s = new LocalGitStore(path.toString, "", "", handler, null) with ScheduledCleanUp {
        override lazy val executionConfig: ExecutionConfig = ExecutionConfig(1, 10, TimeUnit.MILLISECONDS)
      }

      latch.await(30, TimeUnit.MILLISECONDS)

      Mockito
        .verify(handler, Mockito.atLeast(2))
        .deleteOldFiles(mockito.Matchers.any[Path], mockito.Matchers.eq(Duration.ofMinutes(5)))
    }
  }
}

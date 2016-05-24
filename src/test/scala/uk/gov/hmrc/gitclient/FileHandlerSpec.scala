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

import java.nio.file.{Files, Path}
import java.time.Duration

import org.scalatest.{Matchers, WordSpec}

class FileHandlerSpec extends WordSpec with Matchers {

  "FileHandler.deleteOldFiles" should {
    "delete old files" in {

      val dir = Files.createTempDirectory("test")

      val directory1: Path = Files.createTempDirectory(dir, "test1")
      val directory2: Path = Files.createTempDirectory(dir, "test2")
      Thread.sleep(2000)
      val directory3: Path = Files.createTempDirectory(dir, "test3")
      Thread.sleep(1000)

      directory1.toFile.exists() shouldBe true
      directory2.toFile.exists() shouldBe true
      directory3.toFile.exists() shouldBe true

      FileHandler().deleteOldFiles(dir, Duration.ofMillis(3000))

      directory1.toFile.exists() shouldBe false
      directory2.toFile.exists() shouldBe false
      directory3.toFile.exists() shouldBe true

    }
  }

}

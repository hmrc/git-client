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
import java.util.function.{Consumer, Predicate}

import org.apache.commons.io.FileUtils

class FileHandler {

  def createTempDir(inPath: Path, prefix: String): Path = Files.createTempDirectory(inPath, prefix)

  def deleteOldFiles(inPath: Path, olderThan: Duration) {

    Files.list(inPath).filter(new Predicate[Path] {
      override def test(t: Path): Boolean = {
        System.currentTimeMillis() - t.toFile.lastModified() >= olderThan.toMillis
      }
    }).forEach(new Consumer[Path] {
      override def accept(t: Path): Unit = FileUtils.deleteQuietly(t.toFile)
    })

  }

}

object FileHandler {
  def apply() = new FileHandler()
}

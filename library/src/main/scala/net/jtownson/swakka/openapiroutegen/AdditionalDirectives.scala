/*
 * Copyright 2017 Jeremy Townson
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

package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.directives.BasicDirectives.{extractRequestContext, provide}
import akka.http.scaladsl.server.directives.FileInfo
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as, entity}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

object AdditionalDirectives {

  def optionalEntity[T](unmarshaller: FromRequestUnmarshaller[T]): Directive1[Option[T]] =
    entity(as[String]).flatMap { stringEntity =>
      if (stringEntity == null || stringEntity.isEmpty) {
        provide(Option.empty[T])
      } else {
        entity(unmarshaller).flatMap(e => provide(Some(e)))
      }
    }

  private def processField(fieldName: String)(formData: Multipart.FormData): Directive1[Option[(FileInfo, Source[ByteString, Any])]] = {
    extractRequestContext.flatMap { ctx ⇒
      implicit val mat = ctx.materializer
      implicit val ec = ctx.executionContext

      val onePartSource: Source[(FileInfo, Source[ByteString, Any]), Any] = formData.parts
        .filter(part ⇒ part.filename.isDefined && part.name == fieldName)
        .map(part ⇒ (FileInfo(part.name, part.filename.get, part.entity.contentType), part.entity.dataBytes))
        .take(1)

      val onePartF: Future[Option[(FileInfo, Source[ByteString, Any])]] = onePartSource.runWith(Sink.headOption[(FileInfo, Source[ByteString, Any])])

      onSuccess(onePartF)
    }
  }

  def optionalFileUpload(fieldName: String): Directive1[Option[(FileInfo, Source[ByteString, Any])]] = {
    optionalEntity(as[Multipart.FormData]).flatMap({
      case Some(formData) =>
        processField(fieldName)(formData)
      case _ =>
        provide(None)
    })
  }
}

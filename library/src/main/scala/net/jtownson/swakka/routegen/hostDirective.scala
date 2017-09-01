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

package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{host, pass}

object hostDirective {

  def apply(apiHost: Option[String]): Directive0 =
    apiHost match {
      case Some(hostName) => host(removePort(hostName))
      case None => pass
    }

  private def removePort(hostName: String) =
    hostName.replaceFirst("\\:\\d+", "")
}

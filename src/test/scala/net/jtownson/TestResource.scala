package net.jtownson

import scala.io.Source

object TestResource {
  def get(resource: String): Source = Source.fromInputStream(getClass.getResourceAsStream(resource))
}

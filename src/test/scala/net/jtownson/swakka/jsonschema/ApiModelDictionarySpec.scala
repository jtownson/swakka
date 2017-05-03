package net.jtownson.swakka.jsonschema

import io.swagger.annotations.ApiModelProperty
import net.jtownson.swakka.jsonschema.ApiModelDictionary.apiModelDictionary
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ApiModelDictionarySpec extends FlatSpec {

  case class A(
                @ApiModelProperty(name = "the name", value = "the value", required = true) foo: Int,
                bar: String,
                baz: Option[Float])

  val dictionary = apiModelDictionary[A]

  "ApiModelDictionary" should "extract ApiModelProperty annotations from a case class" in {
    dictionary("foo") shouldBe ApiModelPropertyEntry(Some("the name"), Some("the value"), true)
  }

  it should "default required to true for non-optional fields" in {
    dictionary("bar") shouldBe ApiModelPropertyEntry(None, None, true)
  }

  it should "default required to false for optional fields" in {
    dictionary("baz") shouldBe ApiModelPropertyEntry(None, None, false)
  }

  it should "maintain field ordering" in {
    dictionary.keys.toList shouldBe List("foo", "bar", "baz")
  }
}

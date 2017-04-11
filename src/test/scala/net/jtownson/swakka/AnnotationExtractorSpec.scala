package net.jtownson.swakka

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import net.jtownson.swakka.AnnotationExtractor._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class AnnotationExtractorSpec extends FlatSpec {

  @ApiModel
  case class D(
                @ApiModelProperty(value = "field 1", notes = "notes 1") id: Int,
                @ApiModelProperty(value = "field 2", notes = "notes 2") data: String
              )

  it should "find swagger annotations on a class" in {

    val annotationClass = classOf[ApiModelProperty]

    constructorAnnotations[D](annotationClass) shouldBe Map(
      "id" -> Set(("value", "field 1"), ("notes", "notes 1")),
      "data" -> Set(("value", "field 2"), ("notes", "notes 2"))
    )
  }
}

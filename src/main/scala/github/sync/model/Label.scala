package github.sync.model

import io.circe.{Decoder, Encoder}
import org.typelevel.ci.CIString

final case class Label(name: CIString, color: String, description: Option[String])

object Label {

  private implicit val ciStringEncoder: Encoder[CIString] = Encoder.encodeString.contramap(_.toString)
  private implicit val ciStringDecoder: Decoder[CIString] = Decoder.decodeString.map(CIString(_))

  implicit val encoder: Encoder[Label] =
    Encoder.forProduct3[Label, CIString, String, Option[String]]("name", "color", "description") { label =>
      (label.name, label.color, label.description)
    }

  implicit val decoder: Decoder[Label] =
    Decoder.forProduct3[Label, CIString, String, Option[String]]("name", "color", "description")(Label.apply)

}

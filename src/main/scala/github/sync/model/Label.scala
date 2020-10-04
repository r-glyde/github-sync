package github.sync.model

import io.circe.{Decoder, Encoder}

final case class Label(name: String, color: String, description: Option[String])

object Label {

  implicit val encoder: Encoder[Label] =
    Encoder.forProduct3[Label, String, String, Option[String]]("name", "color", "description") { label =>
      (label.name, label.color, label.description)
    }

  implicit val decoder: Decoder[Label] =
    Decoder.forProduct3[Label, String, String, Option[String]]("name", "color", "description")(Label.apply)

}

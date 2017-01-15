package spgui

import scalacss.Defaults._

object GlobalCSS extends StyleSheet.Inline {
  import dsl._

  val background = style(
    unsafeRoot("body")(
      backgroundColor.rgb(192,192,192)
    )
  )

  val layout = style("sp-layout")(

  )

  this.addToDocument()
}
package spgui.dashboard

import scalacss.Defaults._

object DashboardCSS extends StyleSheet.Inline {
  import dsl._

  val widgetBgColor = "#f5f5f5"
  val widgetContentBg = "white"
  val widgetHeadingBg = widgetBgColor

  val widgetPanel = style("sp-widget-panel")(
    backgroundColor := widgetBgColor,
    height(100.%%),
    marginBottom(0.px),
    overflow.hidden,
    display.flex,
    flexDirection.column,

    borderRadius(0.px),
    borderWidth(1.px),
    boxShadow := spgui.GlobalCSS.defaultShadow
  )

  val widgetPanelHeader = style("sp-widget-header")(
    padding(2.px),
    display.block,
    backgroundColor := widgetHeadingBg
  )

  val widgetPanelBody = style("sp-panel-body")(
    backgroundColor := widgetBgColor,
    overflow.auto,
    height(100.%%)
  )

  val widgetPanelContent = style("sp-widget-panel-content")(
    backgroundColor := widgetBgColor,
    height(100.%%)
  )

  val reactGridPlaceholder = style("react-grid-placeholder")(
    backgroundColor(rgb(255,102,0)),
    opacity(0.5)
  )

  this.addToDocument()
}
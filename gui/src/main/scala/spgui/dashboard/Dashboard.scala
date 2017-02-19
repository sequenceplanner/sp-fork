package spgui.dashboard

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import diode.react.ModelProxy
import scalajs.js.Dynamic
import scalajs.js.JSON

import spgui.SPWidgetBase
import spgui.circuit.OpenWidget
import spgui.WidgetList
import spgui.circuit.{SPGUICircuit, LayoutUpdated, WidgetLayout}

object Dashboard {
  case class Props(proxy: ModelProxy[List[OpenWidget]])

  class Backend($: BackendScope[Props, Unit]) {
    def render(p: Props) =
      <.div(
        ReactGridLayout(
          width = 1920,
          cols = 8,
          draggableHandle = "." + DashboardCSS.widgetPanelHeader.htmlClass,
          onLayoutChange = (layout => {
            layout.asInstanceOf[LayoutData].foreach(
              g => {
                p.proxy().foreach(widget => if(widget.id == g.i.toInt) {
                  val newLayout = WidgetLayout(g.x, g.y, g.w, g.h)
                  SPGUICircuit.dispatch(LayoutUpdated(widget.id, newLayout))
                })
              }
            )
          }),
          for(openWidget <- p.proxy())
          yield ReactGridLayoutItem(
            key = openWidget.id.toString,
            i = "idkdk",
            x = openWidget.layout.x,
            y = openWidget.layout.y,
            w = openWidget.layout.w,
            h = openWidget.layout.h,
            isDraggable = true,
            isResizable = true,
            child = DashboardItem(
              WidgetList().toMap.apply(openWidget.widgetType)(
                SPWidgetBase(
                  openWidget.id,
                  openWidget.stringifiedWidgetData
                )
              ),
              openWidget.id
            )
          )
        )
      )
  }

  private val component = ReactComponentB[Props]("Dashboard")
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[List[OpenWidget]]) = component(Props(proxy))
}
package spgui.circuit

import diode._
import japgolly.scalajs.react.ReactElement

// state
case class RootModel(availableWidgets: List[(String, ReactElement)], openWidgets: List[ReactElement])

// actions
case class AddWidget(element: ReactElement) extends Action
case class CloseWidget(index: Int) extends Action

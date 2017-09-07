package spgui.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.all.aria

object SPWidgetElements{
  def button(text: String, onClick: Callback): VdomNode =
    <.span(
      text,
      ^.onClick --> onClick,
      ^.className := "btn",
      ^.className := SPWidgetElementsCSS.defaultMargin.htmlClass,
      ^.className := SPWidgetElementsCSS.clickable.htmlClass,
      ^.className := SPWidgetElementsCSS.button.htmlClass
    )
  
  def button(text:String, icon:VdomNode, onClick: Callback): VdomNode =
    <.span(
      <.span(text, ^.className:= SPWidgetElementsCSS.textIconClearance.htmlClass),
      icon,
      ^.onClick --> onClick,
      ^.className := "btn",
      ^.className := SPWidgetElementsCSS.defaultMargin.htmlClass,
      ^.className := SPWidgetElementsCSS.clickable.htmlClass,
      ^.className := SPWidgetElementsCSS.button.htmlClass
    )
  
  def button(icon: VdomNode, onClick: Callback): VdomNode =
    <.span(icon,
      ^.onClick --> onClick,
      ^.className := "btn",
      ^.className := SPWidgetElementsCSS.defaultMargin.htmlClass,
      ^.className := SPWidgetElementsCSS.clickable.htmlClass,
      ^.className := SPWidgetElementsCSS.button.htmlClass
    )

  def dropdown(text: String, contents: Seq[TagMod]): VdomElement =
    <.span(
      ^.className:= SPWidgetElementsCSS.dropdownRoot.htmlClass,
      <.span(
        ^.className:= SPWidgetElementsCSS.dropdownOuter.htmlClass,
        ^.className := SPWidgetElementsCSS.defaultMargin.htmlClass,
        ^.className:= "dropdown",
        <.span(
          <.span(text, ^.className:= SPWidgetElementsCSS.textIconClearance.htmlClass),
          Icon.caretDown,
          VdomAttr("data-toggle") := "dropdown",
          ^.id:="something",
          ^.className := "nav-link dropdown-toggle",
          aria.hasPopup := "true",
          aria.expanded := "false",
          ^.className := "btn",
          ^.className := SPWidgetElementsCSS.button.htmlClass,
          ^.className := SPWidgetElementsCSS.clickable.htmlClass
        ),
        <.ul(
          contents.collect{
            case e => <.div(
              ^.className := SPWidgetElementsCSS.dropdownElement.htmlClass,
              e
            )
          }.toTagMod,
          ^.className := SPWidgetElementsCSS.dropDownList.htmlClass,
          ^.className := "dropdown-menu",
          aria.labelledBy := "something"
        )
      )
    )

  def dropdownElement(text: String, icon: VdomNode, onClick: Callback): VdomNode =
    <.li(
      ^.className := SPWidgetElementsCSS.dropdownElement.htmlClass,
      <.span(icon, ^.className := SPWidgetElementsCSS.textIconClearance.htmlClass),
      text,
      ^.onClick --> onClick
    )

  def dropdownElement(text: String, onClick: Callback): VdomNode =
    <.li(
      ^.className := SPWidgetElementsCSS.dropdownElement.htmlClass,
      text,
      ^.onClick --> onClick
    )

  def buttonGroup(contents: Seq[TagMod]): VdomElement =
    <.div(
      ^.className:= "form-inline",
      contents.toTagMod
    )

  object TextBox {
    case class Props( defaultText: String, onChange: String => Callback )
    case class State( text: String )

    class Backend($: BackendScope[Props, State]) {
      def render(p:Props,s: State) =
        <.span(
          ^.className := SPWidgetElementsCSS.defaultMargin.htmlClass,
          ^.className := "input-group",
          <.input(
            ^.className := SPWidgetElementsCSS.textBox.htmlClass,
            ^.className := "form-control",
            ^.placeholder := p.defaultText,
            ^.aria.describedBy := "basic-addon1",
            ^.onChange ==> onFilterTextChange(p)
          )
        )
      def onFilterTextChange(p:Props)(e: ReactEventFromInput): Callback =
        e.extract(_.target.value)(v => (p.onChange(v))) // TODO check if this works
    }

    private val component = ScalaComponent.builder[Props]("SPTextBox")
      .initialState(State("test"))
      .renderBackend[Backend]
      .build

    def apply(defaultText: String, onChange: String => Callback) =
      component(Props(defaultText, onChange))
  }


  import java.util.UUID
  import spgui.circuit._
  import scala.scalajs.js

  object DragoverZone {
    trait Rectangle extends js.Object {
      var left: Float = js.native
      var top: Float = js.native
      var width: Float = js.native
      var height: Float = js.native
    }

    case class Props(
      id: UUID, x: Float, y: Float, w: Float, h: Float
    )

    case class State(target: UUID = null, isActive: Boolean = false)

    import diode.ModelRO
    class Backend($: BackendScope[Props, State]) {
      SPGUICircuit.subscribe(SPGUICircuit.zoomRW(myM => myM)((m,v) => v))(m =>
        $.modState(s =>
          State(
            target = m.value.draggingState.target,
            isActive = m.value.draggingState.dragging
          )
        ).runNow()
      )

      def render(p:Props, s:State) =
        <.span(
          ^.style := {
            var rect =  (js.Object()).asInstanceOf[Rectangle]
            rect.left = p.x
            rect.top = p.y
            rect.height = p.h
            rect.width = p.w
            rect
          },
          ^.className := spgui.widgets.sopmaker.SopMakerCSS.dropZone.htmlClass,
          {if(!s.isActive) ^.className := spgui.widgets.sopmaker.SopMakerCSS.disableDropZone.htmlClass
          else ""},
          {if(s.target == p.id)
            ^.className := spgui.widgets.sopmaker.SopMakerCSS.blue.htmlClass
          else ""},

          ^.onMouseOver --> handleMouseOver( p.id ),
          ^.onMouseLeave --> handleMouseLeave( p.id )
        )

      def handleMouseOver(id: UUID)= Callback{
        println("moused over " + id.toString)
        SPGUICircuit.dispatch(SetDraggingTarget(id))
      }

      def handleMouseLeave(id: UUID): Callback = Callback{
          SPGUICircuit.dispatch(UnsetDraggingTarget(id))
      }
    }

    private val component = ScalaComponent.builder[Props]("SPDragZone")
      .initialState(State())
      .renderBackend[Backend]
      .build

    def apply(id: UUID, x: Float, y: Float, w: Float, h: Float): VdomNode =
      component(Props(id, x, y, w, h))
  }
}

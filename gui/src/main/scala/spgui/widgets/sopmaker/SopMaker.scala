package spgui.widgets.sopmaker

import java.util.UUID
import japgolly.scalajs.react._

//import japgolly.scalajs.react.vdom.all.{ a, h1, h2, href, div, className, onClick, br, key }
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.all.svg
//import paths.mid.Bezier
//import paths.mid.Rectangle

import spgui.components.DragAndDrop.{ OnDragMod, OnDropMod, DataOnDrag, OnDataDrop }

import spgui.communication._
import sp.domain._
import sp.messages._
import sp.messages.Pickles._
import scalacss.ScalaCssReact._

trait RenderNode {
  val w: Float
  val h: Float
}

trait RenderGroup extends RenderNode {
  val children: List[RenderNode]
}

case class RenderParallel(w: Float, h:Float, children: List[RenderNode]) extends RenderGroup
case class RenderAlternative(w: Float, h:Float, children: List[RenderNode]) extends RenderGroup
case class RenderArbitrary(w: Float, h:Float, children: List[RenderNode]) extends RenderGroup
case class RenderSometimeSequence(w: Float, h:Float, children: List[RenderNode]) extends RenderGroup
case class RenderOther(w: Float, h:Float, children: List[RenderNode]) extends RenderGroup
case class RenderHierarchy(w:Float, h:Float, sop: Hierarchy) extends RenderNode

case class RenderSequence(w: Float, h:Float, children: List[RenderSequenceElement]) extends RenderGroup
case class RenderSequenceElement(w: Float, h:Float, self: RenderNode) extends RenderNode

case class Pos(x: Float, y: Float)

object SopMakerWidget {
  case class State(drag: String, drop: String, sop: List[String])

  private class Backend($: BackendScope[Unit, State]) {
    /*
    val eventHandler = BackendCommunication.getMessageObserver(
      mess => {
        println("[SopMaker] Got event " + mess.toString)
      },
      "events"
    )
     */
    
    def handleDrag(drag: String)(e: ReactDragEventFromInput): Callback = {
      Callback({
        e.dataTransfer.setData("json", drag)
      })
    }

    def handleDrop(drop: String)(e: ReactDragEvent): Callback = {
      val drag = e.dataTransfer.getData("json")
      Callback.log("Dropping " + drag + " onto " + drop)
    }

    def op(opname: String, x: Float, y: Float) =
      
    <.span(
        // extreme stylesheeting
        ^.className := (
          new SopMakerCSS.Position(
            x.toInt,
            y.toInt
          )
        ).position.htmlClass,
        SopMakerCSS.sopComponent,

        OnDragMod(handleDrag(opname)),
        OnDropMod(handleDrop(opname)),
        svg.svg(
          svg.width := 40,
          svg.height := 40,
          svg.rect(
            svg.x:=0,
            svg.y:=0,
            svg.width:=40,
            svg.height:=40,
            svg.rx:=5, svg.ry:=5,
            svg.fill := "white",
            svg.stroke:="black",
            svg.strokeWidth:=1
          ),
          svg.text(
            svg.x:="50%",
            svg.y:="50%",
            svg.textAnchor:="middle",
            svg.dy:=".3em", opname
          )
        )
      )

    val gridSize = 100

    var ops = List(
      Operation("op1"),
      Operation("op2"),
      Operation("op3"),
      Operation("op4"),
      Operation("op5"),
      Operation("op6"),
      Operation("op7"),
      Operation("op8"),
      Operation("op9"),
      Operation("op10"),
      Operation("op11"),
      Operation("test")
    )
    val idm = ops.map(o=>o.id -> o).toMap

    def render(state: State) = {

      val fakeSop = Sequence(List(
        Sequence(
          List(SOP(ops(7)), SOP(ops(7)), SOP(ops(8)))),
        Parallel(
          List(
            SOP(ops(0)),
            SOP(ops(1)),
            SOP(ops(2)),
            Parallel(
              List(SOP(ops(11)) ,SOP(ops(11)))
            )
          )
        ),
        Parallel(
          List( SOP(ops(4)), SOP(ops(5)), SOP(ops(6)),
            Sequence(
              List(SOP(ops(7)), SOP(ops(7)), SOP(ops(8))))
          )),
        Sequence(
          List(SOP(ops(7)), SOP(ops(8)))),
        Parallel(
          List( SOP(ops(9)), SOP(ops(10)) ))
      ))

      println(traverseTree(fakeSop))

      <.div(
        <.h2("Insert sop here:"),
        OnDataDrop(string => Callback.log("Received data: " + string)),
        <.br(),
        getRenderTree(traverseTree(fakeSop), 2*gridSize, 2*gridSize)
      )
    }
    
    def getRenderTree(node: RenderNode, xOffset: Float, yOffset: Float): TagMod = {
     // println(node)
      node match {
        case n: RenderParallel => {
          <.div("parallel",
            (for{c <- (0 to n.children.length-1)} yield
            {getRenderTree(n.children(c), (xOffset + (c + 0.5f - n.w/2)*gridSize), yOffset)}).toTagMod
          )
        }
        case n: RenderSequence => <.div("sequence",
           getRenderSequence(n, xOffset, yOffset)
        )
        case n: RenderHierarchy => {
          val opname = idm.get(n.sop.operation).map(_.name).getOrElse("[unknown op]")
          op(opname, xOffset, yOffset)
        }
        case _ => <.div("shuold not happen right now")
      }
    }

    def getRenderSequence(seq: RenderSequence, xOffset: Float, yOffset: Float): TagMod = {
      var h = yOffset
      seq.children.collect{case q: RenderSequenceElement => {
        h += q.h * gridSize
        <.div("sequence element",
          getRenderTree( q.self, xOffset, h - q.h * gridSize )
        )
      }}.toTagMod
    }

    def traverseTree(sop: SOP): RenderNode = {
      sop match {
        case s: Parallel => RenderParallel(
          w = getTreeWidth(s),
          h = getTreeHeight(s),
          children = sop.sop.collect{case e => traverseTree(e)}
        )
        case s: Sequence => traverseSequence(s.sop)
        case s: Hierarchy => RenderHierarchy(1,1, s)
      }
    }

    def traverseSequence(s: List[SOP]): RenderSequence = {
      if(!s.isEmpty) RenderSequence(
        h = 1,
        w = 1,
        children = s.collect{case e: SOP => {
          RenderSequenceElement(
            getTreeWidth(e),
            getTreeHeight(e),
            traverseTree(e)
          )
        }}
      ) else null
    }

    def getTreeWidth(sop: SOP): Float = {
      sop match {
        // groups are as wide as the sum of all children widths
        case s: Parallel => s.sop.map(e => getTreeWidth(e)).foldLeft(0f)(_ + _)
        case s: Sequence => { // sequences are as wide as their widest elements
          if(s.sop.isEmpty) 0
          else math.max(getTreeWidth(s.sop.head), getTreeWidth(Sequence(s.sop.tail)))
        }
        case s: Hierarchy => 1
      }
    }

    def getTreeHeight(sop: SOP): Float = {
      sop match  {
        case s: Parallel => {
          if(s.sop.isEmpty) 0
          else math.max(getTreeHeight(s.sop.head), getTreeHeight(Parallel(s.sop.tail)))
        }
        case s: Sequence => {
          s.sop.map(e => getTreeHeight(e)).foldLeft(0f)(_ + _)
        }
        case s:Hierarchy => 1
        case _ => {println("woops"); -1}
      }
   }

    def onUnmount() = Callback {
      println("Unmounting sopmaker")
    }
  }

  private val component = ScalaComponent.builder[Unit]("SopMakerWidget")
    .initialState(State(drag = "", drop = "", sop = List()))
    .renderBackend[Backend]
    .componentWillUnmount(_.backend.onUnmount())
    .build

  def apply() = spgui.SPWidget(spwb => component())
}

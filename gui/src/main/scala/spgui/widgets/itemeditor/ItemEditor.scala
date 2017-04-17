package spgui.widgets.itemeditor

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._
import scalajs.js
import js.Dynamic.{ literal => l }
import js.JSON

import spgui.SPWidgetBase

import spgui.communication._
import sp.domain._
//import sp.messages._
import sp.messages.Pickles._

sealed trait API_ItemEditorService
object API_ItemEditorService {
  case class Hello() extends API_ItemEditorService
  case class RequestSampleItem() extends API_ItemEditorService
  case class SampleItem(operationSPV: SPValue) extends API_ItemEditorService
  case class Item(item: SPValue) extends API_ItemEditorService
}

object ItemEditor {

  private val jsonEditorOptions = JSONEditorOptions(
    mode = "tree",
    schema = l(
      "title" -> "SP item",
      "type" -> "object",
      "properties" -> l(
        "isa" -> l(
          "enum" -> js.Array("HierarchyRoot", "Operation", "SOPSpec")
        ),
        "name" -> l(
          "type" -> "string"
        ),
        /*
        "age" -> l(
          "description" -> "Age in years",
          "type" -> "integer",
          "minimum" -> 0
        ),
         */
        "id" -> l(
          // dk if this is visible somewhere, but I think it would be nice
          "description" -> "UUID as string",
          "type" -> "string",
          // jsoneditor doesn't care, it seems, should facade onEditable
          "readOnly" -> true
        )
      ),
      "required" -> js.Array("isa", "name", "id")
    )
  )

  val jsonSample = l(
    "isa" -> "SomethingIncorrect",
    "name" -> "SampleJSON",
    "id" -> "this-should-be-an-uuid")

  // SPValue is None until we have received it from the backend
  case class State(spvOp: Option[SPValue] = None)

  class Backend($: BackendScope[SPWidgetBase, State]) {

    var jsonEditor: JSONEditor = null // initalized for real upon mounting

    def parseCommand(spm: SPMessage) = spm.getBodyAs[API_ItemEditorService]

    def handleCommand: API_ItemEditorService => Unit = {
      case API_ItemEditorService.Hello() =>
        println("ItemEditorWidget: Somebody said hi")
      case opSPV: API_ItemEditorService.SampleItem =>
        jsonEditor.set(JSON.parse(opSPV.operationSPV.toJson))
      case x =>
        println(s"THIS WAS NOT EXPECTED IN ItemEditorWidget: $x")
    }

    val messObs = BackendCommunication.getMessageObserver(
      spm => spm.getBodyAs[API_ItemEditorService] foreach handleCommand,
      "itemEditorAnswers"
    )

    // send a command to service once websocket is open
    // two ways to do it here
    import rx._
    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()
    /*
    val statusObs: rx.Obs = BackendCommunication.getWebSocketStatusCB(
      status => {
        println("websocketstatus is " + status + ", requesting sample item")
        requestSampleItem()
      },
      "services"
    )
     */
    // commented out one also works but can't kill it when done
    // TODO maybe make this in some less weird way
    val statusVar = BackendCommunication.getWebSocketStatus("services")
    statusVar.trigger{
      println("statusVar is " + statusVar.now)
      if(statusVar.now == true) {
        requestSampleItem()
        sendSampleItem()
        statusVar.kill()
      }
    }

    def sendCommand(cmd: API_ItemEditorService) = {
      val h = SPHeader(from = "ItemEditorWidget", to = "itemEditorService", reply = *("ItemEditorWidget"))
      val jsonMsg = SPMessage.make(h, cmd)
      BackendCommunication.publishMessage("services", jsonMsg)
      //BackendCommunication.ask(jsonMsg)
    }

    def requestSampleItem() = sendCommand(API_ItemEditorService.RequestSampleItem())

    def sendHello() = sendCommand(API_ItemEditorService.Hello())

    def changeJSON() = Callback{
      val someLit = l("mmm2um" -> "kkkkkk")
      jsonEditor.set(someLit)
    }

    def returnItem() = Callback{
      val spVal = fromJsonToSPValue(JSON.stringify(jsonEditor.get())).get
      sendCommand(API_ItemEditorService.Item(spVal))
    }

    val sampleJsonToSend = l("blå" -> "bär")
    val sampleItem = fromJsonToSPValue(JSON.stringify(sampleJsonToSend)).get
    def sendSampleItem() = sendCommand(API_ItemEditorService.Item(sampleItem))

    def render(spwb: SPWidgetBase, s: State) =
      <.div(<.button("Save", ^.onClick --> returnItem()))
  }

  private val component = ReactComponentB[SPWidgetBase]("ItemEditor")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(dcb =>
      Callback(dcb.backend.jsonEditor = JSONEditor(dcb.getDOMNode, jsonEditorOptions))
    )
    .build

  def apply() = (spwb: SPWidgetBase) => component(spwb)
}

/*
// how to use a case class, convert it to SPValue, turn it to actual json, and how to get it back to case class
import sp.domain._
import sp.messages.Pickles.fromJsonToSPValue

case class SomeCC(isa: String, id: String)
val someCC = SomeCC(isa = "något", id = "någotAnnat")

val spValue = SPValue(someCC)
println(spValue) // pretty prints spv as json

val spValueAsJSDynamic = JSON.parse(spValue.toJson)
println(spValueAsJSDynamic) // prints [object Object]

val retrievedSPVal = fromJsonToSPValue(JSON.stringify(spValueAsJSDynamic)).get // fromJson.. returns a Try
  println(retrievedSPVal) // pretty prints spv as json

val retrievedCC = retrievedSPVal.getAs[SomeCC]("").get // getAs returns an Option
println(retrievedCC.id) // prints "någotAnnat"
 */


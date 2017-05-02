package sp.waitingroomservice

import sp.domain._
import sp.domain.Logic._
import sp.messages._
import sp.messages.Pickles._

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import scala.util.{ Try, Success, Failure }

import java.util.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.chrono.ChronoLocalDate

import scala.collection.mutable.ListBuffer

import sp.waitingroomservice.{API_PatientEvent => api}
import sp.waitingroomservice.{API_Patient => apiPatient}

class WaitingRoomDevice extends Actor with ActorLogging {
  import akka.cluster.pubsub._
  import DistributedPubSubMediator.{ Subscribe, Publish }
  val mediator = DistributedPubSub(context.system).mediator
  //mediator ! Subscribe("services", self)
  mediator ! Subscribe("spevents", self)
  mediator ! Subscribe("patient-event-topic", self)
  mediator ! Subscribe("waiting-room-service-topic", self)

  var localState: Map[String, apiPatient.Patient] = Map()
  var widgetStarted: Boolean = false

  /**
  Receives incoming messages on the AKKA-bus
  */
  def receive = {
    case x: String => handleRequests(x)
  }

  /**
  Handles the received message and sends it further
  */
  def handleRequests(x: String): Unit = {
    val mess = SPMessage.fromJson(x)
    matchRequests(mess)
  }

  /**
  Identifies the body of the SP-message and acts correspondingly
  */
  def matchRequests(mess: Try[SPMessage]) = {
    val header = SPHeader(from = "waitingRoomService")
    WaitingRoomComm.extractEvent(mess) map { case (h, b) =>
      b match {
        case api.State(state) => {
          localState = state
          if (widgetStarted) {
            publishOnAkka(header, api.State(state))
          }
        }
        case api.GetState() => {
          println("Got state request from " + h.from)
          widgetStarted = true
          publishOnAkka(header, api.State(localState))
        }
        case _ => println("Not expected")
      }
    }
  }

  /**
  Publishes a SPMessage on bus with header and body.
  */
  def publishOnAkka(header: SPHeader, body: api.StateEvent) {
    val toSend = WaitingRoomComm.makeMess(header, body)
    toSend match {
      case Success(v) =>
        mediator ! Publish("waiting-room-widget-topic", v) // Publishes on bus for widget to receive
      case Failure(e) =>
        println("Failed")
    }
  }

  }

  object WaitingRoomDevice {
    def props = Props(classOf[WaitingRoomDevice])
  }

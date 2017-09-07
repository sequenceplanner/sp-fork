package sp.erica

import akka.actor._
import sp.domain._
import sp.erica.{API_Patient => apiPatient, API_PatientEvent => api}

import scala.util.Try

class WidgetDevice extends Actor with ActorLogging {
  import akka.cluster.pubsub._
  import DistributedPubSubMediator.{Publish, Subscribe}
  val mediator = DistributedPubSub(context.system).mediator
  //mediator ! Subscribe("services", self)
  mediator ! Subscribe("spevents", self)
  mediator ! Subscribe("state-event-topic", self)
  mediator ! Subscribe("widget-event", self)

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
    val header = SPHeader(from = "widgetService")
    for {
      m <- mess
      h <- m.getHeaderAs[SPHeader]
      b <- m.getBodyAs[api.Event]
    } yield {
      b match {
        case api.State(state) => {
          if (localState != state){
            localState = state
            if (widgetStarted) {
              publishOnAkka(header, api.State(state))
            }
          }
        }
        case api.GetState => {
          widgetStarted = true
          publishOnAkka(header, api.State(localState))
        }
        case _ => log.warning("Unexpected SP-message in matchRequests: " + b)
      }
    }
  }

  /**
  Publishes a SPMessage on bus with header and body.
  */
  def publishOnAkka(header: SPHeader, body: api.Event) {
    val toSend = SPMessage.makeJson(header, body)
    mediator ! Publish("patient-cards-widget-topic", toSend)
  }

  }

  object WidgetDevice {
    def props = Props(classOf[WidgetDevice])
  }

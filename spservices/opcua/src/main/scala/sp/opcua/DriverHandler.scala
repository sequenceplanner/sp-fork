package sp.opcua

import java.util.UUID
import akka.actor._
import sp.domain.logic.{ActionParser, PropositionParser}
import sp.domain._
import sp.domain.Logic._
import scala.concurrent.Future
import akka.util._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Properties
import org.joda.time.DateTime
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Put, Subscribe, Publish }
import sp.messages._
import sp.messages.Pickles._
import scala.util.{Failure, Success, Try}

package APIVirtualDevice {
  sealed trait Requests
  // requests setup
  case class SetUpDeviceDriver(driver: Driver) extends Requests
  case class SetUpResource(resource: Resource) extends Requests

  sealed trait DriverStateMapper
  case class OneToOneMapper(thing: UUID, driverID: UUID, driverIdentifier: String) extends DriverStateMapper

  // requests command (gets a SPACK and when applied, SPDone (and indirectly a StateEvent))
  case class ResourceCommand(resource: UUID, stateRequest: Map[UUID, SPValue], timeout: Int = 0) extends Requests

  // requests from driver
  case class DriverStateChange(name: String, id: UUID, state: Map[String, SPValue], diff: Boolean = false) extends Requests
  case class DriverCommand(name: String, id: UUID, state: Map[String, SPValue]) extends Requests
  case class DriverCommandDone(requestID: UUID, result: Boolean) extends Requests

  // answers
  sealed trait Replies
  case class StateEvent(resource: String, id: UUID, state: Map[UUID, SPValue], diff: Boolean = false) extends Replies

  case class Resources(xs: List[Resource]) extends Replies
  case class Drivers(xs: List[Driver]) extends Replies
  case class NewResource(x: Resource) extends Replies
  case class RemovedResource(x: Resource) extends Replies
  case class NewDriver(x: Driver) extends Replies
  case class RemovedDriver(x: Driver) extends Replies

  case class Resource(name: String, id: UUID, stateMap: List[DriverStateMapper], setup: SPAttributes, sendOnlyDiffs: Boolean = false)
  case class Driver(name: String, id: UUID, driverType: String, setup: SPAttributes)


  object  attributes {
    val service = "virtualDevice"
  }
}
import sp.opcua.{APIVirtualDevice => vdapi}

object DriverHandler {
  def props = Props(classOf[DriverHandler])
}

class DriverHandler extends Actor {
  import context.dispatcher
  val mediator = DistributedPubSub(context.system).mediator
  mediator ! akka.cluster.pubsub.DistributedPubSubMediator.Subscribe("driverCommands", self)

  val opcUADriver = "OPCUA"

  def receive = {
    case "stop" =>
      context.children.foreach { child =>
        child ! "disconnect"
      }

    case x: String =>
      println(x)
      SPMessage.fromJson(x) match {
        case Success(mess) =>
          for {
            h <- mess.getHeaderAs[SPHeader]
            b <- mess.getBodyAs[vdapi.Requests]
          } yield {
            b match {
              case vdapi.SetUpDeviceDriver(d) if d.driverType == opcUADriver =>
                context.actorOf(OpcUARuntime.props(d.name, d.id, d.setup), d.id.toString())
              case _ =>
            }
          }
        case Failure(err) =>
      }
    case _ => sender ! APISP.SPError("Ill formed request")
  }
}

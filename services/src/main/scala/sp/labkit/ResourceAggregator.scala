package sp.labkit

import akka.actor._
import sp.domain.logic.{ActionParser, PropositionParser}
import org.json4s.JsonAST.{JValue,JBool,JInt,JString}
import org.json4s.DefaultFormats
import sp.domain._
import sp.domain.Logic._
import scala.concurrent.Future
import akka.util._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Properties
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Put, Subscribe, Publish }
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

import APIOPMaker._

object ResourceAggregator {
  def props() = Props(classOf[ResourceAggregator])
}

class ResourceAggregator extends Actor {
  implicit val timeout = Timeout(100 seconds)
  import context.dispatcher
  val mediator = DistributedPubSub(context.system).mediator

  val processResources = List("p1","p3","p4")

  var processes: Map[String, Map[String, Int]] = processResources.map((_,Map():Map[String,Int])).toMap

  var baseTime: Int = -1

  mediator ! Subscribe("ops", self)

  def updateIdle = {
    // update idle time and send again
    processResources.foreach { resource =>
      val now = org.joda.time.DateTime.now.getMillis().intValue() - baseTime
      val processTime = processes(resource).get("Process").getOrElse(0)
      val moveTime = processes(resource).get("move").getOrElse(0)
      val idleTime = now - processTime - moveTime
      val nm = processes(resource) + ("Idle" -> idleTime)
      val nt = nm + ("move" -> (moveTime - processTime))
      processes ++= Map(resource -> nm)
      mediator ! Publish("frontend", ResourcePies(processes))
    }
  }

  def receive = {
    case "UpdateIdle" =>
      updateIdle
      context.system.scheduler.scheduleOnce(Duration(100, TimeUnit.MILLISECONDS), self, "UpdateIdle")

    case OP(start: OPEvent, end: Option[OPEvent], attributes: SPAttributes) =>
      val t = start.name
      val name = start.id
      val resource = start.resource
      val started = end.isEmpty

      // start tracking times on first event
      if(baseTime == -1) {
        baseTime = org.joda.time.DateTime.now.getMillis().intValue()
        self ! "UpdateIdle"
      }

      if(started) {
        // update gantt view
        mediator ! Publish("frontend", OperationStarted(name, resource, "", t, start.time.toString))
      }
      else {
        // update gantt view
        mediator ! Publish("frontend", OperationFinished(name, resource, "", t, end.get.time.toString))

        // update pie charts
        if(processResources.contains(resource)) {
          if(name.contains("Process")) {
            val duration = (end.get.time.getMillis() - start.time.getMillis()).intValue()
            val nt = processes(resource).get("Process").getOrElse(0) + duration
            val nm = processes(resource) + ("Process" -> nt)
            processes ++= Map(resource -> nm)
          }
          if(name.contains("move")) {
            val duration = (end.get.time.getMillis() - start.time.getMillis()).intValue()
            val processTime = processes(resource).get("Process").getOrElse(0)
            val nt = processes(resource).get("move").getOrElse(0) + duration // subtract process from move
            val nm = processes(resource) + ("move" -> nt)
            processes ++= Map(resource -> nm)
          }
        }
      }

     case _ =>
   }

  def terminate(progress: ActorRef): Unit = {
    self ! PoisonPill
    progress ! PoisonPill
  }
}

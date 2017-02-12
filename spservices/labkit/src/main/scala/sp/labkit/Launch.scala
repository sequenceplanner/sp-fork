package sp.labkit

import akka.actor._


object Launch extends App {
  implicit val system = ActorSystem("SP")

  // Add root actors used in node here
  system.actorOf(OPMakerLabKit.props, "opMakerLabKit")
  system.actorOf(ProductAggregator.props, "ProductAggregator")
  system.actorOf(ResourceAggregator.props, "ResourceAggregator")

  scala.io.StdIn.readLine("Press ENTER to exit application.\n") match {
    case x =>
      system.terminate()
  }

}
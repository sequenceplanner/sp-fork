package sp.blockSorting

import sp.psl._
import akka.actor._
import sp.domain.Logic._
import sp.domain._
import sp.system._
import sp.system.messages._
import scala.collection.mutable.MutableList
import astar._
import scala.collection.mutable.ArrayBuffer


object BSservice extends SPService {

  val specification = SPAttributes(
    "service" -> SPAttributes(
      "group" -> "hide" // to organize in gui. maybe use "hide" to hide service in gui
    ),
    "command" -> KeyDefinition("String", List(), None),
    "Left" -> KeyDefinition("List[List[String]]", List(), None),
    "Right" -> KeyDefinition("List[List[String]]", List(), None),
    "Middle" -> KeyDefinition("List[List[Stting]]", List(), None)
  )
  val transformTuple = (   
    TransformValue("Left", _.getAs[List[List[String]]]("Left")),
    TransformValue("Right", _.getAs[List[List[String]]]("Right")),
    TransformValue("Middle", _.getAs[List[List[String]]]("Middle")),
    TransformValue("command", _.getAs[String]("command"))
    )

  val transformation = transformToList(transformTuple.productIterator.toList)
  def props(serviceHandler: ActorRef, eventHandler: ActorRef, operationController: String, RunnerService: String) = Props(classOf[BSservice], serviceHandler, eventHandler, operationController, RunnerService)

}
class BSservice(serviceHandler: ActorRef, eventHandler: ActorRef, operationController: String, RunnerService: String) extends Actor with ServiceSupport with TowerBuilder{
  var leftRaw : List[List[String]] = List()
  var rightRaw : List[List[String]] = List()
  var middleRaw : List[List[String]] = List()
  var startLeft = Array.fill[Byte](16)(0)
  var startRight = Array.fill[Byte](16)(0)
  var startMiddle = Array.fill[Byte](16)(0)
  var startRobotL : Byte = 0
  var startRobotR : Byte = 0
  var moves : List[Move] = List()
  var m : Int = 0

  eventHandler ! SubscribeToSSE(self)
  
  
  def receive = {
    case r@Request(service, attr, ids, reqID) => {
      println("main")
      
      val replyTo = sender()
      implicit val rnr = RequestNReply(r, replyTo)
      
 
      
      println(transform(BSservice.transformTuple._4))
      if(transform(BSservice.transformTuple._4) == "hej"){
        println("JAAAAAAAAAAAAAAAAAAAA")
        startLeft.foreach{k => println(k)}
       
        
        val (startLeftm,startRightm,startMiddlem,startRobotLm,startRobotRm) = updateStates(moves,m,startLeft,startRight,startMiddle,startRobotL,startRobotR)
        startLeft = startLeftm
        startRight = startRightm
        startMiddle = startMiddlem
        startRobotL = startRobotLm
        startRobotR = startRobotRm
        println(m)
        println("----------------------")
        
        startLeft.foreach{k => println(k)}
        
      }else if(transform(BSservice.transformTuple._4) == "setup"){
        
        leftRaw = transform(BSservice.transformTuple._1)
        rightRaw = transform(BSservice.transformTuple._2)
        middleRaw = transform(BSservice.transformTuple._3)
      
        val (left, right, middle) = GuiToOpt(leftRaw, rightRaw, middleRaw)
        startLeft = left
        startRight = right
        startMiddle = middle
      }else if(transform(BSservice.transformTuple._4) == "order"){
        leftRaw = transform(BSservice.transformTuple._1)
        rightRaw = transform(BSservice.transformTuple._2)
        middleRaw = transform(BSservice.transformTuple._3)
      
        val (left, right, middle) = GuiToOpt(leftRaw, rightRaw, middleRaw)
      
        
        
        serviceHandler ! Request(operationController,SPAttributes("command"->SPAttributes("commandType"->"stop")))
        //serviceHandler ! Request("RunnerService", SPAttributes("command"->"stop"))
       
        val desiredState = new BlockState(left, right, middle,0,0,0,null,null)
        
        println("start: " + startLeft(0) + " desired:" + left(0))
        
        val startState = new BlockState(startLeft, startRight, startMiddle,startRobotL,startRobotR,0,desiredState,ArrayBuffer[Move]())
        val movestemp = Astar.solver(startState)
        moves = movestemp.toList
        
        println(moves.size)
  
        val paraSOP = movesToSOP(moves, ids)
        val sopSpec = SOPSpec("Sequence", List(paraSOP._1))
        val upIds = sopSpec :: paraSOP._2 ++ ids
        val stations = Map("tower" -> sopSpec.id)
        
        m = 0
        
        serviceHandler ! Request("OrderHandler", SPAttributes(
        "order" -> SPAttributes(
        "id" -> ID.newID,
        "name" -> "hej",
        "stations" -> stations
          )
          ) ,upIds) 
        }   
      replyTo ! Response(List(), SPAttributes("tower" -> "hej"), rnr.req.service, rnr.req.reqID)
      
    }
    
    case error: SPError => println("BSservice got an error: $error")
    
   
  }
}



trait TowerBuilder extends TowerOperationTypes {

  def updateStates(moves: List[Move], m: Int, startLeft: Array[Byte], startRight : Array[Byte], startMiddle : Array[Byte], startRobotL: Byte, startRobotR : Byte) = {
    var sL = startLeft
    var sR = startRight
    var sM = startMiddle
    var srL = startRobotL
    var srR = startRobotR
    
    if(moves(m).isPicking == true){
        if(moves(m).usingMiddle == true){
          if(moves(m).usingLeftRobot == true){
            srL = moves(m).color
          }else{
            srR =  moves(m).color
          }
          startMiddle(moves(m).position) = 0
        } else { 
          if(moves(m).usingLeftRobot == true){
            srL = moves(m).color
            sL(moves(m).position) = 0
          }else{
            srR = moves(m).color
            sR(moves(m).position) = 0
          }
        }
      }else {
       if(moves(m).usingMiddle == true){
          if(moves(m).usingLeftRobot == true){
            sM(moves(m).position) = startRobotL
            srL = 0
          }else{
            sM(moves(m).position) = startRobotR
            srR = 0
          }
        } else { 
          if(moves(m).usingLeftRobot == true){
            srL = 0
            sL(moves(m).position) = moves(m).color
          }else{
            srR =  0
            sR(moves(m).position) = moves(m).color
          }
        }  
      }
    (sL, sR, sM, srL, srR)
  }
  
  def GuiToOpt(left: List[List[String]], right: List[List[String]], middle: List[List[String]]) = {
    val leftPlate = new Array[Byte](16)
    var rightPlate = new Array[Byte](16)
    val middlePlate = new Array[Byte](4)
    var j = 0
    left.foreach{i =>
      i.foreach{k =>
        leftPlate(j) = k.toByte
        j += 1
      }
    }
    j = 0
    right.foreach{i =>
      i.foreach{k =>
        rightPlate(j) = k.toByte 
        j += 1
      }
    }
    j = 0
    middle.foreach{i =>
      i.foreach{k =>
        middlePlate(j) = k.toByte 
        j += 1
      }
    }
    
    (leftPlate, rightPlate, middlePlate)
  }

  def movesToSOP(moves: List[Move],  ids: List[IDAble]) = {
    val nameMap = ids.map(x => x.name -> x).toMap   
    val operations = movesToOperations(moves,nameMap)
    val sequence = Sequence(operations.map(o => Hierarchy(o.id)):_*)
    
    (sequence, operations)
  }
  
  def movesToOperations(moves: List[Move], nameMap: Map[String,IDAble]) = {
    println(moves(0).toString)
    
    val operations = for { m <- moves
    } yield {
      var name = "placeBlock"
      var position = 110
      var robot = "R5"
      if(m.isPicking == true){
        name = "pickBlock"
      }
      if(m.usingMiddle == true){
        position += m.position + 1
        if(m.usingLeftRobot == true){
          robot = "R4"
        }
     } else if(m.usingLeftRobot == true){
        robot = "R4"
        if(m.position <= 7){
          position += 100 + m.position + 1
        } else {        
          position += 110 + m.position + 1
        }
      } else {  
        if(m.position <= 7){
          position += 120 + m.position + 1
        } else {
          position += 130 + m.position + 1
        }
      }
      val operation = makeOperationWithParameter(robot,name,"pos",position,nameMap)
      List(operation)
    }
    operations.flatten
  }
  
 // def operationsName(moves: List[Move]) moves.map(_.pos).mkString("_")
}

trait TowerOperationTypes {
  def makeOperationWithParameter(resource: String, ability: String, parameter: String, value: SPValue, nameMap: Map[String, IDAble]) = {
    val ab = nameMap(s"$resource.$ability")
    val p = nameMap(s"$resource.$ability.$parameter")
    val valueJson = if (value.isInstanceOf[SPAttributes]) "" else "_"+value.toJson
    Operation(s"O_$ability${resource}$valueJson", List(), attributes = SPAttributes("ability" -> AbilityStructure(ab.id, List(AbilityParameter(p.id, value)))))
  }

  def makeOperation(resource: String, ability: String, nameMap: Map[String, IDAble]) = {
    val ab = nameMap(s"$resource.$ability")
    Operation(s"O_$ability${resource}", List(), attributes = SPAttributes("ability" -> AbilityStructure(ab.id, List())))
  }


}


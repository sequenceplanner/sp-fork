/**
  * Created by alexa on 15/06/2017.
  */
package spgui.widgets.charts

// imports for scalajs react
import javax.xml.stream.events.{EndDocument, StartDocument}

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scalajs.js
import spgui.communication._
import spgui.googleAPI.{GoogleChart_Trait, GoogleVisualization, timeline}
import spgui.googleAPI.timeline.{OptionsTimeline, TimelineChart}
import spgui.widgets.charts.ChartTest.id

import js.Date

object Timeline {
  /*
   * TODO:
   *      1. Look over which State is needed for Timeline
   */
  // State
  case class State(zoom: String)

  // dummy id
  val id = "widget"
  /*
   * TODO:
   *      1. Let the Widget set the Options through PROPS!
   */
  // local variable to hold the options

  //val timeline_options = new OptionsTimeline(100, 100)




  // ---setupColumns---
  // Setup the columns for the google timeline charts
  // Argument:
  //          -None-
  // Result:
  //        result value: GoogleVisualization.DataTable
  // Description:
  //              creates a new DataTable and adds the column 
  //              to use in the timeline
  /*
  def setupColumns(): GoogleVisualization.DataTable = {
    // defines inner function
    def adder(d: GoogleVisualization.DataTable): GoogleVisualization.DataTable = {
      d.addColumn("string", "Timeline id")
      d.addColumn("string", "Timeline name")
      d.addColumn("date", "Start Date")
      d.addColumn("date", "End Date")
      d
    }
    adder(new GoogleVisualization.DataTable())
  }

  // atm mutable
  // FIX WHEN POSSIBLE
  val data = setupColumns()


*/


  // ---Backend Class---
  // Argument:
  //            BackendScope[ Props , State ]
  private class MyBackend($: BackendScope[Unit, State]) {



    // ---render method---
    // Argument:
    //          State
    // Creates a div with CSS from TimelineCSS.scala
    // The div should contain the Google Timeline Chart
    def render(s: State) = {
      <.div(
        ^.className := TimelineCSS.timelineStyle.htmlClass,
        ^.id := id,
        <.div(
          ^.id := id + "name"
        ),
        <.div(
          ^.id := id+"timeline"
        )
      )
    }

    /**** SETUP LIST OF THE DIFFERENT CHARTS *****/
    // get div element
    val timeline_element = js.Dynamic.global.document.getElementById(id+"timeline")
    // create a new TimelineChart with the div as argument
    val timeline_chart = new TimelineChart(timeline_element)
    // create the list
    val list_charts: List[GoogleChart_Trait] = List.apply(timeline_chart)



    println(list_charts.head.element)


    // Handles the updates of the Timeline cycles
    def handleUpdate(s: State) = ???

    // Handles stopevents from a cycle
    def handleStop(s: State) = ???

    // Handles start-events for a cycle
    def handleStart(s: State) = ???


  }

  // Create a value component of type:
  //                                  ReactComponent
  // Set the initial state
  // Set the render class for the backend
  // Builds
  private val component = ReactComponentB[Unit]("Timeline")
    .initialState(
      State(
        zoom = "default"
      )
    )
    .renderBackend[MyBackend]
    .build

  // defines apply
  def apply() = spgui.SPWidget(spwb => component())
}

package spgui.widgets

import java.time._ //ARTO: Använder wrappern https://github.com/scala-js/scala-js-java-time

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ReactDOM

import spgui.SPWidget
import spgui.widgets.css.{WidgetStyles => Styles}

import org.scalajs.dom.raw
import org.scalajs.dom

import scala.concurrent.duration._
import scala.scalajs.js

import scalacss.ScalaCssReact._
import scalacss.Defaults._


object ClockWidget {

  case class State(secondsElapsed: Long)

  private class Backend($: BackendScope[Unit, State]) {
    spgui.widgets.css.WidgetStyles.addToDocument()

    var interval: js.UndefOr[js.timers.SetIntervalHandle] =
      js.undefined

      def tick =
        $.modState(s => State(s.secondsElapsed + 1))

        def start = Callback {
          interval = js.timers.setInterval(1000)(tick.runNow())
        }

        def clear = Callback {
          interval foreach js.timers.clearInterval
          interval = js.undefined
        }

        def render(s: State) = {
          var currentTime = LocalTime.now()
          <.div(
            <.p()(
              Styles.clock,
              currentTime.getHour(),":",currentTime.getMinute()
            )
          )
        }

      }

      private val component = ReactComponentB[Unit]("clockComponent")
      .initialState(State(0))
      .renderBackend[Backend]
      .componentDidMount(_.backend.start)
      .componentWillUnmount(_.backend.clear)
      .build


      def apply() = spgui.SPWidget(spwb => component())
    }
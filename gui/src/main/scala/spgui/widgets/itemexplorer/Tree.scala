package spgui.widgets.itemexplorer

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._

import spgui.SPWidget
import spgui.components.Icon
import spgui.components.DragAndDrop.{ DataOnDrag, OnDataDrop }

// TODO: replace with SP API
// this is just a dummy to have something to work with
sealed abstract class Item {
  val name: String
  // will be an UUID
  // val id = util.Random.nextInt(2e9.toInt).toString
  val id: Int
}
case class Mapp(name: String, id: Int, childrenIds: List[Int]) extends Item
case class Spotify(name: String, id: Int, content: String) extends Item
case class Youtube(name: String, id: Int, content: String) extends Item

object TVButton {
  private def button(name: String, icon: ReactNode) =
    <.div(<.div(Style.icon, icon), name, <.div(Style.chevron, Icon.chevronRight))

  def apply(item: Item) = item match {
    case Mapp(name, _, _) => button(name, Icon.folder)
    case Spotify(name, _, _) => button(name, Icon.spotify)
    case Youtube(name, _, _) => button(name, Icon.youtube)
  }
}

object ItemContent {
  def apply(item: Item, allItems: List[Item]): ReactNode = item match {
    case Mapp(_, _, childrenIds) => TVColumn(allItems, childrenIds)
    case Spotify(_, _, content) => content
    case Youtube(_, _, content) => content
  }
}

case class TreeState(items: List[Item], rootLevelItemIds: List[Int])

object ListItems {
// expects rootdir as first element (needs to contain everything)
  val listItems = List(
    Youtube("Smör", 2, "mjölk"),
    Youtube("Ägg", 3, "kalcium"),
    Spotify("Kladd", 4, "Kladd"),
    Mapp("kaka", 5, List(2, 3)),
    Spotify("Äpplen", 6, "paj")
  )
  val rootLevelItemIds = List(4, 5, 6)
  def apply() = TreeState(listItems, rootLevelItemIds)
}

object Tree {
  def emptyMap() = Mapp("EmptyMap", util.Random.nextInt(1000000) + 10000, List())
  def newYT() = Youtube("NewYT", util.Random.nextInt(1000000) + 10000, "content of NewYT")

  class TreeBackend($: BackendScope[Unit, TreeState]) {
    def addItem(item: Item) = $.modState{s =>
      s.copy(items = s.items :+ item, rootLevelItemIds = s.rootLevelItemIds :+ item.id)
    }

    def render(s: TreeState) =
      <.div(
        <.button("add EmptyMap", ^.onClick --> addItem(emptyMap())),
        <.button("add Youtube", ^.onClick --> addItem(newYT())),
        TVColumn(s.items, s.rootLevelItemIds)
      )
  }

  private val component = ReactComponentB[Unit]("Tree")
    .initialState(ListItems())
    .renderBackend[TreeBackend]
    .build

  def apply() = SPWidget(spwb => component())
}

object TVColumn {
  case class Props(items: List[Item], itemIds: List[Int])
  case class State(selectedItemId: Int = -1)

  class TVColumnBackend($: BackendScope[Props, State]) {

    def setSelectedId(id: Int) =
      $.modState(s => s.copy(selectedItemId = if(s.selectedItemId == id) -1 else id))

    def onDrop(senderId: String, receiverId: String) =
      Callback.log(s"item of id $senderId dropped on item of id $receiverId")

    def render(p: Props, s: State) =
      <.div(
        <.ul(
          Style.ul,
          p.itemIds.map{id =>
            val item = p.items.find(_.id == id).get
            <.li(
              Style.li(item.id == s.selectedItemId),
              TVButton(item),
              DataOnDrag(item.id.toString),
              OnDataDrop(eventData => onDrop(eventData, item.id.toString)),
              ^.onClick --> setSelectedId(item.id)
            )
          }
        ),
        if(s.selectedItemId == -1) ""
        else ItemContent(p.items.find(_.id == s.selectedItemId).get, p.items)
      )
  }

  val component = ReactComponentB[Props]("TreeDummyList")
    .initialState(State())
    .renderBackend[TVColumnBackend]
    .build

  def apply(items: List[Item], itemIds: List[Int]): ReactElement = component(Props(items, itemIds))
}


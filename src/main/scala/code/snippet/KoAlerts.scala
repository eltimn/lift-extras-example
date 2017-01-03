package code
package snippet

import scala.xml.{NodeSeq, Null, Text}

import net.liftweb._
import common._
import http._
import http.js._
import http.js.JsCmds._
import http.js.JE._
import json._
import util.Helpers.toBoolean

import net.liftmodules.extras.{KoModule, LiftExtras}

/**
  * A snippet for displaying notices to be used with the KoAlerts.js module.
  */
object KoAlerts {
  import JsonDSL._

  lazy val koModule = KoModule("KoAlerts", "ko-alerts")

  def render(in: NodeSeq): NodeSeq = {

    val idsToListenFor: List[String] = S.attr("ids")
      .map(_.split(",").map(_.trim).toList)
      .openOr(Nil)

    def initData: JValue =
      ("titles" -> LiftExtras.titlesAsJValue) ~
      ("ids" -> idsToListenFor)

    val onLoad: JsCmd =
      koModule.init(initData) &
      LiftExtras.noticeConverter.vend.noticesToJsCmd

    S.appendJs(onLoad)

    in
  }
}

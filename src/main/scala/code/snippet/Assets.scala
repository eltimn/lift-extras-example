package code
package snippet

import net.liftweb.common._
import net.liftweb.http.LiftRules
import net.liftweb.json._
import net.liftweb.http.S
import net.liftweb.http.js._
import net.liftweb.util.Helpers._
import net.liftweb.util.Props

import JsCmds._

object Assets {
  type AssetMap = Map[String, String]

  private def assetMap(assetType: String): AssetMap = {
    if (Props.mode == Props.RunModes.Development)
      Map.empty
    else {
      (LiftRules
        .loadResourceAsString(s"/${assetType}.manifest.json")
        .flatMap { s => tryo(JsonParser.parse(s)) }
      ) match {
        case Full(jo: JObject) =>
          jo.values.mapValues(_.toString)
        case _ =>
          Map.empty
      }
    }
  }

  private val cssMap = assetMap("css")
  private val jsMap = assetMap("js")

  private def assetPath(map: AssetMap, src: String): String = {
    map.getOrElse(src, src)
  }

  def css = {
    "* [href]" #> S.attr("src").map(s => "/assets/css/" + assetPath(cssMap, s))
  }

  def js = {
    "* [src]" #> S.attr("src").map(s => "/assets/js/" + assetPath(jsMap, s))
  }
}

package bootstrap.liftweb

import code.config.Site

import scala.xml._

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.Run
import net.liftweb.util._
import Helpers._

import net.liftmodules.extras.{Gravatar, LiftExtras}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("code")

    // set the sitemap.
    LiftRules.setSiteMap(Site.siteMap)

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => Run("$('#ajax-spinner').removeClass('hide')"))

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => Run("$('#ajax-spinner').addClass('hide')"))

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Init Extras
    LiftExtras.init()
    LiftRules.addToPackages("net.liftmodules.extras")
    LiftExtras.errorTitle.default.set(Full(<em>Error!</em>))
    LiftExtras.warningTitle.default.set(Full(Text("Warning!")))
    LiftExtras.noticeTitle.default.set(Full(Text("Info!")))
    LiftExtras.successTitle.default.set(Full(Text("Success!")))
    LiftExtras.artifactName.default.set("extras-example-0.4.0")

    Gravatar.defaultImage.default.set("wavatar")

    // Security Rules
    val protocol = Props.get("http.protocol", "http")
    val appDomain = Props.get("app.domain", "localhost:3000")
    val defaultCsp = ContentSecurityPolicy(
      imageSources = List(
        ContentSourceRestriction.Self,
        ContentSourceRestriction.Host(s"${protocol}://www.gravatar.com"),
        ContentSourceRestriction.Scheme("data")
      ),
      fontSources = List(
        ContentSourceRestriction.Self,
        ContentSourceRestriction.Scheme("data")
      ),
      styleSources = List(
        ContentSourceRestriction.Self,
        ContentSourceRestriction.UnsafeInline // TODO: remove this
      ),
      scriptSources = List(
        ContentSourceRestriction.UnsafeEval, // Lift needs this
        ContentSourceRestriction.Self,
        ContentSourceRestriction.UnsafeInline // TODO: remove this. https://dev.ckeditor.com/ticket/8584
      ),
      connectSources = List(
        ContentSourceRestriction.Self
      ),
      mediaSources = List(
        ContentSourceRestriction.Self
      )
    )

    val csp =
      if (Props.devMode) {
        // for browserSync support
        defaultCsp.copy(connectSources = List(
          ContentSourceRestriction.Self,
          ContentSourceRestriction.Host(s"""ws://${appDomain}""")
        ))
      } else {
        defaultCsp
      }

    val httpRules =
      if (Props.devMode) {
        None
      } else {
        Some(HttpsRules.secure)
      }

    LiftRules.securityRules = () => SecurityRules(
      httpRules,
      Some(csp),
      enforceInOtherModes = true
    )
  }
}

package code

import code.servlet.HttpCacheFilter

import java.net.{InetAddress, InetSocketAddress}
import java.nio.file.{ Files, Paths }
import java.nio.file.attribute.FileTime
import java.util.EnumSet
import javax.servlet.DispatcherType

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.webapp.WebAppContext

import net.liftweb.common.Loggable
import net.liftweb.http.LiftFilter
import net.liftweb.util.{ LoggingAutoConfigurer, Props }

object WebApp extends Loggable {
  private val sessionTimeoutSeconds = 600

  def main(args: Array[String]): Unit = {
    LoggingAutoConfigurer().apply()

    logger.info("run.mode: " + Props.mode.toString)

    // context paths
    lazy val webappPath =
      if (Props.productionMode) {
        sys.props.getOrElse("prog.home", ".") + "/webapp"
      } else {
        "src/main/webapp"
      }

    lazy val assetsPath =
      if (Props.productionMode) {
        sys.props.getOrElse("prog.home", ".") + "/assets"
      } else {
        "target/frontend/dist"
      }

    logger.info("run.mode: " + Props.mode.toString)
    logger.trace("system environment: " + sys.env)
    logger.trace("system props: " + sys.props)
    logger.trace("liftweb props: " + Props.props)

    val handlers = new HandlerList()
    handlers.addHandler(assetsContext(assetsPath))
    handlers.addHandler(webappContext(webappPath))

    val port = sys.props.getOrElse("port", "8080").toInt
    val address =
      if (Props.devMode) {
        new InetSocketAddress(port)
      } else {
        // bind to localhost only
        new InetSocketAddress(InetAddress.getLoopbackAddress, port)
      }

    // start the server
    val server = new Server(address)
    server.setHandler(handlers)
    server.start()
    logger.info(s"Lift started on port $port")

    // browserSync support
    if (Props.devMode) {
      val filePath = Paths.get("target/bsync.reload")
      if (Files.exists(filePath)) {
        val fileTime = FileTime.fromMillis(System.currentTimeMillis)
        Files.setLastModifiedTime(filePath, fileTime)
      } else {
        Files.createFile(filePath)
      }
    }

    server.join()
  }

  private def initContext(context: WebAppContext): Unit = {
    // don't allow directory browsing
    context.setInitParameter(
      "org.eclipse.jetty.servlet.Default.dirAllowed",
      "false"
    )

    if (Props.devMode && (Props.get("os.name", "Unix").toLowerCase.startsWith("windows"))) {
      // On Windows don't let Jetty lock served files
      context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false")
    }
  }

  private def assetsContext(path: String): WebAppContext = {
    val context = new WebAppContext(path, "/assets")
    // context.setErrorHandler(createErrorHandler)
    initContext(context)

    // serve .gz files if available
    // context.setInitParameter("org.eclipse.jetty.servlet.Default.gzip", "true")

    // cache filter
    if (Props.productionMode) {
      context.addFilter(
        classOf[HttpCacheFilter],
        "/*",
        EnumSet.of(DispatcherType.REQUEST)
      )
    }

    context
  }

  private def webappContext(path: String): WebAppContext = {
    val context = new WebAppContext(path, "/")
    // context.setErrorHandler(createErrorHandler)
    initContext(context)

    // add filters
    context.addFilter(
      classOf[LiftFilter],
      "/*",
      EnumSet.allOf(classOf[DispatcherType])
    )

    context
      .getSessionHandler
      .getSessionManager
      .setMaxInactiveInterval(sessionTimeoutSeconds)

    context
  }
}

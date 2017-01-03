package code.servlet

import javax.servlet._
import javax.servlet.http.HttpServletResponse

import net.liftweb.util._
import Helpers._

/**
  * Add caching headers to static resources
  */
class HttpCacheFilter extends Filter {

  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain): Unit = {
    res.asInstanceOf[HttpServletResponse].setHeader("Cache-Control", "public")
    res.asInstanceOf[HttpServletResponse].setHeader("Pragma", null)
    res.asInstanceOf[HttpServletResponse]
      .setDateHeader("Expires", (millis + 180.days))

    chain.doFilter(req, res)
  }

  def init(config: FilterConfig): Unit = {}

  def destroy(): Unit = {}

}

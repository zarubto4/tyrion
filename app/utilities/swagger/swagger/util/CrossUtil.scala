package utilities.swagger.swagger.util;

import play.routes.compiler.Parameter

object CrossUtil {
  def getParameterDefaultField(parameter: Parameter): String = {
    parameter.default.getOrElse("")
  }
}

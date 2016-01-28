
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Thu Jan 28 16:10:22 CET 2016


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}

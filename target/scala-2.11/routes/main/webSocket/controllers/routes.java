
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Wed Jan 27 15:12:26 CET 2016

package webSocket.controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final webSocket.controllers.ReverseOutsideCommunicationPackageController OutsideCommunicationPackageController = new webSocket.controllers.ReverseOutsideCommunicationPackageController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final webSocket.controllers.javascript.ReverseOutsideCommunicationPackageController OutsideCommunicationPackageController = new webSocket.controllers.javascript.ReverseOutsideCommunicationPackageController(RoutesPrefix.byNamePrefix());
  }

}


// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Mon Jan 25 23:42:15 CET 2016

package webSocket.controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final webSocket.controllers.ReverseOutsideCommunicationPackageController OutsideCommunicationPackageController = new webSocket.controllers.ReverseOutsideCommunicationPackageController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final webSocket.controllers.javascript.ReverseOutsideCommunicationPackageController OutsideCommunicationPackageController = new webSocket.controllers.javascript.ReverseOutsideCommunicationPackageController(RoutesPrefix.byNamePrefix());
  }

}

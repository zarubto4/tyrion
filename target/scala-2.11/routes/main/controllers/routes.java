
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Tue Jan 26 17:01:35 CET 2016

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseOverFlowController OverFlowController = new controllers.ReverseOverFlowController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseCompilationLibrariesController CompilationLibrariesController = new controllers.ReverseCompilationLibrariesController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseSecurityController SecurityController = new controllers.ReverseSecurityController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReversePermissionController PermissionController = new controllers.ReversePermissionController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReversePersonCreateController PersonCreateController = new controllers.ReversePersonCreateController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseProgramingPackageController ProgramingPackageController = new controllers.ReverseProgramingPackageController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseOverFlowController OverFlowController = new controllers.javascript.ReverseOverFlowController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseCompilationLibrariesController CompilationLibrariesController = new controllers.javascript.ReverseCompilationLibrariesController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseSecurityController SecurityController = new controllers.javascript.ReverseSecurityController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReversePermissionController PermissionController = new controllers.javascript.ReversePermissionController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReversePersonCreateController PersonCreateController = new controllers.javascript.ReversePersonCreateController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseProgramingPackageController ProgramingPackageController = new controllers.javascript.ReverseProgramingPackageController(RoutesPrefix.byNamePrefix());
  }

}


// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Thu Feb 25 18:09:35 CET 2016

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:8
  SecurityController_4: javax.inject.Provider[controllers.SecurityController],
  // @LINE:11
  WikyController_8: javax.inject.Provider[controllers.WikyController],
  // @LINE:21
  WebSocketController_Incoming_0: javax.inject.Provider[controllers.WebSocketController_Incoming],
  // @LINE:45
  PermissionController_1: javax.inject.Provider[controllers.PermissionController],
  // @LINE:68
  PersonController_2: javax.inject.Provider[controllers.PersonController],
  // @LINE:87
  OverFlowController_10: javax.inject.Provider[controllers.OverFlowController],
  // @LINE:129
  ProgramingPackageController_5: javax.inject.Provider[controllers.ProgramingPackageController],
  // @LINE:198
  CompilationLibrariesController_7: javax.inject.Provider[controllers.CompilationLibrariesController],
  // @LINE:290
  GridController_6: javax.inject.Provider[controllers.GridController],
  // @LINE:326
  ApiHelpController_3: javax.inject.Provider[utilities.swagger.ApiHelpController],
  // @LINE:332
  Assets_9: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:8
    SecurityController_4: javax.inject.Provider[controllers.SecurityController],
    // @LINE:11
    WikyController_8: javax.inject.Provider[controllers.WikyController],
    // @LINE:21
    WebSocketController_Incoming_0: javax.inject.Provider[controllers.WebSocketController_Incoming],
    // @LINE:45
    PermissionController_1: javax.inject.Provider[controllers.PermissionController],
    // @LINE:68
    PersonController_2: javax.inject.Provider[controllers.PersonController],
    // @LINE:87
    OverFlowController_10: javax.inject.Provider[controllers.OverFlowController],
    // @LINE:129
    ProgramingPackageController_5: javax.inject.Provider[controllers.ProgramingPackageController],
    // @LINE:198
    CompilationLibrariesController_7: javax.inject.Provider[controllers.CompilationLibrariesController],
    // @LINE:290
    GridController_6: javax.inject.Provider[controllers.GridController],
    // @LINE:326
    ApiHelpController_3: javax.inject.Provider[utilities.swagger.ApiHelpController],
    // @LINE:332
    Assets_9: controllers.Assets
  ) = this(errorHandler, SecurityController_4, WikyController_8, WebSocketController_Incoming_0, PermissionController_1, PersonController_2, OverFlowController_10, ProgramingPackageController_5, CompilationLibrariesController_7, GridController_6, ApiHelpController_3, Assets_9, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, SecurityController_4, WikyController_8, WebSocketController_Incoming_0, PermissionController_1, PersonController_2, OverFlowController_10, ProgramingPackageController_5, CompilationLibrariesController_7, GridController_6, ApiHelpController_3, Assets_9, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """@controllers.SecurityController@.index"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test1""", """@controllers.WikyController@.test1()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test2""", """@controllers.WikyController@.test2()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test3""", """@controllers.WikyController@.test3()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test4""", """@controllers.WikyController@.test4(projectId:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test5""", """@controllers.WikyController@.test5(projectId:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test6""", """@controllers.WikyController@.test6()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/homer/$id<[^/]+>""", """@controllers.WebSocketController_Incoming@.homer_connection(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/mobile/$id<[^/]+>""", """@controllers.WebSocketController_Incoming@.mobile_connection(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/webSocketStats""", """@controllers.WebSocketController_Incoming@.getWebSocketStats()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/sendTo/$id<[^/]+>""", """@controllers.WebSocketController_Incoming@.sendTo(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/permission/login""", """@controllers.SecurityController@.login()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/permission/logout""", """@controllers.SecurityController@.logout"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/facebook""", """@controllers.SecurityController@.Facebook(returnLink:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/twitter""", """@controllers.SecurityController@.Twitter(returnLink:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/github""", """@controllers.SecurityController@.GitHub(returnLink:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/vkontakte""", """@controllers.SecurityController@.Vkontakte(returnLink:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/person""", """@controllers.SecurityController@.getPersonByToken()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/facebook/$url<.+>""", """@controllers.SecurityController@.GET_facebook_oauth(url:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/github/$url<.+>""", """@controllers.SecurityController@.GET_github_oauth(url:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/permission/person/add""", """@controllers.PermissionController@.add_Permission_Person(person_id:String, permission_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/permission/person/remove""", """@controllers.PermissionController@.remove_Permission_Person(person_id:String, permission_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/permission""", """@controllers.PermissionController@.get_Permission_All()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role/permission/add""", """@controllers.PermissionController@.add_Permission_to_Role(permission_id:String, role_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role/permission""", """@controllers.PermissionController@.get_Permission_in_Group(role_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role/permission/remove""", """@controllers.PermissionController@.remove_Permission_from_Role(permission_id:String, role_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role""", """@controllers.PermissionController@.new_Role()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role""", """@controllers.PermissionController@.delete_Role(role_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role/person/add""", """@controllers.PermissionController@.add_Role_Person(person_id:String, role_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role/person/remove""", """@controllers.PermissionController@.remove_Role_Person(person_id:String, role_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/role/all""", """@controllers.PermissionController@.get_Role_All()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """secure/person/system_acces""", """@controllers.PermissionController@.get_System_Acces(person_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/developer""", """@controllers.PersonController@.developerRegistration()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonController@.registred_Person()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonController@.edit_Person_Information()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonController@.getPerson(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonController@.deletePerson(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """emailPersonAuthentication/""", """@controllers.PersonController@.email_Person_authentitaction(mail:String, authToken:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post""", """@controllers.OverFlowController@.newPost()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/$id<[^/]+>""", """@controllers.OverFlowController@.getPost(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/$id<[^/]+>""", """@controllers.OverFlowController@.deletePost(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/$id<[^/]+>""", """@controllers.OverFlowController@.editPost(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/postFilter""", """@controllers.OverFlowController@.getPostByFilter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/linkedAnswers/$id<[^/]+>""", """@controllers.OverFlowController@.getPostLinkedAnswers(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/hashTags/$id<[^/]+>""", """@controllers.OverFlowController@.hashTagsListOnPost(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/comments/$id<[^/]+>""", """@controllers.OverFlowController@.commentsListOnPost(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/answers/$id<[^/]+>""", """@controllers.OverFlowController@.answereListOnPost(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/textOfPost/$id<[^/]+>""", """@controllers.OverFlowController@.textOfPost(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/typeOfPost""", """@controllers.OverFlowController@.newTypeOfPost()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/typeOfPost""", """@controllers.OverFlowController@.getTypeOfPost()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/typeOfConfirm""", """@controllers.OverFlowController@.newTypeOfConfirms()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/typeOfConfirm""", """@controllers.OverFlowController@.getTypeOfConfirms()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/typeOfConfirm/$conf<[^/]+>/$pst<[^/]+>""", """@controllers.OverFlowController@.putTypeOfConfirmToPost(conf:String, pst:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/comment""", """@controllers.OverFlowController@.addComment()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/comment/$id<[^/]+>""", """@controllers.OverFlowController@.updateComment(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/comment/$id<[^/]+>""", """@controllers.OverFlowController@.deletePost(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/answer""", """@controllers.OverFlowController@.addAnswer()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/answer/$id<[^/]+>""", """@controllers.OverFlowController@.updateComment(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/answer/$id<[^/]+>""", """@controllers.OverFlowController@.deletePost(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/likePlus/$id<[^/]+>""", """@controllers.OverFlowController@.likePlus(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/likeMinus/$id<[^/]+>""", """@controllers.OverFlowController@.likeMinus(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/link""", """@controllers.OverFlowController@.linkWithPreviousAnswer()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/link/$id<[^/]+>""", """@controllers.OverFlowController@.unlinkWithPreviousAnswer(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/removeLink""", """@controllers.OverFlowController@.removeHashTag()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/hashTag""", """@controllers.OverFlowController@.addHashTag()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/removeHashTag""", """@controllers.OverFlowController@.removeHashTag()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project""", """@controllers.ProgramingPackageController@.postNewProject()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/$id<[^/]+>""", """@controllers.ProgramingPackageController@.updateProject(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProject(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project""", """@controllers.ProgramingPackageController@.getProjectsByUserAccount()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/$id<[^/]+>""", """@controllers.ProgramingPackageController@.deleteProject(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/shareProject/$id<[^/]+>""", """@controllers.ProgramingPackageController@.shareProjectWithUsers(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/unshareProject/$id<[^/]+>""", """@controllers.ProgramingPackageController@.unshareProjectWithUsers(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/b_programs/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getAll_b_Programs(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/c_programs/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getAll_c_Programs(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/m_projects/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getAll_m_Projects(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/homerList/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramhomerList(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/owners/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProjectOwners(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer""", """@controllers.ProgramingPackageController@.newHomer()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/$id<[^/]+>""", """@controllers.ProgramingPackageController@.removeHomer(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getHomer(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer""", """@controllers.ProgramingPackageController@.getAllHomers()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/getAllConnectedHomers/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getConnectedHomers(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/connectHomerWithProject""", """@controllers.ProgramingPackageController@.connectHomerWithProject()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/disconnectHomerWithProject""", """@controllers.ProgramingPackageController@.disconnectHomerWithProject()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_program""", """@controllers.ProgramingPackageController@.postNewBProgram()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_program/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgram(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_program/$id<[^/]+>""", """@controllers.ProgramingPackageController@.editProgram(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_program/update/$id<[^/]+>""", """@controllers.ProgramingPackageController@.update_b_program(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_program/$id<[^/]+>""", """@controllers.ProgramingPackageController@.remove_b_Program(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_programInJson/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramInString(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_program/upload/$id<[^/]+>/$ver<[^/]+>""", """@controllers.ProgramingPackageController@.uploadProgramToHomer_Immediately(id:String, ver:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/b_program/uploadToCloud/$id<[^/]+>/$ver<[^/]+>""", """@controllers.ProgramingPackageController@.uploadProgramToCloud(id:String, ver:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/listOfUploadedHomers/$id<[^/]+>""", """@controllers.ProgramingPackageController@.listOfUploadedHomers(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/listOfHomersWaitingForUpload/$id<[^/]+>""", """@controllers.ProgramingPackageController@.listOfHomersWaitingForUpload(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/boards/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProjectsBoard(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock""", """@controllers.ProgramingPackageController@.newBlock()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$id<[^/]+>""", """@controllers.ProgramingPackageController@.updateOfBlock(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$id<[^/]+>""", """@controllers.ProgramingPackageController@.editBlock(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getBlockBlock(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/versions/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getBlockVersions(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/allPrevVersions/$id<[^/]+>""", """@controllers.ProgramingPackageController@.allPrevVersions(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/version/$id<[^/]+>""", """@controllers.ProgramingPackageController@.deleteBlockVersion(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/block/id""", """@controllers.ProgramingPackageController@.deleteBlock(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/typeOfBlock""", """@controllers.ProgramingPackageController@.newTypeOfBlock()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/typeOfBlock/filter""", """@controllers.ProgramingPackageController@.getByCategory()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/typeOfBlock/$id<[^/]+>""", """@controllers.ProgramingPackageController@.editTypeOfBlock(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/typeOfBlock""", """@controllers.ProgramingPackageController@.getAllTypeOfBlocks()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/typeOfBlock/$id<[^/]+>""", """@controllers.ProgramingPackageController@.deleteTypeOfBlock(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/$project_id<[^/]+>""", """@controllers.CompilationLibrariesController@.create_C_Program(project_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/$c_program_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_C_Program(c_program_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/project/$project_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_C_Program_All_from_Project(project_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/edit/$c_program_id<[^/]+>""", """@controllers.CompilationLibrariesController@.edit_C_Program_Description(c_program_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/update/$c_program_id<[^/]+>""", """@controllers.CompilationLibrariesController@.update_C_Program(c_program_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/c_program/$c_program_id<[^/]+>""", """@controllers.CompilationLibrariesController@.delete_C_Program(c_program_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/version/$c_program_id<[^/]+>/$version_id<[^/]+>""", """@controllers.CompilationLibrariesController@.delete_C_Program_Version(c_program_id:String, version_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/eclipse""", """@controllers.CompilationLibrariesController@.generateProjectForEclipse()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/upload/$c_program_id<[^/]+>/$board_id<[^/]+>""", """@controllers.CompilationLibrariesController@.uploadCompilationToBoard(c_program_id:String, board_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/binary/$board_id<[^/]+>""", """@controllers.CompilationLibrariesController@.uploadBinaryFileToBoard(board_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/project/board/$project_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_Boards_from_Project(project_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor""", """@controllers.CompilationLibrariesController@.new_Processor()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$processor_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_Processor(processor_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor""", """@controllers.CompilationLibrariesController@.get_Processor_All()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$processor_id<[^/]+>""", """@controllers.CompilationLibrariesController@.update_Processor(processor_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$processor_id<[^/]+>""", """@controllers.CompilationLibrariesController@.delete_Processor(processor_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/library/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.connectProcessorWithLibrary(processor_id:String, library_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroup/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.connectProcessorWithLibraryGroup(processor_id:String, library_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/library/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.disconnectProcessorWithLibrary(processor_id:String, library_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroup/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.disconnectProcessorWithLibraryGroup(processor_id:String, library_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroups/$processor_id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorLibraryGroups(processor_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/singleLibrary/$processor_id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorSingleLibraries(processor_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board""", """@controllers.CompilationLibrariesController@.new_Board()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/userDescription/$type_of_board_id<[^/]+>""", """@controllers.CompilationLibrariesController@.edit_Board_User_Description(type_of_board_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/filter""", """@controllers.CompilationLibrariesController@.get_Board_Filter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/$board_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_Board(board_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/deactivateBoard/$board_id<[^/]+>""", """@controllers.CompilationLibrariesController@.deactivate_Board(board_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/connect/$board_id<[^/]+>/$project_id<[^/]+>""", """@controllers.CompilationLibrariesController@.connect_Board_with_Project(board_id:String, project_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/disconnect/$board_id<[^/]+>/$project_id<[^/]+>""", """@controllers.CompilationLibrariesController@.disconnect_Board_from_Project(board_id:String, project_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/projects/$board_id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoardProjects(board_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.new_Producer()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.edit_Producer(producer_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/all""", """@controllers.CompilationLibrariesController@.get_Producers()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.get_Producer(producer_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/description""", """@controllers.CompilationLibrariesController@.get_Producer_Description(producer_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/typeOfBoards""", """@controllers.CompilationLibrariesController@.get_Producer_TypeOfBoards(producer_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.new_TypeOfBoard()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.edit_TypeOfBoard(type_of_board_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.delete_TypeOfBoard(type_of_board_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/all""", """@controllers.CompilationLibrariesController@.get_TypeOfBoard_all()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.get_TypeOfBoard(type_of_board_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/description""", """@controllers.CompilationLibrariesController@.get_TypeOfBoard_Description(type_of_board_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/boards""", """@controllers.CompilationLibrariesController@.getTypeOfBoardAllBoards(type_of_board_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.new_LibraryGroup()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$libraryGroup_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_LibraryGroup(libraryGroup_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$libraryGroup_id<[^/]+>""", """@controllers.CompilationLibrariesController@.delete_LibraryGroup(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/filter""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Filter()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$libraryGroup_id<[^/]+>""", """@controllers.CompilationLibrariesController@.editLibraryGroup(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/generalDescription/$libraryGroup_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Description(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/processors/$libraryGroup_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Processors(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/libraries/$libraryGroup_id<[^/]+>/$version_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Libraries(libraryGroup_id:String, version_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/version/$version_id<[^/]+>""", """@controllers.CompilationLibrariesController@.new_LibraryGroup_Version(version_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/versions/$version_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Version(version_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/upload/$libraryGroup_id<[^/]+>/$version_id<[^/]+>""", """@controllers.CompilationLibrariesController@.upload_Library_To_LibraryGroup(libraryGroup_id:String, version_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.new_SingleLibrary()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/version/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.new_SingleLibrary_Version(library_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/versions/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_SingleLibrary_Versions(library_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/filter""", """@controllers.CompilationLibrariesController@.get_SingleLibrary_Filter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_SingleLibrary(library_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.edit_SingleLibrary(library_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.delete_SingleLibrary(library_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/upload$library_id<[^/]+>/$version_id<[^/]+>""", """@controllers.CompilationLibrariesController@.upload_SingleLibrary_Version(library_id:String, version_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """file/listOfFiles/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Version_Libraries(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """file/fileRecord/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.fileRecord(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project""", """@controllers.GridController@.new_M_Project(project_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/person""", """@controllers.GridController@.get_M_Projects_ByLoggedPerson()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/$m_project_id<[^/]+>""", """@controllers.GridController@.get_M_Project(m_project_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/$m_project_id<[^/]+>""", """@controllers.GridController@.edit_M_Project(m_project_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/$m_project_id<[^/]+>""", """@controllers.GridController@.remove_M_Project(m_project_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/project/$project_id<[^/]+>""", """@controllers.GridController@.get_M_Projects_from_GlobalProject(project_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program""", """@controllers.GridController@.new_M_Program()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/app/token/$qr_token<[^/]+>""", """@controllers.GridController@.get_M_Program_byQR_Token_forMobile(qr_token:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/app/m_programs""", """@controllers.GridController@.get_M_Program_all_forMobile()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/$m_progrm_id<[^/]+>""", """@controllers.GridController@.get_M_Program(m_progrm_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/$m_progrm_id<[^/]+>""", """@controllers.GridController@.edit_M_Program(m_progrm_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/$m_progrm_id<[^/]+>""", """@controllers.GridController@.remove_M_Program(m_progrm_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type""", """@controllers.GridController@.new_Screen_Size_Type()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/all""", """@controllers.GridController@.get_Screen_Size_Type_Combination()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/$screen_size_type_id<[^/]+>""", """@controllers.GridController@.get_Screen_Size_Type(screen_size_type_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/$screen_size_type_id<[^/]+>""", """@controllers.GridController@.edit_Screen_Size_Type(screen_size_type_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/$screen_size_type_id<[^/]+>""", """@controllers.GridController@.remove_Screen_Size_Type(screen_size_type_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api-docs""", """@utilities.swagger.ApiHelpController@.getResources"""),
    ("""OPTIONS""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """$all<.+>""", """@controllers.SecurityController@.optionLink(all:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""", """controllers.Assets.at(path:String = "/public", file:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:8
  private[this] lazy val controllers_SecurityController_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_SecurityController_index0_invoker = createInvoker(
    SecurityController_4.get.index,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "index",
      Nil,
      "GET",
      """""",
      this.prefix + """"""
    )
  )

  // @LINE:11
  private[this] lazy val controllers_WikyController_test11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test1")))
  )
  private[this] lazy val controllers_WikyController_test11_invoker = createInvoker(
    WikyController_8.get.test1(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test1",
      Nil,
      "GET",
      """ -> Testovací""",
      this.prefix + """test1"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_WikyController_test22_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test2")))
  )
  private[this] lazy val controllers_WikyController_test22_invoker = createInvoker(
    WikyController_8.get.test2(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test2",
      Nil,
      "GET",
      """""",
      this.prefix + """test2"""
    )
  )

  // @LINE:13
  private[this] lazy val controllers_WikyController_test33_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test3")))
  )
  private[this] lazy val controllers_WikyController_test33_invoker = createInvoker(
    WikyController_8.get.test3(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test3",
      Nil,
      "GET",
      """""",
      this.prefix + """test3"""
    )
  )

  // @LINE:14
  private[this] lazy val controllers_WikyController_test44_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test4")))
  )
  private[this] lazy val controllers_WikyController_test44_invoker = createInvoker(
    WikyController_8.get.test4(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test4",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """test4"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_WikyController_test55_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test5")))
  )
  private[this] lazy val controllers_WikyController_test55_invoker = createInvoker(
    WikyController_8.get.test5(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test5",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """test5"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_WikyController_test66_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test6")))
  )
  private[this] lazy val controllers_WikyController_test66_invoker = createInvoker(
    WikyController_8.get.test6(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test6",
      Nil,
      "GET",
      """""",
      this.prefix + """test6"""
    )
  )

  // @LINE:21
  private[this] lazy val controllers_WebSocketController_Incoming_homer_connection7_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_WebSocketController_Incoming_homer_connection7_invoker = createInvoker(
    WebSocketController_Incoming_0.get.homer_connection(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WebSocketController_Incoming",
      "homer_connection",
      Seq(classOf[String]),
      "GET",
      """  WEB SOCET  //////////////////////////////////////////////////////////////////////////////""",
      this.prefix + """websocket/homer/$id<[^/]+>"""
    )
  )

  // @LINE:22
  private[this] lazy val controllers_WebSocketController_Incoming_mobile_connection8_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/mobile/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_WebSocketController_Incoming_mobile_connection8_invoker = createInvoker(
    WebSocketController_Incoming_0.get.mobile_connection(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WebSocketController_Incoming",
      "mobile_connection",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """websocket/mobile/$id<[^/]+>"""
    )
  )

  // @LINE:24
  private[this] lazy val controllers_WebSocketController_Incoming_getWebSocketStats9_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/webSocketStats")))
  )
  private[this] lazy val controllers_WebSocketController_Incoming_getWebSocketStats9_invoker = createInvoker(
    WebSocketController_Incoming_0.get.getWebSocketStats(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WebSocketController_Incoming",
      "getWebSocketStats",
      Nil,
      "POST",
      """""",
      this.prefix + """websocket/webSocketStats"""
    )
  )

  // @LINE:25
  private[this] lazy val controllers_WebSocketController_Incoming_sendTo10_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/sendTo/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_WebSocketController_Incoming_sendTo10_invoker = createInvoker(
    WebSocketController_Incoming_0.get.sendTo(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WebSocketController_Incoming",
      "sendTo",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """websocket/sendTo/$id<[^/]+>"""
    )
  )

  // @LINE:30
  private[this] lazy val controllers_SecurityController_login11_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/login")))
  )
  private[this] lazy val controllers_SecurityController_login11_invoker = createInvoker(
    SecurityController_4.get.login(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "login",
      Nil,
      "POST",
      """Login page""",
      this.prefix + """coreClient/person/permission/login"""
    )
  )

  // @LINE:31
  private[this] lazy val controllers_SecurityController_logout12_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/logout")))
  )
  private[this] lazy val controllers_SecurityController_logout12_invoker = createInvoker(
    SecurityController_4.get.logout,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "logout",
      Nil,
      "POST",
      """""",
      this.prefix + """coreClient/person/permission/logout"""
    )
  )

  // @LINE:33
  private[this] lazy val controllers_SecurityController_Facebook13_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/facebook")))
  )
  private[this] lazy val controllers_SecurityController_Facebook13_invoker = createInvoker(
    SecurityController_4.get.Facebook(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "Facebook",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """login/facebook"""
    )
  )

  // @LINE:34
  private[this] lazy val controllers_SecurityController_Twitter14_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/twitter")))
  )
  private[this] lazy val controllers_SecurityController_Twitter14_invoker = createInvoker(
    SecurityController_4.get.Twitter(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "Twitter",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """login/twitter"""
    )
  )

  // @LINE:35
  private[this] lazy val controllers_SecurityController_GitHub15_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/github")))
  )
  private[this] lazy val controllers_SecurityController_GitHub15_invoker = createInvoker(
    SecurityController_4.get.GitHub(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "GitHub",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """login/github"""
    )
  )

  // @LINE:36
  private[this] lazy val controllers_SecurityController_Vkontakte16_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/vkontakte")))
  )
  private[this] lazy val controllers_SecurityController_Vkontakte16_invoker = createInvoker(
    SecurityController_4.get.Vkontakte(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "Vkontakte",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """login/vkontakte"""
    )
  )

  // @LINE:38
  private[this] lazy val controllers_SecurityController_getPersonByToken17_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/person")))
  )
  private[this] lazy val controllers_SecurityController_getPersonByToken17_invoker = createInvoker(
    SecurityController_4.get.getPersonByToken(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "getPersonByToken",
      Nil,
      "GET",
      """""",
      this.prefix + """login/person"""
    )
  )

  // @LINE:40
  private[this] lazy val controllers_SecurityController_GET_facebook_oauth18_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/facebook/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_GET_facebook_oauth18_invoker = createInvoker(
    SecurityController_4.get.GET_facebook_oauth(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "GET_facebook_oauth",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """login/facebook/$url<.+>"""
    )
  )

  // @LINE:41
  private[this] lazy val controllers_SecurityController_GET_github_oauth19_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/github/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_GET_github_oauth19_invoker = createInvoker(
    SecurityController_4.get.GET_github_oauth(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "GET_github_oauth",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """login/github/$url<.+>"""
    )
  )

  // @LINE:45
  private[this] lazy val controllers_PermissionController_add_Permission_Person20_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/permission/person/add")))
  )
  private[this] lazy val controllers_PermissionController_add_Permission_Person20_invoker = createInvoker(
    PermissionController_1.get.add_Permission_Person(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "add_Permission_Person",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """secure/permission/person/add"""
    )
  )

  // @LINE:46
  private[this] lazy val controllers_PermissionController_remove_Permission_Person21_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/permission/person/remove")))
  )
  private[this] lazy val controllers_PermissionController_remove_Permission_Person21_invoker = createInvoker(
    PermissionController_1.get.remove_Permission_Person(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "remove_Permission_Person",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """secure/permission/person/remove"""
    )
  )

  // @LINE:47
  private[this] lazy val controllers_PermissionController_get_Permission_All22_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/permission")))
  )
  private[this] lazy val controllers_PermissionController_get_Permission_All22_invoker = createInvoker(
    PermissionController_1.get.get_Permission_All(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "get_Permission_All",
      Nil,
      "GET",
      """""",
      this.prefix + """secure/permission"""
    )
  )

  // @LINE:49
  private[this] lazy val controllers_PermissionController_add_Permission_to_Role23_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role/permission/add")))
  )
  private[this] lazy val controllers_PermissionController_add_Permission_to_Role23_invoker = createInvoker(
    PermissionController_1.get.add_Permission_to_Role(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "add_Permission_to_Role",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """secure/role/permission/add"""
    )
  )

  // @LINE:50
  private[this] lazy val controllers_PermissionController_get_Permission_in_Group24_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role/permission")))
  )
  private[this] lazy val controllers_PermissionController_get_Permission_in_Group24_invoker = createInvoker(
    PermissionController_1.get.get_Permission_in_Group(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "get_Permission_in_Group",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """secure/role/permission"""
    )
  )

  // @LINE:51
  private[this] lazy val controllers_PermissionController_remove_Permission_from_Role25_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role/permission/remove")))
  )
  private[this] lazy val controllers_PermissionController_remove_Permission_from_Role25_invoker = createInvoker(
    PermissionController_1.get.remove_Permission_from_Role(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "remove_Permission_from_Role",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """secure/role/permission/remove"""
    )
  )

  // @LINE:53
  private[this] lazy val controllers_PermissionController_new_Role26_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role")))
  )
  private[this] lazy val controllers_PermissionController_new_Role26_invoker = createInvoker(
    PermissionController_1.get.new_Role(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "new_Role",
      Nil,
      "POST",
      """""",
      this.prefix + """secure/role"""
    )
  )

  // @LINE:54
  private[this] lazy val controllers_PermissionController_delete_Role27_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role")))
  )
  private[this] lazy val controllers_PermissionController_delete_Role27_invoker = createInvoker(
    PermissionController_1.get.delete_Role(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "delete_Role",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """secure/role"""
    )
  )

  // @LINE:56
  private[this] lazy val controllers_PermissionController_add_Role_Person28_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role/person/add")))
  )
  private[this] lazy val controllers_PermissionController_add_Role_Person28_invoker = createInvoker(
    PermissionController_1.get.add_Role_Person(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "add_Role_Person",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """secure/role/person/add"""
    )
  )

  // @LINE:57
  private[this] lazy val controllers_PermissionController_remove_Role_Person29_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role/person/remove")))
  )
  private[this] lazy val controllers_PermissionController_remove_Role_Person29_invoker = createInvoker(
    PermissionController_1.get.remove_Role_Person(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "remove_Role_Person",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """secure/role/person/remove"""
    )
  )

  // @LINE:58
  private[this] lazy val controllers_PermissionController_get_Role_All30_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/role/all")))
  )
  private[this] lazy val controllers_PermissionController_get_Role_All30_invoker = createInvoker(
    PermissionController_1.get.get_Role_All(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "get_Role_All",
      Nil,
      "GET",
      """""",
      this.prefix + """secure/role/all"""
    )
  )

  // @LINE:60
  private[this] lazy val controllers_PermissionController_get_System_Acces31_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("secure/person/system_acces")))
  )
  private[this] lazy val controllers_PermissionController_get_System_Acces31_invoker = createInvoker(
    PermissionController_1.get.get_System_Acces(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "get_System_Acces",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """secure/person/system_acces"""
    )
  )

  // @LINE:68
  private[this] lazy val controllers_PersonController_developerRegistration32_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/developer")))
  )
  private[this] lazy val controllers_PersonController_developerRegistration32_invoker = createInvoker(
    PersonController_2.get.developerRegistration(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonController",
      "developerRegistration",
      Nil,
      "POST",
      """Peron CRUD""",
      this.prefix + """coreClient/person/developer"""
    )
  )

  // @LINE:69
  private[this] lazy val controllers_PersonController_registred_Person33_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonController_registred_Person33_invoker = createInvoker(
    PersonController_2.get.registred_Person(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonController",
      "registred_Person",
      Nil,
      "POST",
      """""",
      this.prefix + """coreClient/person/person"""
    )
  )

  // @LINE:70
  private[this] lazy val controllers_PersonController_edit_Person_Information34_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonController_edit_Person_Information34_invoker = createInvoker(
    PersonController_2.get.edit_Person_Information(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonController",
      "edit_Person_Information",
      Nil,
      "PUT",
      """""",
      this.prefix + """coreClient/person/person"""
    )
  )

  // @LINE:71
  private[this] lazy val controllers_PersonController_getPerson35_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonController_getPerson35_invoker = createInvoker(
    PersonController_2.get.getPerson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonController",
      "getPerson",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """coreClient/person/person/$id<[^/]+>"""
    )
  )

  // @LINE:73
  private[this] lazy val controllers_PersonController_deletePerson36_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonController_deletePerson36_invoker = createInvoker(
    PersonController_2.get.deletePerson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonController",
      "deletePerson",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """coreClient/person/person/$id<[^/]+>"""
    )
  )

  // @LINE:75
  private[this] lazy val controllers_PersonController_email_Person_authentitaction37_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("emailPersonAuthentication/")))
  )
  private[this] lazy val controllers_PersonController_email_Person_authentitaction37_invoker = createInvoker(
    PersonController_2.get.email_Person_authentitaction(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonController",
      "email_Person_authentitaction",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """emailPersonAuthentication/"""
    )
  )

  // @LINE:87
  private[this] lazy val controllers_OverFlowController_newPost38_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_newPost38_invoker = createInvoker(
    OverFlowController_10.get.newPost(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "newPost",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/post"""
    )
  )

  // @LINE:88
  private[this] lazy val controllers_OverFlowController_getPost39_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPost39_invoker = createInvoker(
    OverFlowController_10.get.getPost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "getPost",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """overflow/post/$id<[^/]+>"""
    )
  )

  // @LINE:89
  private[this] lazy val controllers_OverFlowController_deletePost40_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost40_invoker = createInvoker(
    OverFlowController_10.get.deletePost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "deletePost",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """overflow/post/$id<[^/]+>"""
    )
  )

  // @LINE:90
  private[this] lazy val controllers_OverFlowController_editPost41_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_editPost41_invoker = createInvoker(
    OverFlowController_10.get.editPost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "editPost",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """overflow/post/$id<[^/]+>"""
    )
  )

  // @LINE:91
  private[this] lazy val controllers_OverFlowController_getPostByFilter42_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postFilter")))
  )
  private[this] lazy val controllers_OverFlowController_getPostByFilter42_invoker = createInvoker(
    OverFlowController_10.get.getPostByFilter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "getPostByFilter",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/postFilter"""
    )
  )

  // @LINE:92
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers43_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/linkedAnswers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers43_invoker = createInvoker(
    OverFlowController_10.get.getPostLinkedAnswers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "getPostLinkedAnswers",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """overflow/linkedAnswers/$id<[^/]+>"""
    )
  )

  // @LINE:94
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost44_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/hashTags/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost44_invoker = createInvoker(
    OverFlowController_10.get.hashTagsListOnPost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "hashTagsListOnPost",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """overflow/post/hashTags/$id<[^/]+>"""
    )
  )

  // @LINE:95
  private[this] lazy val controllers_OverFlowController_commentsListOnPost45_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/comments/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_commentsListOnPost45_invoker = createInvoker(
    OverFlowController_10.get.commentsListOnPost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "commentsListOnPost",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """overflow/post/comments/$id<[^/]+>"""
    )
  )

  // @LINE:96
  private[this] lazy val controllers_OverFlowController_answereListOnPost46_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/answers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_answereListOnPost46_invoker = createInvoker(
    OverFlowController_10.get.answereListOnPost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "answereListOnPost",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """overflow/post/answers/$id<[^/]+>"""
    )
  )

  // @LINE:97
  private[this] lazy val controllers_OverFlowController_textOfPost47_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/textOfPost/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_textOfPost47_invoker = createInvoker(
    OverFlowController_10.get.textOfPost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "textOfPost",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """overflow/post/textOfPost/$id<[^/]+>"""
    )
  )

  // @LINE:99
  private[this] lazy val controllers_OverFlowController_newTypeOfPost48_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfPost48_invoker = createInvoker(
    OverFlowController_10.get.newTypeOfPost(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "newTypeOfPost",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/typeOfPost"""
    )
  )

  // @LINE:100
  private[this] lazy val controllers_OverFlowController_getTypeOfPost49_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfPost49_invoker = createInvoker(
    OverFlowController_10.get.getTypeOfPost(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "getTypeOfPost",
      Nil,
      "GET",
      """""",
      this.prefix + """overflow/typeOfPost"""
    )
  )

  // @LINE:102
  private[this] lazy val controllers_OverFlowController_newTypeOfConfirms50_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfConfirms50_invoker = createInvoker(
    OverFlowController_10.get.newTypeOfConfirms(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "newTypeOfConfirms",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/typeOfConfirm"""
    )
  )

  // @LINE:103
  private[this] lazy val controllers_OverFlowController_getTypeOfConfirms51_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfConfirms51_invoker = createInvoker(
    OverFlowController_10.get.getTypeOfConfirms(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "getTypeOfConfirms",
      Nil,
      "GET",
      """""",
      this.prefix + """overflow/typeOfConfirm"""
    )
  )

  // @LINE:104
  private[this] lazy val controllers_OverFlowController_putTypeOfConfirmToPost52_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm/"), DynamicPart("conf", """[^/]+""",true), StaticPart("/"), DynamicPart("pst", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_putTypeOfConfirmToPost52_invoker = createInvoker(
    OverFlowController_10.get.putTypeOfConfirmToPost(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "putTypeOfConfirmToPost",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """overflow/typeOfConfirm/$conf<[^/]+>/$pst<[^/]+>"""
    )
  )

  // @LINE:106
  private[this] lazy val controllers_OverFlowController_addComment53_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment")))
  )
  private[this] lazy val controllers_OverFlowController_addComment53_invoker = createInvoker(
    OverFlowController_10.get.addComment(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "addComment",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/comment"""
    )
  )

  // @LINE:107
  private[this] lazy val controllers_OverFlowController_updateComment54_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment54_invoker = createInvoker(
    OverFlowController_10.get.updateComment(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "updateComment",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """overflow/comment/$id<[^/]+>"""
    )
  )

  // @LINE:108
  private[this] lazy val controllers_OverFlowController_deletePost55_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost55_invoker = createInvoker(
    OverFlowController_10.get.deletePost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "deletePost",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """overflow/comment/$id<[^/]+>"""
    )
  )

  // @LINE:110
  private[this] lazy val controllers_OverFlowController_addAnswer56_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer")))
  )
  private[this] lazy val controllers_OverFlowController_addAnswer56_invoker = createInvoker(
    OverFlowController_10.get.addAnswer(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "addAnswer",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/answer"""
    )
  )

  // @LINE:111
  private[this] lazy val controllers_OverFlowController_updateComment57_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment57_invoker = createInvoker(
    OverFlowController_10.get.updateComment(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "updateComment",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """overflow/answer/$id<[^/]+>"""
    )
  )

  // @LINE:112
  private[this] lazy val controllers_OverFlowController_deletePost58_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost58_invoker = createInvoker(
    OverFlowController_10.get.deletePost(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "deletePost",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """overflow/answer/$id<[^/]+>"""
    )
  )

  // @LINE:114
  private[this] lazy val controllers_OverFlowController_likePlus59_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likePlus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likePlus59_invoker = createInvoker(
    OverFlowController_10.get.likePlus(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "likePlus",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """overflow/likePlus/$id<[^/]+>"""
    )
  )

  // @LINE:115
  private[this] lazy val controllers_OverFlowController_likeMinus60_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likeMinus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likeMinus60_invoker = createInvoker(
    OverFlowController_10.get.likeMinus(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "likeMinus",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """overflow/likeMinus/$id<[^/]+>"""
    )
  )

  // @LINE:116
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer61_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link")))
  )
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer61_invoker = createInvoker(
    OverFlowController_10.get.linkWithPreviousAnswer(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "linkWithPreviousAnswer",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/link"""
    )
  )

  // @LINE:117
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer62_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer62_invoker = createInvoker(
    OverFlowController_10.get.unlinkWithPreviousAnswer(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "unlinkWithPreviousAnswer",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """overflow/link/$id<[^/]+>"""
    )
  )

  // @LINE:118
  private[this] lazy val controllers_OverFlowController_removeHashTag63_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeLink")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag63_invoker = createInvoker(
    OverFlowController_10.get.removeHashTag(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "removeHashTag",
      Nil,
      "PUT",
      """""",
      this.prefix + """overflow/removeLink"""
    )
  )

  // @LINE:119
  private[this] lazy val controllers_OverFlowController_addHashTag64_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/hashTag")))
  )
  private[this] lazy val controllers_OverFlowController_addHashTag64_invoker = createInvoker(
    OverFlowController_10.get.addHashTag(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "addHashTag",
      Nil,
      "POST",
      """""",
      this.prefix + """overflow/hashTag"""
    )
  )

  // @LINE:120
  private[this] lazy val controllers_OverFlowController_removeHashTag65_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeHashTag")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag65_invoker = createInvoker(
    OverFlowController_10.get.removeHashTag(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "removeHashTag",
      Nil,
      "PUT",
      """""",
      this.prefix + """overflow/removeHashTag"""
    )
  )

  // @LINE:129
  private[this] lazy val controllers_ProgramingPackageController_postNewProject66_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProject66_invoker = createInvoker(
    ProgramingPackageController_5.get.postNewProject(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "postNewProject",
      Nil,
      "POST",
      """## PROJECT ############ PROJECT ############### PROJECT ################ PROJECT ################# PROJECT ####################################################################
###############################################################################################################################################################################
Project""",
      this.prefix + """project/project"""
    )
  )

  // @LINE:130
  private[this] lazy val controllers_ProgramingPackageController_updateProject67_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateProject67_invoker = createInvoker(
    ProgramingPackageController_5.get.updateProject(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "updateProject",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/project/$id<[^/]+>"""
    )
  )

  // @LINE:131
  private[this] lazy val controllers_ProgramingPackageController_getProject68_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProject68_invoker = createInvoker(
    ProgramingPackageController_5.get.getProject(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProject",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/$id<[^/]+>"""
    )
  )

  // @LINE:132
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount69_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount69_invoker = createInvoker(
    ProgramingPackageController_5.get.getProjectsByUserAccount(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProjectsByUserAccount",
      Nil,
      "GET",
      """""",
      this.prefix + """project/project"""
    )
  )

  // @LINE:133
  private[this] lazy val controllers_ProgramingPackageController_deleteProject70_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteProject70_invoker = createInvoker(
    ProgramingPackageController_5.get.deleteProject(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "deleteProject",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/project/$id<[^/]+>"""
    )
  )

  // @LINE:134
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers71_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/shareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers71_invoker = createInvoker(
    ProgramingPackageController_5.get.shareProjectWithUsers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "shareProjectWithUsers",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/project/shareProject/$id<[^/]+>"""
    )
  )

  // @LINE:135
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers72_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/unshareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers72_invoker = createInvoker(
    ProgramingPackageController_5.get.unshareProjectWithUsers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "unshareProjectWithUsers",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/project/unshareProject/$id<[^/]+>"""
    )
  )

  // @LINE:136
  private[this] lazy val controllers_ProgramingPackageController_getAll_b_Programs73_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/b_programs/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAll_b_Programs73_invoker = createInvoker(
    ProgramingPackageController_5.get.getAll_b_Programs(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getAll_b_Programs",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/b_programs/$id<[^/]+>"""
    )
  )

  // @LINE:137
  private[this] lazy val controllers_ProgramingPackageController_getAll_c_Programs74_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/c_programs/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAll_c_Programs74_invoker = createInvoker(
    ProgramingPackageController_5.get.getAll_c_Programs(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getAll_c_Programs",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/c_programs/$id<[^/]+>"""
    )
  )

  // @LINE:138
  private[this] lazy val controllers_ProgramingPackageController_getAll_m_Projects75_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/m_projects/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAll_m_Projects75_invoker = createInvoker(
    ProgramingPackageController_5.get.getAll_m_Projects(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getAll_m_Projects",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/m_projects/$id<[^/]+>"""
    )
  )

  // @LINE:140
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList76_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/homerList/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList76_invoker = createInvoker(
    ProgramingPackageController_5.get.getProgramhomerList(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgramhomerList",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/homerList/$id<[^/]+>"""
    )
  )

  // @LINE:141
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners77_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/owners/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners77_invoker = createInvoker(
    ProgramingPackageController_5.get.getProjectOwners(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProjectOwners",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/owners/$id<[^/]+>"""
    )
  )

  // @LINE:144
  private[this] lazy val controllers_ProgramingPackageController_newHomer78_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newHomer78_invoker = createInvoker(
    ProgramingPackageController_5.get.newHomer(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "newHomer",
      Nil,
      "POST",
      """Homer""",
      this.prefix + """project/homer"""
    )
  )

  // @LINE:145
  private[this] lazy val controllers_ProgramingPackageController_removeHomer79_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeHomer79_invoker = createInvoker(
    ProgramingPackageController_5.get.removeHomer(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "removeHomer",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/homer/$id<[^/]+>"""
    )
  )

  // @LINE:146
  private[this] lazy val controllers_ProgramingPackageController_getHomer80_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getHomer80_invoker = createInvoker(
    ProgramingPackageController_5.get.getHomer(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getHomer",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/homer/$id<[^/]+>"""
    )
  )

  // @LINE:147
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers81_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers81_invoker = createInvoker(
    ProgramingPackageController_5.get.getAllHomers(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getAllHomers",
      Nil,
      "GET",
      """""",
      this.prefix + """project/homer"""
    )
  )

  // @LINE:148
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers82_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/getAllConnectedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers82_invoker = createInvoker(
    ProgramingPackageController_5.get.getConnectedHomers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getConnectedHomers",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/homer/getAllConnectedHomers/$id<[^/]+>"""
    )
  )

  // @LINE:152
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject83_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject83_invoker = createInvoker(
    ProgramingPackageController_5.get.connectHomerWithProject(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "connectHomerWithProject",
      Nil,
      "PUT",
      """Project - connection""",
      this.prefix + """project/connectHomerWithProject"""
    )
  )

  // @LINE:153
  private[this] lazy val controllers_ProgramingPackageController_disconnectHomerWithProject84_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/disconnectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_disconnectHomerWithProject84_invoker = createInvoker(
    ProgramingPackageController_5.get.disconnectHomerWithProject(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "disconnectHomerWithProject",
      Nil,
      "PUT",
      """""",
      this.prefix + """project/disconnectHomerWithProject"""
    )
  )

  // @LINE:157
  private[this] lazy val controllers_ProgramingPackageController_postNewBProgram85_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewBProgram85_invoker = createInvoker(
    ProgramingPackageController_5.get.postNewBProgram(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "postNewBProgram",
      Nil,
      "POST",
      """Program""",
      this.prefix + """project/b_program"""
    )
  )

  // @LINE:158
  private[this] lazy val controllers_ProgramingPackageController_getProgram86_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgram86_invoker = createInvoker(
    ProgramingPackageController_5.get.getProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgram",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/b_program/$id<[^/]+>"""
    )
  )

  // @LINE:159
  private[this] lazy val controllers_ProgramingPackageController_editProgram87_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editProgram87_invoker = createInvoker(
    ProgramingPackageController_5.get.editProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "editProgram",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/b_program/$id<[^/]+>"""
    )
  )

  // @LINE:160
  private[this] lazy val controllers_ProgramingPackageController_update_b_program88_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/update/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_update_b_program88_invoker = createInvoker(
    ProgramingPackageController_5.get.update_b_program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "update_b_program",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/b_program/update/$id<[^/]+>"""
    )
  )

  // @LINE:161
  private[this] lazy val controllers_ProgramingPackageController_remove_b_Program89_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_remove_b_Program89_invoker = createInvoker(
    ProgramingPackageController_5.get.remove_b_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "remove_b_Program",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/b_program/$id<[^/]+>"""
    )
  )

  // @LINE:162
  private[this] lazy val controllers_ProgramingPackageController_getProgramInString90_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_programInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInString90_invoker = createInvoker(
    ProgramingPackageController_5.get.getProgramInString(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgramInString",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/b_programInJson/$id<[^/]+>"""
    )
  )

  // @LINE:163
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately91_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/upload/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("ver", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately91_invoker = createInvoker(
    ProgramingPackageController_5.get.uploadProgramToHomer_Immediately(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "uploadProgramToHomer_Immediately",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/b_program/upload/$id<[^/]+>/$ver<[^/]+>"""
    )
  )

  // @LINE:164
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToCloud92_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/uploadToCloud/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("ver", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToCloud92_invoker = createInvoker(
    ProgramingPackageController_5.get.uploadProgramToCloud(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "uploadProgramToCloud",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/b_program/uploadToCloud/$id<[^/]+>/$ver<[^/]+>"""
    )
  )

  // @LINE:166
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers93_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfUploadedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers93_invoker = createInvoker(
    ProgramingPackageController_5.get.listOfUploadedHomers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "listOfUploadedHomers",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/listOfUploadedHomers/$id<[^/]+>"""
    )
  )

  // @LINE:167
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload94_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfHomersWaitingForUpload/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload94_invoker = createInvoker(
    ProgramingPackageController_5.get.listOfHomersWaitingForUpload(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "listOfHomersWaitingForUpload",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/listOfHomersWaitingForUpload/$id<[^/]+>"""
    )
  )

  // @LINE:168
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard95_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard95_invoker = createInvoker(
    ProgramingPackageController_5.get.getProjectsBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProjectsBoard",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/boards/$id<[^/]+>"""
    )
  )

  // @LINE:171
  private[this] lazy val controllers_ProgramingPackageController_newBlock96_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newBlock96_invoker = createInvoker(
    ProgramingPackageController_5.get.newBlock(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "newBlock",
      Nil,
      "POST",
      """Blocks""",
      this.prefix + """project/blockoBlock"""
    )
  )

  // @LINE:172
  private[this] lazy val controllers_ProgramingPackageController_updateOfBlock97_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateOfBlock97_invoker = createInvoker(
    ProgramingPackageController_5.get.updateOfBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "updateOfBlock",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """project/blockoBlock/$id<[^/]+>"""
    )
  )

  // @LINE:173
  private[this] lazy val controllers_ProgramingPackageController_editBlock98_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editBlock98_invoker = createInvoker(
    ProgramingPackageController_5.get.editBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "editBlock",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/blockoBlock/$id<[^/]+>"""
    )
  )

  // @LINE:174
  private[this] lazy val controllers_ProgramingPackageController_getBlockBlock99_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlockBlock99_invoker = createInvoker(
    ProgramingPackageController_5.get.getBlockBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getBlockBlock",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/$id<[^/]+>"""
    )
  )

  // @LINE:175
  private[this] lazy val controllers_ProgramingPackageController_getBlockVersions100_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/versions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlockVersions100_invoker = createInvoker(
    ProgramingPackageController_5.get.getBlockVersions(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getBlockVersions",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/versions/$id<[^/]+>"""
    )
  )

  // @LINE:178
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions101_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/allPrevVersions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions101_invoker = createInvoker(
    ProgramingPackageController_5.get.allPrevVersions(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "allPrevVersions",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/allPrevVersions/$id<[^/]+>"""
    )
  )

  // @LINE:179
  private[this] lazy val controllers_ProgramingPackageController_deleteBlockVersion102_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlockVersion102_invoker = createInvoker(
    ProgramingPackageController_5.get.deleteBlockVersion(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "deleteBlockVersion",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/blockoBlock/version/$id<[^/]+>"""
    )
  )

  // @LINE:180
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock103_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/block/id")))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock103_invoker = createInvoker(
    ProgramingPackageController_5.get.deleteBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "deleteBlock",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/blockoBlock/block/id"""
    )
  )

  // @LINE:182
  private[this] lazy val controllers_ProgramingPackageController_newTypeOfBlock104_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newTypeOfBlock104_invoker = createInvoker(
    ProgramingPackageController_5.get.newTypeOfBlock(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "newTypeOfBlock",
      Nil,
      "POST",
      """""",
      this.prefix + """project/typeOfBlock"""
    )
  )

  // @LINE:183
  private[this] lazy val controllers_ProgramingPackageController_getByCategory105_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock/filter")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getByCategory105_invoker = createInvoker(
    ProgramingPackageController_5.get.getByCategory(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getByCategory",
      Nil,
      "GET",
      """""",
      this.prefix + """project/typeOfBlock/filter"""
    )
  )

  // @LINE:184
  private[this] lazy val controllers_ProgramingPackageController_editTypeOfBlock106_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editTypeOfBlock106_invoker = createInvoker(
    ProgramingPackageController_5.get.editTypeOfBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "editTypeOfBlock",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/typeOfBlock/$id<[^/]+>"""
    )
  )

  // @LINE:185
  private[this] lazy val controllers_ProgramingPackageController_getAllTypeOfBlocks107_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllTypeOfBlocks107_invoker = createInvoker(
    ProgramingPackageController_5.get.getAllTypeOfBlocks(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getAllTypeOfBlocks",
      Nil,
      "GET",
      """""",
      this.prefix + """project/typeOfBlock"""
    )
  )

  // @LINE:186
  private[this] lazy val controllers_ProgramingPackageController_deleteTypeOfBlock108_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteTypeOfBlock108_invoker = createInvoker(
    ProgramingPackageController_5.get.deleteTypeOfBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "deleteTypeOfBlock",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/typeOfBlock/$id<[^/]+>"""
    )
  )

  // @LINE:198
  private[this] lazy val controllers_CompilationLibrariesController_create_C_Program109_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/"), DynamicPart("project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_create_C_Program109_invoker = createInvoker(
    CompilationLibrariesController_7.get.create_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "create_C_Program",
      Seq(classOf[String]),
      "POST",
      """C:Program""",
      this.prefix + """compilation/c_program/$project_id<[^/]+>"""
    )
  )

  // @LINE:199
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program110_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/"), DynamicPart("c_program_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program110_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_C_Program",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/c_program/$c_program_id<[^/]+>"""
    )
  )

  // @LINE:200
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program_All_from_Project111_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/project/"), DynamicPart("project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program_All_from_Project111_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_C_Program_All_from_Project(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_C_Program_All_from_Project",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/c_program/project/$project_id<[^/]+>"""
    )
  )

  // @LINE:202
  private[this] lazy val controllers_CompilationLibrariesController_edit_C_Program_Description112_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/edit/"), DynamicPart("c_program_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_edit_C_Program_Description112_invoker = createInvoker(
    CompilationLibrariesController_7.get.edit_C_Program_Description(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "edit_C_Program_Description",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/c_program/edit/$c_program_id<[^/]+>"""
    )
  )

  // @LINE:203
  private[this] lazy val controllers_CompilationLibrariesController_update_C_Program113_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/update/"), DynamicPart("c_program_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_update_C_Program113_invoker = createInvoker(
    CompilationLibrariesController_7.get.update_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "update_C_Program",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/c_program/update/$c_program_id<[^/]+>"""
    )
  )

  // @LINE:205
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program114_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/c_program/"), DynamicPart("c_program_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program114_invoker = createInvoker(
    CompilationLibrariesController_7.get.delete_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_C_Program",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/c_program/c_program/$c_program_id<[^/]+>"""
    )
  )

  // @LINE:206
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program_Version115_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/version/"), DynamicPart("c_program_id", """[^/]+""",true), StaticPart("/"), DynamicPart("version_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program_Version115_invoker = createInvoker(
    CompilationLibrariesController_7.get.delete_C_Program_Version(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_C_Program_Version",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/c_program/version/$c_program_id<[^/]+>/$version_id<[^/]+>"""
    )
  )

  // @LINE:208
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse116_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/eclipse")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse116_invoker = createInvoker(
    CompilationLibrariesController_7.get.generateProjectForEclipse(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "generateProjectForEclipse",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/c_program/eclipse"""
    )
  )

  // @LINE:209
  private[this] lazy val controllers_CompilationLibrariesController_uploadCompilationToBoard117_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/upload/"), DynamicPart("c_program_id", """[^/]+""",true), StaticPart("/"), DynamicPart("board_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadCompilationToBoard117_invoker = createInvoker(
    CompilationLibrariesController_7.get.uploadCompilationToBoard(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploadCompilationToBoard",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/c_program/upload/$c_program_id<[^/]+>/$board_id<[^/]+>"""
    )
  )

  // @LINE:210
  private[this] lazy val controllers_CompilationLibrariesController_uploadBinaryFileToBoard118_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/binary/"), DynamicPart("board_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadBinaryFileToBoard118_invoker = createInvoker(
    CompilationLibrariesController_7.get.uploadBinaryFileToBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploadBinaryFileToBoard",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/c_program/binary/$board_id<[^/]+>"""
    )
  )

  // @LINE:212
  private[this] lazy val controllers_CompilationLibrariesController_get_Boards_from_Project119_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/board/"), DynamicPart("project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Boards_from_Project119_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Boards_from_Project(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Boards_from_Project",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/project/board/$project_id<[^/]+>"""
    )
  )

  // @LINE:215
  private[this] lazy val controllers_CompilationLibrariesController_new_Processor120_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_Processor120_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_Processor(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_Processor",
      Nil,
      "POST",
      """Processor""",
      this.prefix + """compilation/processor"""
    )
  )

  // @LINE:216
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor121_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("processor_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor121_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Processor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Processor",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/$processor_id<[^/]+>"""
    )
  )

  // @LINE:217
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor_All122_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor_All122_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Processor_All(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Processor_All",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/processor"""
    )
  )

  // @LINE:218
  private[this] lazy val controllers_CompilationLibrariesController_update_Processor123_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("processor_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_update_Processor123_invoker = createInvoker(
    CompilationLibrariesController_7.get.update_Processor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "update_Processor",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/processor/$processor_id<[^/]+>"""
    )
  )

  // @LINE:219
  private[this] lazy val controllers_CompilationLibrariesController_delete_Processor124_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("processor_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_Processor124_invoker = createInvoker(
    CompilationLibrariesController_7.get.delete_Processor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_Processor",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/processor/$processor_id<[^/]+>"""
    )
  )

  // @LINE:221
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary125_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/library/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary125_invoker = createInvoker(
    CompilationLibrariesController_7.get.connectProcessorWithLibrary(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "connectProcessorWithLibrary",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/processor/library/$processor_id<[^/]+>/$library_id<[^/]+>"""
    )
  )

  // @LINE:222
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup126_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroup/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup126_invoker = createInvoker(
    CompilationLibrariesController_7.get.connectProcessorWithLibraryGroup(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "connectProcessorWithLibraryGroup",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/processor/libraryGroup/$processor_id<[^/]+>/$library_id<[^/]+>"""
    )
  )

  // @LINE:223
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibrary127_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/library/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibrary127_invoker = createInvoker(
    CompilationLibrariesController_7.get.disconnectProcessorWithLibrary(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "disconnectProcessorWithLibrary",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/processor/library/$processor_id<[^/]+>/$library_id<[^/]+>"""
    )
  )

  // @LINE:224
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup128_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroup/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup128_invoker = createInvoker(
    CompilationLibrariesController_7.get.disconnectProcessorWithLibraryGroup(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "disconnectProcessorWithLibraryGroup",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/processor/libraryGroup/$processor_id<[^/]+>/$library_id<[^/]+>"""
    )
  )

  // @LINE:226
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups129_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroups/"), DynamicPart("processor_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups129_invoker = createInvoker(
    CompilationLibrariesController_7.get.getProcessorLibraryGroups(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorLibraryGroups",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/libraryGroups/$processor_id<[^/]+>"""
    )
  )

  // @LINE:227
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries130_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/singleLibrary/"), DynamicPart("processor_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries130_invoker = createInvoker(
    CompilationLibrariesController_7.get.getProcessorSingleLibraries(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorSingleLibraries",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/singleLibrary/$processor_id<[^/]+>"""
    )
  )

  // @LINE:230
  private[this] lazy val controllers_CompilationLibrariesController_new_Board131_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_Board131_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_Board(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_Board",
      Nil,
      "POST",
      """Board""",
      this.prefix + """compilation/board"""
    )
  )

  // @LINE:231
  private[this] lazy val controllers_CompilationLibrariesController_edit_Board_User_Description132_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("type_of_board_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_edit_Board_User_Description132_invoker = createInvoker(
    CompilationLibrariesController_7.get.edit_Board_User_Description(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "edit_Board_User_Description",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/userDescription/$type_of_board_id<[^/]+>"""
    )
  )

  // @LINE:232
  private[this] lazy val controllers_CompilationLibrariesController_get_Board_Filter133_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Board_Filter133_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Board_Filter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Board_Filter",
      Nil,
      "PUT",
      """""",
      this.prefix + """compilation/board/filter"""
    )
  )

  // @LINE:233
  private[this] lazy val controllers_CompilationLibrariesController_get_Board134_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("board_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Board134_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Board(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Board",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/board/$board_id<[^/]+>"""
    )
  )

  // @LINE:234
  private[this] lazy val controllers_CompilationLibrariesController_deactivate_Board135_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/deactivateBoard/"), DynamicPart("board_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deactivate_Board135_invoker = createInvoker(
    CompilationLibrariesController_7.get.deactivate_Board(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deactivate_Board",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/board/deactivateBoard/$board_id<[^/]+>"""
    )
  )

  // @LINE:235
  private[this] lazy val controllers_CompilationLibrariesController_connect_Board_with_Project136_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/connect/"), DynamicPart("board_id", """[^/]+""",true), StaticPart("/"), DynamicPart("project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connect_Board_with_Project136_invoker = createInvoker(
    CompilationLibrariesController_7.get.connect_Board_with_Project(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "connect_Board_with_Project",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/connect/$board_id<[^/]+>/$project_id<[^/]+>"""
    )
  )

  // @LINE:236
  private[this] lazy val controllers_CompilationLibrariesController_disconnect_Board_from_Project137_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/disconnect/"), DynamicPart("board_id", """[^/]+""",true), StaticPart("/"), DynamicPart("project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_disconnect_Board_from_Project137_invoker = createInvoker(
    CompilationLibrariesController_7.get.disconnect_Board_from_Project(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "disconnect_Board_from_Project",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/disconnect/$board_id<[^/]+>/$project_id<[^/]+>"""
    )
  )

  // @LINE:237
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects138_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/projects/"), DynamicPart("board_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects138_invoker = createInvoker(
    CompilationLibrariesController_7.get.getBoardProjects(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getBoardProjects",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/board/projects/$board_id<[^/]+>"""
    )
  )

  // @LINE:241
  private[this] lazy val controllers_CompilationLibrariesController_new_Producer139_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_Producer139_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_Producer(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_Producer",
      Nil,
      "POST",
      """Producer""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:242
  private[this] lazy val controllers_CompilationLibrariesController_edit_Producer140_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_edit_Producer140_invoker = createInvoker(
    CompilationLibrariesController_7.get.edit_Producer(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "edit_Producer",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:243
  private[this] lazy val controllers_CompilationLibrariesController_get_Producers141_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/all")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Producers141_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Producers(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Producers",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/producer/all"""
    )
  )

  // @LINE:244
  private[this] lazy val controllers_CompilationLibrariesController_get_Producer142_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Producer142_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Producer(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Producer",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:245
  private[this] lazy val controllers_CompilationLibrariesController_get_Producer_Description143_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/description")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Producer_Description143_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Producer_Description(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Producer_Description",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer/description"""
    )
  )

  // @LINE:246
  private[this] lazy val controllers_CompilationLibrariesController_get_Producer_TypeOfBoards144_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/typeOfBoards")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Producer_TypeOfBoards144_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_Producer_TypeOfBoards(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Producer_TypeOfBoards",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer/typeOfBoards"""
    )
  )

  // @LINE:249
  private[this] lazy val controllers_CompilationLibrariesController_new_TypeOfBoard145_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_TypeOfBoard145_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_TypeOfBoard(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_TypeOfBoard",
      Nil,
      "POST",
      """TypeOfBoard""",
      this.prefix + """compilation/typeOfBoard"""
    )
  )

  // @LINE:250
  private[this] lazy val controllers_CompilationLibrariesController_edit_TypeOfBoard146_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_edit_TypeOfBoard146_invoker = createInvoker(
    CompilationLibrariesController_7.get.edit_TypeOfBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "edit_TypeOfBoard",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/typeOfBoard"""
    )
  )

  // @LINE:251
  private[this] lazy val controllers_CompilationLibrariesController_delete_TypeOfBoard147_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_TypeOfBoard147_invoker = createInvoker(
    CompilationLibrariesController_7.get.delete_TypeOfBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_TypeOfBoard",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/typeOfBoard"""
    )
  )

  // @LINE:252
  private[this] lazy val controllers_CompilationLibrariesController_get_TypeOfBoard_all148_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/all")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_TypeOfBoard_all148_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_TypeOfBoard_all(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_TypeOfBoard_all",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard/all"""
    )
  )

  // @LINE:253
  private[this] lazy val controllers_CompilationLibrariesController_get_TypeOfBoard149_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_TypeOfBoard149_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_TypeOfBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_TypeOfBoard",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard"""
    )
  )

  // @LINE:254
  private[this] lazy val controllers_CompilationLibrariesController_get_TypeOfBoard_Description150_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/description")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_TypeOfBoard_Description150_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_TypeOfBoard_Description(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_TypeOfBoard_Description",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard/description"""
    )
  )

  // @LINE:255
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards151_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/boards")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards151_invoker = createInvoker(
    CompilationLibrariesController_7.get.getTypeOfBoardAllBoards(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getTypeOfBoardAllBoards",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard/boards"""
    )
  )

  // @LINE:258
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup152_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup152_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_LibraryGroup(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_LibraryGroup",
      Nil,
      "POST",
      """LibraryGroups""",
      this.prefix + """compilation/libraryGroup"""
    )
  )

  // @LINE:259
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup153_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("libraryGroup_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup153_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_LibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/$libraryGroup_id<[^/]+>"""
    )
  )

  // @LINE:260
  private[this] lazy val controllers_CompilationLibrariesController_delete_LibraryGroup154_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("libraryGroup_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_LibraryGroup154_invoker = createInvoker(
    CompilationLibrariesController_7.get.delete_LibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_LibraryGroup",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/libraryGroup/$libraryGroup_id<[^/]+>"""
    )
  )

  // @LINE:261
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Filter155_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Filter155_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_LibraryGroup_Filter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Filter",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/filter"""
    )
  )

  // @LINE:262
  private[this] lazy val controllers_CompilationLibrariesController_editLibraryGroup156_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("libraryGroup_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_editLibraryGroup156_invoker = createInvoker(
    CompilationLibrariesController_7.get.editLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "editLibraryGroup",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/libraryGroup/$libraryGroup_id<[^/]+>"""
    )
  )

  // @LINE:263
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Description157_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/generalDescription/"), DynamicPart("libraryGroup_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Description157_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_LibraryGroup_Description(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Description",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/generalDescription/$libraryGroup_id<[^/]+>"""
    )
  )

  // @LINE:264
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Processors158_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/processors/"), DynamicPart("libraryGroup_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Processors158_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_LibraryGroup_Processors(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Processors",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/processors/$libraryGroup_id<[^/]+>"""
    )
  )

  // @LINE:265
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Libraries159_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/libraries/"), DynamicPart("libraryGroup_id", """[^/]+""",true), StaticPart("/"), DynamicPart("version_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Libraries159_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_LibraryGroup_Libraries(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Libraries",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/libraries/$libraryGroup_id<[^/]+>/$version_id<[^/]+>"""
    )
  )

  // @LINE:266
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup_Version160_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/version/"), DynamicPart("version_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup_Version160_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_LibraryGroup_Version(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_LibraryGroup_Version",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/version/$version_id<[^/]+>"""
    )
  )

  // @LINE:267
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version161_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/versions/"), DynamicPart("version_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version161_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_LibraryGroup_Version(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Version",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/versions/$version_id<[^/]+>"""
    )
  )

  // @LINE:268
  private[this] lazy val controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup162_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/upload/"), DynamicPart("libraryGroup_id", """[^/]+""",true), StaticPart("/"), DynamicPart("version_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup162_invoker = createInvoker(
    CompilationLibrariesController_7.get.upload_Library_To_LibraryGroup(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "upload_Library_To_LibraryGroup",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/upload/$libraryGroup_id<[^/]+>/$version_id<[^/]+>"""
    )
  )

  // @LINE:271
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary163_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary163_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_SingleLibrary(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_SingleLibrary",
      Nil,
      "POST",
      """Single Library""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:272
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary_Version164_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/version/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary_Version164_invoker = createInvoker(
    CompilationLibrariesController_7.get.new_SingleLibrary_Version(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_SingleLibrary_Version",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/library/version/$library_id<[^/]+>"""
    )
  )

  // @LINE:273
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Versions165_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/versions/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Versions165_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_SingleLibrary_Versions(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_SingleLibrary_Versions",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/versions/$library_id<[^/]+>"""
    )
  )

  // @LINE:274
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Filter166_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Filter166_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_SingleLibrary_Filter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_SingleLibrary_Filter",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/library/filter"""
    )
  )

  // @LINE:275
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary167_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary167_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_SingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_SingleLibrary",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/$library_id<[^/]+>"""
    )
  )

  // @LINE:277
  private[this] lazy val controllers_CompilationLibrariesController_edit_SingleLibrary168_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_edit_SingleLibrary168_invoker = createInvoker(
    CompilationLibrariesController_7.get.edit_SingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "edit_SingleLibrary",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/library$library_id<[^/]+>"""
    )
  )

  // @LINE:278
  private[this] lazy val controllers_CompilationLibrariesController_delete_SingleLibrary169_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_SingleLibrary169_invoker = createInvoker(
    CompilationLibrariesController_7.get.delete_SingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_SingleLibrary",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/library$library_id<[^/]+>"""
    )
  )

  // @LINE:279
  private[this] lazy val controllers_CompilationLibrariesController_upload_SingleLibrary_Version170_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/upload"), DynamicPart("library_id", """[^/]+""",true), StaticPart("/"), DynamicPart("version_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_upload_SingleLibrary_Version170_invoker = createInvoker(
    CompilationLibrariesController_7.get.upload_SingleLibrary_Version(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "upload_SingleLibrary_Version",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/library/upload$library_id<[^/]+>/$version_id<[^/]+>"""
    )
  )

  // @LINE:282
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries171_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("file/listOfFiles/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries171_invoker = createInvoker(
    CompilationLibrariesController_7.get.get_LibraryGroup_Version_Libraries(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Version_Libraries",
      Seq(classOf[String]),
      "GET",
      """File""",
      this.prefix + """file/listOfFiles/$id<[^/]+>"""
    )
  )

  // @LINE:283
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord172_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("file/fileRecord/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord172_invoker = createInvoker(
    CompilationLibrariesController_7.get.fileRecord(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "fileRecord",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """file/fileRecord/$id<[^/]+>"""
    )
  )

  // @LINE:290
  private[this] lazy val controllers_GridController_new_M_Project173_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project")))
  )
  private[this] lazy val controllers_GridController_new_M_Project173_invoker = createInvoker(
    GridController_6.get.new_M_Project(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "new_M_Project",
      Seq(classOf[String]),
      "POST",
      """M Project""",
      this.prefix + """grid/m_project"""
    )
  )

  // @LINE:291
  private[this] lazy val controllers_GridController_get_M_Projects_ByLoggedPerson174_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/person")))
  )
  private[this] lazy val controllers_GridController_get_M_Projects_ByLoggedPerson174_invoker = createInvoker(
    GridController_6.get.get_M_Projects_ByLoggedPerson(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Projects_ByLoggedPerson",
      Nil,
      "GET",
      """""",
      this.prefix + """grid/m_project/person"""
    )
  )

  // @LINE:292
  private[this] lazy val controllers_GridController_get_M_Project175_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/"), DynamicPart("m_project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_M_Project175_invoker = createInvoker(
    GridController_6.get.get_M_Project(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Project",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/m_project/$m_project_id<[^/]+>"""
    )
  )

  // @LINE:293
  private[this] lazy val controllers_GridController_edit_M_Project176_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/"), DynamicPart("m_project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_edit_M_Project176_invoker = createInvoker(
    GridController_6.get.edit_M_Project(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "edit_M_Project",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """grid/m_project/$m_project_id<[^/]+>"""
    )
  )

  // @LINE:294
  private[this] lazy val controllers_GridController_remove_M_Project177_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/"), DynamicPart("m_project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_remove_M_Project177_invoker = createInvoker(
    GridController_6.get.remove_M_Project(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "remove_M_Project",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """grid/m_project/$m_project_id<[^/]+>"""
    )
  )

  // @LINE:296
  private[this] lazy val controllers_GridController_get_M_Projects_from_GlobalProject178_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/project/"), DynamicPart("project_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_M_Projects_from_GlobalProject178_invoker = createInvoker(
    GridController_6.get.get_M_Projects_from_GlobalProject(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Projects_from_GlobalProject",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/m_project/project/$project_id<[^/]+>"""
    )
  )

  // @LINE:300
  private[this] lazy val controllers_GridController_new_M_Program179_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program")))
  )
  private[this] lazy val controllers_GridController_new_M_Program179_invoker = createInvoker(
    GridController_6.get.new_M_Program(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "new_M_Program",
      Nil,
      "POST",
      """M Program""",
      this.prefix + """grid/m_program"""
    )
  )

  // @LINE:301
  private[this] lazy val controllers_GridController_get_M_Program_byQR_Token_forMobile180_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/app/token/"), DynamicPart("qr_token", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_M_Program_byQR_Token_forMobile180_invoker = createInvoker(
    GridController_6.get.get_M_Program_byQR_Token_forMobile(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Program_byQR_Token_forMobile",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/m_program/app/token/$qr_token<[^/]+>"""
    )
  )

  // @LINE:302
  private[this] lazy val controllers_GridController_get_M_Program_all_forMobile181_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/app/m_programs")))
  )
  private[this] lazy val controllers_GridController_get_M_Program_all_forMobile181_invoker = createInvoker(
    GridController_6.get.get_M_Program_all_forMobile(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Program_all_forMobile",
      Nil,
      "GET",
      """""",
      this.prefix + """grid/m_program/app/m_programs"""
    )
  )

  // @LINE:305
  private[this] lazy val controllers_GridController_get_M_Program182_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/"), DynamicPart("m_progrm_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_M_Program182_invoker = createInvoker(
    GridController_6.get.get_M_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Program",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/m_program/$m_progrm_id<[^/]+>"""
    )
  )

  // @LINE:306
  private[this] lazy val controllers_GridController_edit_M_Program183_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/"), DynamicPart("m_progrm_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_edit_M_Program183_invoker = createInvoker(
    GridController_6.get.edit_M_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "edit_M_Program",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """grid/m_program/$m_progrm_id<[^/]+>"""
    )
  )

  // @LINE:307
  private[this] lazy val controllers_GridController_remove_M_Program184_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/"), DynamicPart("m_progrm_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_remove_M_Program184_invoker = createInvoker(
    GridController_6.get.remove_M_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "remove_M_Program",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """grid/m_program/$m_progrm_id<[^/]+>"""
    )
  )

  // @LINE:313
  private[this] lazy val controllers_GridController_new_Screen_Size_Type185_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type")))
  )
  private[this] lazy val controllers_GridController_new_Screen_Size_Type185_invoker = createInvoker(
    GridController_6.get.new_Screen_Size_Type(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "new_Screen_Size_Type",
      Nil,
      "POST",
      """M Screen Type""",
      this.prefix + """grid/screen_type"""
    )
  )

  // @LINE:314
  private[this] lazy val controllers_GridController_get_Screen_Size_Type_Combination186_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/all")))
  )
  private[this] lazy val controllers_GridController_get_Screen_Size_Type_Combination186_invoker = createInvoker(
    GridController_6.get.get_Screen_Size_Type_Combination(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_Screen_Size_Type_Combination",
      Nil,
      "GET",
      """""",
      this.prefix + """grid/screen_type/all"""
    )
  )

  // @LINE:315
  private[this] lazy val controllers_GridController_get_Screen_Size_Type187_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/"), DynamicPart("screen_size_type_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_Screen_Size_Type187_invoker = createInvoker(
    GridController_6.get.get_Screen_Size_Type(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_Screen_Size_Type",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/screen_type/$screen_size_type_id<[^/]+>"""
    )
  )

  // @LINE:316
  private[this] lazy val controllers_GridController_edit_Screen_Size_Type188_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/"), DynamicPart("screen_size_type_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_edit_Screen_Size_Type188_invoker = createInvoker(
    GridController_6.get.edit_Screen_Size_Type(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "edit_Screen_Size_Type",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """grid/screen_type/$screen_size_type_id<[^/]+>"""
    )
  )

  // @LINE:317
  private[this] lazy val controllers_GridController_remove_Screen_Size_Type189_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/"), DynamicPart("screen_size_type_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_remove_Screen_Size_Type189_invoker = createInvoker(
    GridController_6.get.remove_Screen_Size_Type(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "remove_Screen_Size_Type",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """grid/screen_type/$screen_size_type_id<[^/]+>"""
    )
  )

  // @LINE:326
  private[this] lazy val utilities_swagger_ApiHelpController_getResources190_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api-docs")))
  )
  private[this] lazy val utilities_swagger_ApiHelpController_getResources190_invoker = createInvoker(
    ApiHelpController_3.get.getResources,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "utilities.swagger.ApiHelpController",
      "getResources",
      Nil,
      "GET",
      """SWAGGER API""",
      this.prefix + """api-docs"""
    )
  )

  // @LINE:329
  private[this] lazy val controllers_SecurityController_optionLink191_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("all", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_optionLink191_invoker = createInvoker(
    SecurityController_4.get.optionLink(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "optionLink",
      Seq(classOf[String]),
      "OPTIONS",
      """CORS""",
      this.prefix + """$all<.+>"""
    )
  )

  // @LINE:332
  private[this] lazy val controllers_Assets_at192_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at192_invoker = createInvoker(
    Assets_9.at(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      """ Map static resources from the /public folder to the /assets URL path""",
      this.prefix + """assets/$file<.+>"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:8
    case controllers_SecurityController_index0_route(params) =>
      call { 
        controllers_SecurityController_index0_invoker.call(SecurityController_4.get.index)
      }
  
    // @LINE:11
    case controllers_WikyController_test11_route(params) =>
      call { 
        controllers_WikyController_test11_invoker.call(WikyController_8.get.test1())
      }
  
    // @LINE:12
    case controllers_WikyController_test22_route(params) =>
      call { 
        controllers_WikyController_test22_invoker.call(WikyController_8.get.test2())
      }
  
    // @LINE:13
    case controllers_WikyController_test33_route(params) =>
      call { 
        controllers_WikyController_test33_invoker.call(WikyController_8.get.test3())
      }
  
    // @LINE:14
    case controllers_WikyController_test44_route(params) =>
      call(params.fromQuery[String]("projectId", None)) { (projectId) =>
        controllers_WikyController_test44_invoker.call(WikyController_8.get.test4(projectId))
      }
  
    // @LINE:15
    case controllers_WikyController_test55_route(params) =>
      call(params.fromQuery[String]("projectId", None)) { (projectId) =>
        controllers_WikyController_test55_invoker.call(WikyController_8.get.test5(projectId))
      }
  
    // @LINE:16
    case controllers_WikyController_test66_route(params) =>
      call { 
        controllers_WikyController_test66_invoker.call(WikyController_8.get.test6())
      }
  
    // @LINE:21
    case controllers_WebSocketController_Incoming_homer_connection7_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_WebSocketController_Incoming_homer_connection7_invoker.call(WebSocketController_Incoming_0.get.homer_connection(id))
      }
  
    // @LINE:22
    case controllers_WebSocketController_Incoming_mobile_connection8_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_WebSocketController_Incoming_mobile_connection8_invoker.call(WebSocketController_Incoming_0.get.mobile_connection(id))
      }
  
    // @LINE:24
    case controllers_WebSocketController_Incoming_getWebSocketStats9_route(params) =>
      call { 
        controllers_WebSocketController_Incoming_getWebSocketStats9_invoker.call(WebSocketController_Incoming_0.get.getWebSocketStats())
      }
  
    // @LINE:25
    case controllers_WebSocketController_Incoming_sendTo10_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_WebSocketController_Incoming_sendTo10_invoker.call(WebSocketController_Incoming_0.get.sendTo(id))
      }
  
    // @LINE:30
    case controllers_SecurityController_login11_route(params) =>
      call { 
        controllers_SecurityController_login11_invoker.call(SecurityController_4.get.login())
      }
  
    // @LINE:31
    case controllers_SecurityController_logout12_route(params) =>
      call { 
        controllers_SecurityController_logout12_invoker.call(SecurityController_4.get.logout)
      }
  
    // @LINE:33
    case controllers_SecurityController_Facebook13_route(params) =>
      call(params.fromQuery[String]("returnLink", None)) { (returnLink) =>
        controllers_SecurityController_Facebook13_invoker.call(SecurityController_4.get.Facebook(returnLink))
      }
  
    // @LINE:34
    case controllers_SecurityController_Twitter14_route(params) =>
      call(params.fromQuery[String]("returnLink", None)) { (returnLink) =>
        controllers_SecurityController_Twitter14_invoker.call(SecurityController_4.get.Twitter(returnLink))
      }
  
    // @LINE:35
    case controllers_SecurityController_GitHub15_route(params) =>
      call(params.fromQuery[String]("returnLink", None)) { (returnLink) =>
        controllers_SecurityController_GitHub15_invoker.call(SecurityController_4.get.GitHub(returnLink))
      }
  
    // @LINE:36
    case controllers_SecurityController_Vkontakte16_route(params) =>
      call(params.fromQuery[String]("returnLink", None)) { (returnLink) =>
        controllers_SecurityController_Vkontakte16_invoker.call(SecurityController_4.get.Vkontakte(returnLink))
      }
  
    // @LINE:38
    case controllers_SecurityController_getPersonByToken17_route(params) =>
      call { 
        controllers_SecurityController_getPersonByToken17_invoker.call(SecurityController_4.get.getPersonByToken())
      }
  
    // @LINE:40
    case controllers_SecurityController_GET_facebook_oauth18_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_SecurityController_GET_facebook_oauth18_invoker.call(SecurityController_4.get.GET_facebook_oauth(url))
      }
  
    // @LINE:41
    case controllers_SecurityController_GET_github_oauth19_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_SecurityController_GET_github_oauth19_invoker.call(SecurityController_4.get.GET_github_oauth(url))
      }
  
    // @LINE:45
    case controllers_PermissionController_add_Permission_Person20_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("permission_id", None)) { (person_id, permission_id) =>
        controllers_PermissionController_add_Permission_Person20_invoker.call(PermissionController_1.get.add_Permission_Person(person_id, permission_id))
      }
  
    // @LINE:46
    case controllers_PermissionController_remove_Permission_Person21_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("permission_id", None)) { (person_id, permission_id) =>
        controllers_PermissionController_remove_Permission_Person21_invoker.call(PermissionController_1.get.remove_Permission_Person(person_id, permission_id))
      }
  
    // @LINE:47
    case controllers_PermissionController_get_Permission_All22_route(params) =>
      call { 
        controllers_PermissionController_get_Permission_All22_invoker.call(PermissionController_1.get.get_Permission_All())
      }
  
    // @LINE:49
    case controllers_PermissionController_add_Permission_to_Role23_route(params) =>
      call(params.fromQuery[String]("permission_id", None), params.fromQuery[String]("role_id", None)) { (permission_id, role_id) =>
        controllers_PermissionController_add_Permission_to_Role23_invoker.call(PermissionController_1.get.add_Permission_to_Role(permission_id, role_id))
      }
  
    // @LINE:50
    case controllers_PermissionController_get_Permission_in_Group24_route(params) =>
      call(params.fromQuery[String]("role_id", None)) { (role_id) =>
        controllers_PermissionController_get_Permission_in_Group24_invoker.call(PermissionController_1.get.get_Permission_in_Group(role_id))
      }
  
    // @LINE:51
    case controllers_PermissionController_remove_Permission_from_Role25_route(params) =>
      call(params.fromQuery[String]("permission_id", None), params.fromQuery[String]("role_id", None)) { (permission_id, role_id) =>
        controllers_PermissionController_remove_Permission_from_Role25_invoker.call(PermissionController_1.get.remove_Permission_from_Role(permission_id, role_id))
      }
  
    // @LINE:53
    case controllers_PermissionController_new_Role26_route(params) =>
      call { 
        controllers_PermissionController_new_Role26_invoker.call(PermissionController_1.get.new_Role())
      }
  
    // @LINE:54
    case controllers_PermissionController_delete_Role27_route(params) =>
      call(params.fromQuery[String]("role_id", None)) { (role_id) =>
        controllers_PermissionController_delete_Role27_invoker.call(PermissionController_1.get.delete_Role(role_id))
      }
  
    // @LINE:56
    case controllers_PermissionController_add_Role_Person28_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("role_id", None)) { (person_id, role_id) =>
        controllers_PermissionController_add_Role_Person28_invoker.call(PermissionController_1.get.add_Role_Person(person_id, role_id))
      }
  
    // @LINE:57
    case controllers_PermissionController_remove_Role_Person29_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("role_id", None)) { (person_id, role_id) =>
        controllers_PermissionController_remove_Role_Person29_invoker.call(PermissionController_1.get.remove_Role_Person(person_id, role_id))
      }
  
    // @LINE:58
    case controllers_PermissionController_get_Role_All30_route(params) =>
      call { 
        controllers_PermissionController_get_Role_All30_invoker.call(PermissionController_1.get.get_Role_All())
      }
  
    // @LINE:60
    case controllers_PermissionController_get_System_Acces31_route(params) =>
      call(params.fromQuery[String]("person_id", None)) { (person_id) =>
        controllers_PermissionController_get_System_Acces31_invoker.call(PermissionController_1.get.get_System_Acces(person_id))
      }
  
    // @LINE:68
    case controllers_PersonController_developerRegistration32_route(params) =>
      call { 
        controllers_PersonController_developerRegistration32_invoker.call(PersonController_2.get.developerRegistration())
      }
  
    // @LINE:69
    case controllers_PersonController_registred_Person33_route(params) =>
      call { 
        controllers_PersonController_registred_Person33_invoker.call(PersonController_2.get.registred_Person())
      }
  
    // @LINE:70
    case controllers_PersonController_edit_Person_Information34_route(params) =>
      call { 
        controllers_PersonController_edit_Person_Information34_invoker.call(PersonController_2.get.edit_Person_Information())
      }
  
    // @LINE:71
    case controllers_PersonController_getPerson35_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonController_getPerson35_invoker.call(PersonController_2.get.getPerson(id))
      }
  
    // @LINE:73
    case controllers_PersonController_deletePerson36_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonController_deletePerson36_invoker.call(PersonController_2.get.deletePerson(id))
      }
  
    // @LINE:75
    case controllers_PersonController_email_Person_authentitaction37_route(params) =>
      call(params.fromQuery[String]("mail", None), params.fromQuery[String]("authToken", None)) { (mail, authToken) =>
        controllers_PersonController_email_Person_authentitaction37_invoker.call(PersonController_2.get.email_Person_authentitaction(mail, authToken))
      }
  
    // @LINE:87
    case controllers_OverFlowController_newPost38_route(params) =>
      call { 
        controllers_OverFlowController_newPost38_invoker.call(OverFlowController_10.get.newPost())
      }
  
    // @LINE:88
    case controllers_OverFlowController_getPost39_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPost39_invoker.call(OverFlowController_10.get.getPost(id))
      }
  
    // @LINE:89
    case controllers_OverFlowController_deletePost40_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost40_invoker.call(OverFlowController_10.get.deletePost(id))
      }
  
    // @LINE:90
    case controllers_OverFlowController_editPost41_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_editPost41_invoker.call(OverFlowController_10.get.editPost(id))
      }
  
    // @LINE:91
    case controllers_OverFlowController_getPostByFilter42_route(params) =>
      call { 
        controllers_OverFlowController_getPostByFilter42_invoker.call(OverFlowController_10.get.getPostByFilter())
      }
  
    // @LINE:92
    case controllers_OverFlowController_getPostLinkedAnswers43_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPostLinkedAnswers43_invoker.call(OverFlowController_10.get.getPostLinkedAnswers(id))
      }
  
    // @LINE:94
    case controllers_OverFlowController_hashTagsListOnPost44_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_hashTagsListOnPost44_invoker.call(OverFlowController_10.get.hashTagsListOnPost(id))
      }
  
    // @LINE:95
    case controllers_OverFlowController_commentsListOnPost45_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_commentsListOnPost45_invoker.call(OverFlowController_10.get.commentsListOnPost(id))
      }
  
    // @LINE:96
    case controllers_OverFlowController_answereListOnPost46_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_answereListOnPost46_invoker.call(OverFlowController_10.get.answereListOnPost(id))
      }
  
    // @LINE:97
    case controllers_OverFlowController_textOfPost47_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_textOfPost47_invoker.call(OverFlowController_10.get.textOfPost(id))
      }
  
    // @LINE:99
    case controllers_OverFlowController_newTypeOfPost48_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfPost48_invoker.call(OverFlowController_10.get.newTypeOfPost())
      }
  
    // @LINE:100
    case controllers_OverFlowController_getTypeOfPost49_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfPost49_invoker.call(OverFlowController_10.get.getTypeOfPost())
      }
  
    // @LINE:102
    case controllers_OverFlowController_newTypeOfConfirms50_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfConfirms50_invoker.call(OverFlowController_10.get.newTypeOfConfirms())
      }
  
    // @LINE:103
    case controllers_OverFlowController_getTypeOfConfirms51_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfConfirms51_invoker.call(OverFlowController_10.get.getTypeOfConfirms())
      }
  
    // @LINE:104
    case controllers_OverFlowController_putTypeOfConfirmToPost52_route(params) =>
      call(params.fromPath[String]("conf", None), params.fromPath[String]("pst", None)) { (conf, pst) =>
        controllers_OverFlowController_putTypeOfConfirmToPost52_invoker.call(OverFlowController_10.get.putTypeOfConfirmToPost(conf, pst))
      }
  
    // @LINE:106
    case controllers_OverFlowController_addComment53_route(params) =>
      call { 
        controllers_OverFlowController_addComment53_invoker.call(OverFlowController_10.get.addComment())
      }
  
    // @LINE:107
    case controllers_OverFlowController_updateComment54_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment54_invoker.call(OverFlowController_10.get.updateComment(id))
      }
  
    // @LINE:108
    case controllers_OverFlowController_deletePost55_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost55_invoker.call(OverFlowController_10.get.deletePost(id))
      }
  
    // @LINE:110
    case controllers_OverFlowController_addAnswer56_route(params) =>
      call { 
        controllers_OverFlowController_addAnswer56_invoker.call(OverFlowController_10.get.addAnswer())
      }
  
    // @LINE:111
    case controllers_OverFlowController_updateComment57_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment57_invoker.call(OverFlowController_10.get.updateComment(id))
      }
  
    // @LINE:112
    case controllers_OverFlowController_deletePost58_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost58_invoker.call(OverFlowController_10.get.deletePost(id))
      }
  
    // @LINE:114
    case controllers_OverFlowController_likePlus59_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likePlus59_invoker.call(OverFlowController_10.get.likePlus(id))
      }
  
    // @LINE:115
    case controllers_OverFlowController_likeMinus60_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likeMinus60_invoker.call(OverFlowController_10.get.likeMinus(id))
      }
  
    // @LINE:116
    case controllers_OverFlowController_linkWithPreviousAnswer61_route(params) =>
      call { 
        controllers_OverFlowController_linkWithPreviousAnswer61_invoker.call(OverFlowController_10.get.linkWithPreviousAnswer())
      }
  
    // @LINE:117
    case controllers_OverFlowController_unlinkWithPreviousAnswer62_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_unlinkWithPreviousAnswer62_invoker.call(OverFlowController_10.get.unlinkWithPreviousAnswer(id))
      }
  
    // @LINE:118
    case controllers_OverFlowController_removeHashTag63_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag63_invoker.call(OverFlowController_10.get.removeHashTag())
      }
  
    // @LINE:119
    case controllers_OverFlowController_addHashTag64_route(params) =>
      call { 
        controllers_OverFlowController_addHashTag64_invoker.call(OverFlowController_10.get.addHashTag())
      }
  
    // @LINE:120
    case controllers_OverFlowController_removeHashTag65_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag65_invoker.call(OverFlowController_10.get.removeHashTag())
      }
  
    // @LINE:129
    case controllers_ProgramingPackageController_postNewProject66_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProject66_invoker.call(ProgramingPackageController_5.get.postNewProject())
      }
  
    // @LINE:130
    case controllers_ProgramingPackageController_updateProject67_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateProject67_invoker.call(ProgramingPackageController_5.get.updateProject(id))
      }
  
    // @LINE:131
    case controllers_ProgramingPackageController_getProject68_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProject68_invoker.call(ProgramingPackageController_5.get.getProject(id))
      }
  
    // @LINE:132
    case controllers_ProgramingPackageController_getProjectsByUserAccount69_route(params) =>
      call { 
        controllers_ProgramingPackageController_getProjectsByUserAccount69_invoker.call(ProgramingPackageController_5.get.getProjectsByUserAccount())
      }
  
    // @LINE:133
    case controllers_ProgramingPackageController_deleteProject70_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteProject70_invoker.call(ProgramingPackageController_5.get.deleteProject(id))
      }
  
    // @LINE:134
    case controllers_ProgramingPackageController_shareProjectWithUsers71_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_shareProjectWithUsers71_invoker.call(ProgramingPackageController_5.get.shareProjectWithUsers(id))
      }
  
    // @LINE:135
    case controllers_ProgramingPackageController_unshareProjectWithUsers72_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_unshareProjectWithUsers72_invoker.call(ProgramingPackageController_5.get.unshareProjectWithUsers(id))
      }
  
    // @LINE:136
    case controllers_ProgramingPackageController_getAll_b_Programs73_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAll_b_Programs73_invoker.call(ProgramingPackageController_5.get.getAll_b_Programs(id))
      }
  
    // @LINE:137
    case controllers_ProgramingPackageController_getAll_c_Programs74_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAll_c_Programs74_invoker.call(ProgramingPackageController_5.get.getAll_c_Programs(id))
      }
  
    // @LINE:138
    case controllers_ProgramingPackageController_getAll_m_Projects75_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAll_m_Projects75_invoker.call(ProgramingPackageController_5.get.getAll_m_Projects(id))
      }
  
    // @LINE:140
    case controllers_ProgramingPackageController_getProgramhomerList76_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramhomerList76_invoker.call(ProgramingPackageController_5.get.getProgramhomerList(id))
      }
  
    // @LINE:141
    case controllers_ProgramingPackageController_getProjectOwners77_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectOwners77_invoker.call(ProgramingPackageController_5.get.getProjectOwners(id))
      }
  
    // @LINE:144
    case controllers_ProgramingPackageController_newHomer78_route(params) =>
      call { 
        controllers_ProgramingPackageController_newHomer78_invoker.call(ProgramingPackageController_5.get.newHomer())
      }
  
    // @LINE:145
    case controllers_ProgramingPackageController_removeHomer79_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeHomer79_invoker.call(ProgramingPackageController_5.get.removeHomer(id))
      }
  
    // @LINE:146
    case controllers_ProgramingPackageController_getHomer80_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getHomer80_invoker.call(ProgramingPackageController_5.get.getHomer(id))
      }
  
    // @LINE:147
    case controllers_ProgramingPackageController_getAllHomers81_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllHomers81_invoker.call(ProgramingPackageController_5.get.getAllHomers())
      }
  
    // @LINE:148
    case controllers_ProgramingPackageController_getConnectedHomers82_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getConnectedHomers82_invoker.call(ProgramingPackageController_5.get.getConnectedHomers(id))
      }
  
    // @LINE:152
    case controllers_ProgramingPackageController_connectHomerWithProject83_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectHomerWithProject83_invoker.call(ProgramingPackageController_5.get.connectHomerWithProject())
      }
  
    // @LINE:153
    case controllers_ProgramingPackageController_disconnectHomerWithProject84_route(params) =>
      call { 
        controllers_ProgramingPackageController_disconnectHomerWithProject84_invoker.call(ProgramingPackageController_5.get.disconnectHomerWithProject())
      }
  
    // @LINE:157
    case controllers_ProgramingPackageController_postNewBProgram85_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewBProgram85_invoker.call(ProgramingPackageController_5.get.postNewBProgram())
      }
  
    // @LINE:158
    case controllers_ProgramingPackageController_getProgram86_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgram86_invoker.call(ProgramingPackageController_5.get.getProgram(id))
      }
  
    // @LINE:159
    case controllers_ProgramingPackageController_editProgram87_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editProgram87_invoker.call(ProgramingPackageController_5.get.editProgram(id))
      }
  
    // @LINE:160
    case controllers_ProgramingPackageController_update_b_program88_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_update_b_program88_invoker.call(ProgramingPackageController_5.get.update_b_program(id))
      }
  
    // @LINE:161
    case controllers_ProgramingPackageController_remove_b_Program89_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_remove_b_Program89_invoker.call(ProgramingPackageController_5.get.remove_b_Program(id))
      }
  
    // @LINE:162
    case controllers_ProgramingPackageController_getProgramInString90_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInString90_invoker.call(ProgramingPackageController_5.get.getProgramInString(id))
      }
  
    // @LINE:163
    case controllers_ProgramingPackageController_uploadProgramToHomer_Immediately91_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("ver", None)) { (id, ver) =>
        controllers_ProgramingPackageController_uploadProgramToHomer_Immediately91_invoker.call(ProgramingPackageController_5.get.uploadProgramToHomer_Immediately(id, ver))
      }
  
    // @LINE:164
    case controllers_ProgramingPackageController_uploadProgramToCloud92_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("ver", None)) { (id, ver) =>
        controllers_ProgramingPackageController_uploadProgramToCloud92_invoker.call(ProgramingPackageController_5.get.uploadProgramToCloud(id, ver))
      }
  
    // @LINE:166
    case controllers_ProgramingPackageController_listOfUploadedHomers93_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfUploadedHomers93_invoker.call(ProgramingPackageController_5.get.listOfUploadedHomers(id))
      }
  
    // @LINE:167
    case controllers_ProgramingPackageController_listOfHomersWaitingForUpload94_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfHomersWaitingForUpload94_invoker.call(ProgramingPackageController_5.get.listOfHomersWaitingForUpload(id))
      }
  
    // @LINE:168
    case controllers_ProgramingPackageController_getProjectsBoard95_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectsBoard95_invoker.call(ProgramingPackageController_5.get.getProjectsBoard(id))
      }
  
    // @LINE:171
    case controllers_ProgramingPackageController_newBlock96_route(params) =>
      call { 
        controllers_ProgramingPackageController_newBlock96_invoker.call(ProgramingPackageController_5.get.newBlock())
      }
  
    // @LINE:172
    case controllers_ProgramingPackageController_updateOfBlock97_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateOfBlock97_invoker.call(ProgramingPackageController_5.get.updateOfBlock(id))
      }
  
    // @LINE:173
    case controllers_ProgramingPackageController_editBlock98_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editBlock98_invoker.call(ProgramingPackageController_5.get.editBlock(id))
      }
  
    // @LINE:174
    case controllers_ProgramingPackageController_getBlockBlock99_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getBlockBlock99_invoker.call(ProgramingPackageController_5.get.getBlockBlock(id))
      }
  
    // @LINE:175
    case controllers_ProgramingPackageController_getBlockVersions100_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getBlockVersions100_invoker.call(ProgramingPackageController_5.get.getBlockVersions(id))
      }
  
    // @LINE:178
    case controllers_ProgramingPackageController_allPrevVersions101_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_allPrevVersions101_invoker.call(ProgramingPackageController_5.get.allPrevVersions(id))
      }
  
    // @LINE:179
    case controllers_ProgramingPackageController_deleteBlockVersion102_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteBlockVersion102_invoker.call(ProgramingPackageController_5.get.deleteBlockVersion(id))
      }
  
    // @LINE:180
    case controllers_ProgramingPackageController_deleteBlock103_route(params) =>
      call(params.fromQuery[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteBlock103_invoker.call(ProgramingPackageController_5.get.deleteBlock(id))
      }
  
    // @LINE:182
    case controllers_ProgramingPackageController_newTypeOfBlock104_route(params) =>
      call { 
        controllers_ProgramingPackageController_newTypeOfBlock104_invoker.call(ProgramingPackageController_5.get.newTypeOfBlock())
      }
  
    // @LINE:183
    case controllers_ProgramingPackageController_getByCategory105_route(params) =>
      call { 
        controllers_ProgramingPackageController_getByCategory105_invoker.call(ProgramingPackageController_5.get.getByCategory())
      }
  
    // @LINE:184
    case controllers_ProgramingPackageController_editTypeOfBlock106_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editTypeOfBlock106_invoker.call(ProgramingPackageController_5.get.editTypeOfBlock(id))
      }
  
    // @LINE:185
    case controllers_ProgramingPackageController_getAllTypeOfBlocks107_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllTypeOfBlocks107_invoker.call(ProgramingPackageController_5.get.getAllTypeOfBlocks())
      }
  
    // @LINE:186
    case controllers_ProgramingPackageController_deleteTypeOfBlock108_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteTypeOfBlock108_invoker.call(ProgramingPackageController_5.get.deleteTypeOfBlock(id))
      }
  
    // @LINE:198
    case controllers_CompilationLibrariesController_create_C_Program109_route(params) =>
      call(params.fromPath[String]("project_id", None)) { (project_id) =>
        controllers_CompilationLibrariesController_create_C_Program109_invoker.call(CompilationLibrariesController_7.get.create_C_Program(project_id))
      }
  
    // @LINE:199
    case controllers_CompilationLibrariesController_get_C_Program110_route(params) =>
      call(params.fromPath[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_get_C_Program110_invoker.call(CompilationLibrariesController_7.get.get_C_Program(c_program_id))
      }
  
    // @LINE:200
    case controllers_CompilationLibrariesController_get_C_Program_All_from_Project111_route(params) =>
      call(params.fromPath[String]("project_id", None)) { (project_id) =>
        controllers_CompilationLibrariesController_get_C_Program_All_from_Project111_invoker.call(CompilationLibrariesController_7.get.get_C_Program_All_from_Project(project_id))
      }
  
    // @LINE:202
    case controllers_CompilationLibrariesController_edit_C_Program_Description112_route(params) =>
      call(params.fromPath[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_edit_C_Program_Description112_invoker.call(CompilationLibrariesController_7.get.edit_C_Program_Description(c_program_id))
      }
  
    // @LINE:203
    case controllers_CompilationLibrariesController_update_C_Program113_route(params) =>
      call(params.fromPath[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_update_C_Program113_invoker.call(CompilationLibrariesController_7.get.update_C_Program(c_program_id))
      }
  
    // @LINE:205
    case controllers_CompilationLibrariesController_delete_C_Program114_route(params) =>
      call(params.fromPath[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_delete_C_Program114_invoker.call(CompilationLibrariesController_7.get.delete_C_Program(c_program_id))
      }
  
    // @LINE:206
    case controllers_CompilationLibrariesController_delete_C_Program_Version115_route(params) =>
      call(params.fromPath[String]("c_program_id", None), params.fromPath[String]("version_id", None)) { (c_program_id, version_id) =>
        controllers_CompilationLibrariesController_delete_C_Program_Version115_invoker.call(CompilationLibrariesController_7.get.delete_C_Program_Version(c_program_id, version_id))
      }
  
    // @LINE:208
    case controllers_CompilationLibrariesController_generateProjectForEclipse116_route(params) =>
      call { 
        controllers_CompilationLibrariesController_generateProjectForEclipse116_invoker.call(CompilationLibrariesController_7.get.generateProjectForEclipse())
      }
  
    // @LINE:209
    case controllers_CompilationLibrariesController_uploadCompilationToBoard117_route(params) =>
      call(params.fromPath[String]("c_program_id", None), params.fromPath[String]("board_id", None)) { (c_program_id, board_id) =>
        controllers_CompilationLibrariesController_uploadCompilationToBoard117_invoker.call(CompilationLibrariesController_7.get.uploadCompilationToBoard(c_program_id, board_id))
      }
  
    // @LINE:210
    case controllers_CompilationLibrariesController_uploadBinaryFileToBoard118_route(params) =>
      call(params.fromPath[String]("board_id", None)) { (board_id) =>
        controllers_CompilationLibrariesController_uploadBinaryFileToBoard118_invoker.call(CompilationLibrariesController_7.get.uploadBinaryFileToBoard(board_id))
      }
  
    // @LINE:212
    case controllers_CompilationLibrariesController_get_Boards_from_Project119_route(params) =>
      call(params.fromPath[String]("project_id", None)) { (project_id) =>
        controllers_CompilationLibrariesController_get_Boards_from_Project119_invoker.call(CompilationLibrariesController_7.get.get_Boards_from_Project(project_id))
      }
  
    // @LINE:215
    case controllers_CompilationLibrariesController_new_Processor120_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_Processor120_invoker.call(CompilationLibrariesController_7.get.new_Processor())
      }
  
    // @LINE:216
    case controllers_CompilationLibrariesController_get_Processor121_route(params) =>
      call(params.fromPath[String]("processor_id", None)) { (processor_id) =>
        controllers_CompilationLibrariesController_get_Processor121_invoker.call(CompilationLibrariesController_7.get.get_Processor(processor_id))
      }
  
    // @LINE:217
    case controllers_CompilationLibrariesController_get_Processor_All122_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_Processor_All122_invoker.call(CompilationLibrariesController_7.get.get_Processor_All())
      }
  
    // @LINE:218
    case controllers_CompilationLibrariesController_update_Processor123_route(params) =>
      call(params.fromPath[String]("processor_id", None)) { (processor_id) =>
        controllers_CompilationLibrariesController_update_Processor123_invoker.call(CompilationLibrariesController_7.get.update_Processor(processor_id))
      }
  
    // @LINE:219
    case controllers_CompilationLibrariesController_delete_Processor124_route(params) =>
      call(params.fromPath[String]("processor_id", None)) { (processor_id) =>
        controllers_CompilationLibrariesController_delete_Processor124_invoker.call(CompilationLibrariesController_7.get.delete_Processor(processor_id))
      }
  
    // @LINE:221
    case controllers_CompilationLibrariesController_connectProcessorWithLibrary125_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibrary125_invoker.call(CompilationLibrariesController_7.get.connectProcessorWithLibrary(processor_id, library_id))
      }
  
    // @LINE:222
    case controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup126_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup126_invoker.call(CompilationLibrariesController_7.get.connectProcessorWithLibraryGroup(processor_id, library_id))
      }
  
    // @LINE:223
    case controllers_CompilationLibrariesController_disconnectProcessorWithLibrary127_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_disconnectProcessorWithLibrary127_invoker.call(CompilationLibrariesController_7.get.disconnectProcessorWithLibrary(processor_id, library_id))
      }
  
    // @LINE:224
    case controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup128_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup128_invoker.call(CompilationLibrariesController_7.get.disconnectProcessorWithLibraryGroup(processor_id, library_id))
      }
  
    // @LINE:226
    case controllers_CompilationLibrariesController_getProcessorLibraryGroups129_route(params) =>
      call(params.fromPath[String]("processor_id", None)) { (processor_id) =>
        controllers_CompilationLibrariesController_getProcessorLibraryGroups129_invoker.call(CompilationLibrariesController_7.get.getProcessorLibraryGroups(processor_id))
      }
  
    // @LINE:227
    case controllers_CompilationLibrariesController_getProcessorSingleLibraries130_route(params) =>
      call(params.fromPath[String]("processor_id", None)) { (processor_id) =>
        controllers_CompilationLibrariesController_getProcessorSingleLibraries130_invoker.call(CompilationLibrariesController_7.get.getProcessorSingleLibraries(processor_id))
      }
  
    // @LINE:230
    case controllers_CompilationLibrariesController_new_Board131_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_Board131_invoker.call(CompilationLibrariesController_7.get.new_Board())
      }
  
    // @LINE:231
    case controllers_CompilationLibrariesController_edit_Board_User_Description132_route(params) =>
      call(params.fromPath[String]("type_of_board_id", None)) { (type_of_board_id) =>
        controllers_CompilationLibrariesController_edit_Board_User_Description132_invoker.call(CompilationLibrariesController_7.get.edit_Board_User_Description(type_of_board_id))
      }
  
    // @LINE:232
    case controllers_CompilationLibrariesController_get_Board_Filter133_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_Board_Filter133_invoker.call(CompilationLibrariesController_7.get.get_Board_Filter())
      }
  
    // @LINE:233
    case controllers_CompilationLibrariesController_get_Board134_route(params) =>
      call(params.fromPath[String]("board_id", None)) { (board_id) =>
        controllers_CompilationLibrariesController_get_Board134_invoker.call(CompilationLibrariesController_7.get.get_Board(board_id))
      }
  
    // @LINE:234
    case controllers_CompilationLibrariesController_deactivate_Board135_route(params) =>
      call(params.fromPath[String]("board_id", None)) { (board_id) =>
        controllers_CompilationLibrariesController_deactivate_Board135_invoker.call(CompilationLibrariesController_7.get.deactivate_Board(board_id))
      }
  
    // @LINE:235
    case controllers_CompilationLibrariesController_connect_Board_with_Project136_route(params) =>
      call(params.fromPath[String]("board_id", None), params.fromPath[String]("project_id", None)) { (board_id, project_id) =>
        controllers_CompilationLibrariesController_connect_Board_with_Project136_invoker.call(CompilationLibrariesController_7.get.connect_Board_with_Project(board_id, project_id))
      }
  
    // @LINE:236
    case controllers_CompilationLibrariesController_disconnect_Board_from_Project137_route(params) =>
      call(params.fromPath[String]("board_id", None), params.fromPath[String]("project_id", None)) { (board_id, project_id) =>
        controllers_CompilationLibrariesController_disconnect_Board_from_Project137_invoker.call(CompilationLibrariesController_7.get.disconnect_Board_from_Project(board_id, project_id))
      }
  
    // @LINE:237
    case controllers_CompilationLibrariesController_getBoardProjects138_route(params) =>
      call(params.fromPath[String]("board_id", None)) { (board_id) =>
        controllers_CompilationLibrariesController_getBoardProjects138_invoker.call(CompilationLibrariesController_7.get.getBoardProjects(board_id))
      }
  
    // @LINE:241
    case controllers_CompilationLibrariesController_new_Producer139_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_Producer139_invoker.call(CompilationLibrariesController_7.get.new_Producer())
      }
  
    // @LINE:242
    case controllers_CompilationLibrariesController_edit_Producer140_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_edit_Producer140_invoker.call(CompilationLibrariesController_7.get.edit_Producer(producer_id))
      }
  
    // @LINE:243
    case controllers_CompilationLibrariesController_get_Producers141_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_Producers141_invoker.call(CompilationLibrariesController_7.get.get_Producers())
      }
  
    // @LINE:244
    case controllers_CompilationLibrariesController_get_Producer142_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_get_Producer142_invoker.call(CompilationLibrariesController_7.get.get_Producer(producer_id))
      }
  
    // @LINE:245
    case controllers_CompilationLibrariesController_get_Producer_Description143_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_get_Producer_Description143_invoker.call(CompilationLibrariesController_7.get.get_Producer_Description(producer_id))
      }
  
    // @LINE:246
    case controllers_CompilationLibrariesController_get_Producer_TypeOfBoards144_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_get_Producer_TypeOfBoards144_invoker.call(CompilationLibrariesController_7.get.get_Producer_TypeOfBoards(producer_id))
      }
  
    // @LINE:249
    case controllers_CompilationLibrariesController_new_TypeOfBoard145_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_TypeOfBoard145_invoker.call(CompilationLibrariesController_7.get.new_TypeOfBoard())
      }
  
    // @LINE:250
    case controllers_CompilationLibrariesController_edit_TypeOfBoard146_route(params) =>
      call(params.fromQuery[String]("type_of_board_id", None)) { (type_of_board_id) =>
        controllers_CompilationLibrariesController_edit_TypeOfBoard146_invoker.call(CompilationLibrariesController_7.get.edit_TypeOfBoard(type_of_board_id))
      }
  
    // @LINE:251
    case controllers_CompilationLibrariesController_delete_TypeOfBoard147_route(params) =>
      call(params.fromQuery[String]("type_of_board_id", None)) { (type_of_board_id) =>
        controllers_CompilationLibrariesController_delete_TypeOfBoard147_invoker.call(CompilationLibrariesController_7.get.delete_TypeOfBoard(type_of_board_id))
      }
  
    // @LINE:252
    case controllers_CompilationLibrariesController_get_TypeOfBoard_all148_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_TypeOfBoard_all148_invoker.call(CompilationLibrariesController_7.get.get_TypeOfBoard_all())
      }
  
    // @LINE:253
    case controllers_CompilationLibrariesController_get_TypeOfBoard149_route(params) =>
      call(params.fromQuery[String]("type_of_board_id", None)) { (type_of_board_id) =>
        controllers_CompilationLibrariesController_get_TypeOfBoard149_invoker.call(CompilationLibrariesController_7.get.get_TypeOfBoard(type_of_board_id))
      }
  
    // @LINE:254
    case controllers_CompilationLibrariesController_get_TypeOfBoard_Description150_route(params) =>
      call(params.fromQuery[String]("type_of_board_id", None)) { (type_of_board_id) =>
        controllers_CompilationLibrariesController_get_TypeOfBoard_Description150_invoker.call(CompilationLibrariesController_7.get.get_TypeOfBoard_Description(type_of_board_id))
      }
  
    // @LINE:255
    case controllers_CompilationLibrariesController_getTypeOfBoardAllBoards151_route(params) =>
      call(params.fromQuery[String]("type_of_board_id", None)) { (type_of_board_id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardAllBoards151_invoker.call(CompilationLibrariesController_7.get.getTypeOfBoardAllBoards(type_of_board_id))
      }
  
    // @LINE:258
    case controllers_CompilationLibrariesController_new_LibraryGroup152_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_LibraryGroup152_invoker.call(CompilationLibrariesController_7.get.new_LibraryGroup())
      }
  
    // @LINE:259
    case controllers_CompilationLibrariesController_get_LibraryGroup153_route(params) =>
      call(params.fromPath[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup153_invoker.call(CompilationLibrariesController_7.get.get_LibraryGroup(libraryGroup_id))
      }
  
    // @LINE:260
    case controllers_CompilationLibrariesController_delete_LibraryGroup154_route(params) =>
      call(params.fromPath[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_delete_LibraryGroup154_invoker.call(CompilationLibrariesController_7.get.delete_LibraryGroup(libraryGroup_id))
      }
  
    // @LINE:261
    case controllers_CompilationLibrariesController_get_LibraryGroup_Filter155_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_LibraryGroup_Filter155_invoker.call(CompilationLibrariesController_7.get.get_LibraryGroup_Filter())
      }
  
    // @LINE:262
    case controllers_CompilationLibrariesController_editLibraryGroup156_route(params) =>
      call(params.fromPath[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_editLibraryGroup156_invoker.call(CompilationLibrariesController_7.get.editLibraryGroup(libraryGroup_id))
      }
  
    // @LINE:263
    case controllers_CompilationLibrariesController_get_LibraryGroup_Description157_route(params) =>
      call(params.fromPath[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Description157_invoker.call(CompilationLibrariesController_7.get.get_LibraryGroup_Description(libraryGroup_id))
      }
  
    // @LINE:264
    case controllers_CompilationLibrariesController_get_LibraryGroup_Processors158_route(params) =>
      call(params.fromPath[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Processors158_invoker.call(CompilationLibrariesController_7.get.get_LibraryGroup_Processors(libraryGroup_id))
      }
  
    // @LINE:265
    case controllers_CompilationLibrariesController_get_LibraryGroup_Libraries159_route(params) =>
      call(params.fromPath[String]("libraryGroup_id", None), params.fromPath[String]("version_id", None)) { (libraryGroup_id, version_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Libraries159_invoker.call(CompilationLibrariesController_7.get.get_LibraryGroup_Libraries(libraryGroup_id, version_id))
      }
  
    // @LINE:266
    case controllers_CompilationLibrariesController_new_LibraryGroup_Version160_route(params) =>
      call(params.fromPath[String]("version_id", None)) { (version_id) =>
        controllers_CompilationLibrariesController_new_LibraryGroup_Version160_invoker.call(CompilationLibrariesController_7.get.new_LibraryGroup_Version(version_id))
      }
  
    // @LINE:267
    case controllers_CompilationLibrariesController_get_LibraryGroup_Version161_route(params) =>
      call(params.fromPath[String]("version_id", None)) { (version_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Version161_invoker.call(CompilationLibrariesController_7.get.get_LibraryGroup_Version(version_id))
      }
  
    // @LINE:268
    case controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup162_route(params) =>
      call(params.fromPath[String]("libraryGroup_id", None), params.fromPath[String]("version_id", None)) { (libraryGroup_id, version_id) =>
        controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup162_invoker.call(CompilationLibrariesController_7.get.upload_Library_To_LibraryGroup(libraryGroup_id, version_id))
      }
  
    // @LINE:271
    case controllers_CompilationLibrariesController_new_SingleLibrary163_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_SingleLibrary163_invoker.call(CompilationLibrariesController_7.get.new_SingleLibrary())
      }
  
    // @LINE:272
    case controllers_CompilationLibrariesController_new_SingleLibrary_Version164_route(params) =>
      call(params.fromPath[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_new_SingleLibrary_Version164_invoker.call(CompilationLibrariesController_7.get.new_SingleLibrary_Version(library_id))
      }
  
    // @LINE:273
    case controllers_CompilationLibrariesController_get_SingleLibrary_Versions165_route(params) =>
      call(params.fromPath[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_get_SingleLibrary_Versions165_invoker.call(CompilationLibrariesController_7.get.get_SingleLibrary_Versions(library_id))
      }
  
    // @LINE:274
    case controllers_CompilationLibrariesController_get_SingleLibrary_Filter166_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_SingleLibrary_Filter166_invoker.call(CompilationLibrariesController_7.get.get_SingleLibrary_Filter())
      }
  
    // @LINE:275
    case controllers_CompilationLibrariesController_get_SingleLibrary167_route(params) =>
      call(params.fromPath[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_get_SingleLibrary167_invoker.call(CompilationLibrariesController_7.get.get_SingleLibrary(library_id))
      }
  
    // @LINE:277
    case controllers_CompilationLibrariesController_edit_SingleLibrary168_route(params) =>
      call(params.fromPath[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_edit_SingleLibrary168_invoker.call(CompilationLibrariesController_7.get.edit_SingleLibrary(library_id))
      }
  
    // @LINE:278
    case controllers_CompilationLibrariesController_delete_SingleLibrary169_route(params) =>
      call(params.fromPath[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_delete_SingleLibrary169_invoker.call(CompilationLibrariesController_7.get.delete_SingleLibrary(library_id))
      }
  
    // @LINE:279
    case controllers_CompilationLibrariesController_upload_SingleLibrary_Version170_route(params) =>
      call(params.fromPath[String]("library_id", None), params.fromPath[String]("version_id", None)) { (library_id, version_id) =>
        controllers_CompilationLibrariesController_upload_SingleLibrary_Version170_invoker.call(CompilationLibrariesController_7.get.upload_SingleLibrary_Version(library_id, version_id))
      }
  
    // @LINE:282
    case controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries171_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries171_invoker.call(CompilationLibrariesController_7.get.get_LibraryGroup_Version_Libraries(id))
      }
  
    // @LINE:283
    case controllers_CompilationLibrariesController_fileRecord172_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_fileRecord172_invoker.call(CompilationLibrariesController_7.get.fileRecord(id))
      }
  
    // @LINE:290
    case controllers_GridController_new_M_Project173_route(params) =>
      call(params.fromQuery[String]("project_id", None)) { (project_id) =>
        controllers_GridController_new_M_Project173_invoker.call(GridController_6.get.new_M_Project(project_id))
      }
  
    // @LINE:291
    case controllers_GridController_get_M_Projects_ByLoggedPerson174_route(params) =>
      call { 
        controllers_GridController_get_M_Projects_ByLoggedPerson174_invoker.call(GridController_6.get.get_M_Projects_ByLoggedPerson())
      }
  
    // @LINE:292
    case controllers_GridController_get_M_Project175_route(params) =>
      call(params.fromPath[String]("m_project_id", None)) { (m_project_id) =>
        controllers_GridController_get_M_Project175_invoker.call(GridController_6.get.get_M_Project(m_project_id))
      }
  
    // @LINE:293
    case controllers_GridController_edit_M_Project176_route(params) =>
      call(params.fromPath[String]("m_project_id", None)) { (m_project_id) =>
        controllers_GridController_edit_M_Project176_invoker.call(GridController_6.get.edit_M_Project(m_project_id))
      }
  
    // @LINE:294
    case controllers_GridController_remove_M_Project177_route(params) =>
      call(params.fromPath[String]("m_project_id", None)) { (m_project_id) =>
        controllers_GridController_remove_M_Project177_invoker.call(GridController_6.get.remove_M_Project(m_project_id))
      }
  
    // @LINE:296
    case controllers_GridController_get_M_Projects_from_GlobalProject178_route(params) =>
      call(params.fromPath[String]("project_id", None)) { (project_id) =>
        controllers_GridController_get_M_Projects_from_GlobalProject178_invoker.call(GridController_6.get.get_M_Projects_from_GlobalProject(project_id))
      }
  
    // @LINE:300
    case controllers_GridController_new_M_Program179_route(params) =>
      call { 
        controllers_GridController_new_M_Program179_invoker.call(GridController_6.get.new_M_Program())
      }
  
    // @LINE:301
    case controllers_GridController_get_M_Program_byQR_Token_forMobile180_route(params) =>
      call(params.fromPath[String]("qr_token", None)) { (qr_token) =>
        controllers_GridController_get_M_Program_byQR_Token_forMobile180_invoker.call(GridController_6.get.get_M_Program_byQR_Token_forMobile(qr_token))
      }
  
    // @LINE:302
    case controllers_GridController_get_M_Program_all_forMobile181_route(params) =>
      call { 
        controllers_GridController_get_M_Program_all_forMobile181_invoker.call(GridController_6.get.get_M_Program_all_forMobile())
      }
  
    // @LINE:305
    case controllers_GridController_get_M_Program182_route(params) =>
      call(params.fromPath[String]("m_progrm_id", None)) { (m_progrm_id) =>
        controllers_GridController_get_M_Program182_invoker.call(GridController_6.get.get_M_Program(m_progrm_id))
      }
  
    // @LINE:306
    case controllers_GridController_edit_M_Program183_route(params) =>
      call(params.fromPath[String]("m_progrm_id", None)) { (m_progrm_id) =>
        controllers_GridController_edit_M_Program183_invoker.call(GridController_6.get.edit_M_Program(m_progrm_id))
      }
  
    // @LINE:307
    case controllers_GridController_remove_M_Program184_route(params) =>
      call(params.fromPath[String]("m_progrm_id", None)) { (m_progrm_id) =>
        controllers_GridController_remove_M_Program184_invoker.call(GridController_6.get.remove_M_Program(m_progrm_id))
      }
  
    // @LINE:313
    case controllers_GridController_new_Screen_Size_Type185_route(params) =>
      call { 
        controllers_GridController_new_Screen_Size_Type185_invoker.call(GridController_6.get.new_Screen_Size_Type())
      }
  
    // @LINE:314
    case controllers_GridController_get_Screen_Size_Type_Combination186_route(params) =>
      call { 
        controllers_GridController_get_Screen_Size_Type_Combination186_invoker.call(GridController_6.get.get_Screen_Size_Type_Combination())
      }
  
    // @LINE:315
    case controllers_GridController_get_Screen_Size_Type187_route(params) =>
      call(params.fromPath[String]("screen_size_type_id", None)) { (screen_size_type_id) =>
        controllers_GridController_get_Screen_Size_Type187_invoker.call(GridController_6.get.get_Screen_Size_Type(screen_size_type_id))
      }
  
    // @LINE:316
    case controllers_GridController_edit_Screen_Size_Type188_route(params) =>
      call(params.fromPath[String]("screen_size_type_id", None)) { (screen_size_type_id) =>
        controllers_GridController_edit_Screen_Size_Type188_invoker.call(GridController_6.get.edit_Screen_Size_Type(screen_size_type_id))
      }
  
    // @LINE:317
    case controllers_GridController_remove_Screen_Size_Type189_route(params) =>
      call(params.fromPath[String]("screen_size_type_id", None)) { (screen_size_type_id) =>
        controllers_GridController_remove_Screen_Size_Type189_invoker.call(GridController_6.get.remove_Screen_Size_Type(screen_size_type_id))
      }
  
    // @LINE:326
    case utilities_swagger_ApiHelpController_getResources190_route(params) =>
      call { 
        utilities_swagger_ApiHelpController_getResources190_invoker.call(ApiHelpController_3.get.getResources)
      }
  
    // @LINE:329
    case controllers_SecurityController_optionLink191_route(params) =>
      call(params.fromPath[String]("all", None)) { (all) =>
        controllers_SecurityController_optionLink191_invoker.call(SecurityController_4.get.optionLink(all))
      }
  
    // @LINE:332
    case controllers_Assets_at192_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at192_invoker.call(Assets_9.at(path, file))
      }
  }
}
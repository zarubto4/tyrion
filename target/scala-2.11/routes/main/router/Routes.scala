
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Fri Feb 19 20:16:14 CET 2016

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
  SecurityController_3: javax.inject.Provider[controllers.SecurityController],
  // @LINE:11
  WikyController_7: javax.inject.Provider[controllers.WikyController],
  // @LINE:21
  WebSocketController_Incoming_0: javax.inject.Provider[controllers.WebSocketController_Incoming],
  // @LINE:44
  PermissionController_1: javax.inject.Provider[controllers.PermissionController],
  // @LINE:66
  PersonCreateController_10: javax.inject.Provider[controllers.PersonCreateController],
  // @LINE:85
  OverFlowController_9: javax.inject.Provider[controllers.OverFlowController],
  // @LINE:127
  ProgramingPackageController_4: javax.inject.Provider[controllers.ProgramingPackageController],
  // @LINE:196
  CompilationLibrariesController_6: javax.inject.Provider[controllers.CompilationLibrariesController],
  // @LINE:288
  GridController_5: javax.inject.Provider[controllers.GridController],
  // @LINE:317
  ApiHelpController_2: javax.inject.Provider[utilities.swagger.ApiHelpController],
  // @LINE:323
  Assets_8: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:8
    SecurityController_3: javax.inject.Provider[controllers.SecurityController],
    // @LINE:11
    WikyController_7: javax.inject.Provider[controllers.WikyController],
    // @LINE:21
    WebSocketController_Incoming_0: javax.inject.Provider[controllers.WebSocketController_Incoming],
    // @LINE:44
    PermissionController_1: javax.inject.Provider[controllers.PermissionController],
    // @LINE:66
    PersonCreateController_10: javax.inject.Provider[controllers.PersonCreateController],
    // @LINE:85
    OverFlowController_9: javax.inject.Provider[controllers.OverFlowController],
    // @LINE:127
    ProgramingPackageController_4: javax.inject.Provider[controllers.ProgramingPackageController],
    // @LINE:196
    CompilationLibrariesController_6: javax.inject.Provider[controllers.CompilationLibrariesController],
    // @LINE:288
    GridController_5: javax.inject.Provider[controllers.GridController],
    // @LINE:317
    ApiHelpController_2: javax.inject.Provider[utilities.swagger.ApiHelpController],
    // @LINE:323
    Assets_8: controllers.Assets
  ) = this(errorHandler, SecurityController_3, WikyController_7, WebSocketController_Incoming_0, PermissionController_1, PersonCreateController_10, OverFlowController_9, ProgramingPackageController_4, CompilationLibrariesController_6, GridController_5, ApiHelpController_2, Assets_8, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, SecurityController_3, WikyController_7, WebSocketController_Incoming_0, PermissionController_1, PersonCreateController_10, OverFlowController_9, ProgramingPackageController_4, CompilationLibrariesController_6, GridController_5, ApiHelpController_2, Assets_8, prefix)
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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/twitter""", """@controllers.SecurityController@.Twitter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/github""", """@controllers.SecurityController@.GitHub(returnLink:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/vkontakte""", """@controllers.SecurityController@.Vkontakte()"""),
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
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/developer""", """@controllers.PersonCreateController@.developerRegistration()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonCreateController@.standartRegistration()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonCreateController@.updatePersonInformation()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonCreateController@.getPerson(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonCreateController@.deletePerson(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """emailPersonAuthentication/""", """@controllers.PersonCreateController@.emailPersonAuthentitaction(mail:String, authToken:String)"""),
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
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program""", """@controllers.CompilationLibrariesController@.create_C_Program(project_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program""", """@controllers.CompilationLibrariesController@.get_C_Program(c_program_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/project""", """@controllers.CompilationLibrariesController@.get_C_Program_All_from_Project(project_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/edit""", """@controllers.CompilationLibrariesController@.edit_C_Program_Description(c_program_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/update""", """@controllers.CompilationLibrariesController@.update_C_Program(c_program_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/c_program""", """@controllers.CompilationLibrariesController@.delete_C_Program(c_program_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/version""", """@controllers.CompilationLibrariesController@.delete_C_Program_Version(c_program_id:String, version_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/eclipse""", """@controllers.CompilationLibrariesController@.generateProjectForEclipse()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/upload/$id<[^/]+>/$board<[^/]+>""", """@controllers.CompilationLibrariesController@.uploadCompilationToBoard(id:String, board:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/c_program/binary/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.uploadBinaryFileToBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/project/board/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoardsFromProject(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor""", """@controllers.CompilationLibrariesController@.new_Processor()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_Processor(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor""", """@controllers.CompilationLibrariesController@.get_Processor_All()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.update_Processor(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.delete_Processor(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/library/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.connectProcessorWithLibrary(processor_id:String, library_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroup/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.connectProcessorWithLibraryGroup(processor_id:String, library_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/library/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.disconnectProcessorWithLibrary(processor_id:String, library_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroup/$processor_id<[^/]+>/$library_id<[^/]+>""", """@controllers.CompilationLibrariesController@.disconnectProcessorWithLibraryGroup(processor_id:String, library_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroups/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorLibraryGroups(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/singleLibrary/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorSingleLibraries(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board""", """@controllers.CompilationLibrariesController@.newBoard()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/userDescription/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.addUserDescription(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/filter""", """@controllers.CompilationLibrariesController@.getBoardByFilter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoard(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/deactivateBoard$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deactivateBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/userDescription/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getUserDescription(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/connect/$id<[^/]+>/$pr<[^/]+>""", """@controllers.CompilationLibrariesController@.connectBoardWthProject(id:String, pr:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/disconnect/$id<[^/]+>/$pr<[^/]+>""", """@controllers.CompilationLibrariesController@.disconnectBoardWthProject(id:String, pr:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/projects/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoardProjects(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.new_Producers()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.updateProducers(producer_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.get_Producers()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.getProducer(producer_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/description""", """@controllers.CompilationLibrariesController@.getProducerDescription(producer_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/typeOfBoards""", """@controllers.CompilationLibrariesController@.getProducerTypeOfBoards(producer_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.newTypeOfBoard()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateTypeOfBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.getTypeOfBoards()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getTypeOfBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getTypeOfBoardDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/boards/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getTypeOfBoardAllBoards(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.new_LibraryGroup()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.get_LibraryGroup(libraryGroup_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.delete_LibraryGroup(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/filter""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Filter()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.editLibraryGroup(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/generalDescription""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Description(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/processors""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Processors(libraryGroup_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/libraries""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Libraries(libraryGroup_id:String, version_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/version""", """@controllers.CompilationLibrariesController@.new_LibraryGroup_Version(version_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/versions""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Version(version_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/upload""", """@controllers.CompilationLibrariesController@.upload_Library_To_LibraryGroup(libraryGroup_id:String, version_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.new_SingleLibrary()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/version""", """@controllers.CompilationLibrariesController@.new_SingleLibrary_Version(library_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/versions""", """@controllers.CompilationLibrariesController@.get_SingleLibrary_Versions(library_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/filter""", """@controllers.CompilationLibrariesController@.get_SingleLibrary_Filter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.get_SingleLibrary(library_id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.edit_SingleLibrary(library_id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.delete_SingleLibrary(library_id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/upload""", """@controllers.CompilationLibrariesController@.upload_SingleLibrary_Version(library_id:String, version_id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """file/listOfFiles/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.get_LibraryGroup_Version_Libraries(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """file/fileRecord/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.fileRecord(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project""", """@controllers.GridController@.new_M_Program()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/$id<[^/]+>""", """@controllers.GridController@.get_M_Program(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/$id<[^/]+>""", """@controllers.GridController@.edit_M_Program(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_project/$id<[^/]+>""", """@controllers.GridController@.remove_M_Program(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program""", """@controllers.GridController@.new_M_Program_Screen()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/$id<[^/]+>""", """@controllers.GridController@.get_M_Program_Screen(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/$id<[^/]+>""", """@controllers.GridController@.edit_M_Program_Screen(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/m_program/$id<[^/]+>""", """@controllers.GridController@.remove_M_Program_Screen(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type""", """@controllers.GridController@.new_Screen_Size_Type()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/all""", """@controllers.GridController@.get_Screen_Size_Type_PublicList()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/all/$id<[^/]+>""", """@controllers.GridController@.get_Screen_Size_Type_Combination(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/$id<[^/]+>""", """@controllers.GridController@.get_Screen_Size_Type(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/$id<[^/]+>""", """@controllers.GridController@.edit_Screen_Size_Type(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """grid/screen_type/$id<[^/]+>""", """@controllers.GridController@.remove_Screen_Size_Type(id:String)"""),
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
    SecurityController_3.get.index,
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
    WikyController_7.get.test1(),
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
    WikyController_7.get.test2(),
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
    WikyController_7.get.test3(),
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
    WikyController_7.get.test4(fakeValue[String]),
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
    WikyController_7.get.test5(fakeValue[String]),
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
    WikyController_7.get.test6(),
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
    SecurityController_3.get.login(),
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
    SecurityController_3.get.logout,
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
    SecurityController_3.get.Facebook(fakeValue[String]),
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
    SecurityController_3.get.Twitter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "Twitter",
      Nil,
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
    SecurityController_3.get.GitHub(fakeValue[String]),
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
    SecurityController_3.get.Vkontakte(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "Vkontakte",
      Nil,
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
    SecurityController_3.get.getPersonByToken(),
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
    SecurityController_3.get.GET_facebook_oauth(fakeValue[String]),
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
    SecurityController_3.get.GET_github_oauth(fakeValue[String]),
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

  // @LINE:44
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
      """Permission""",
      this.prefix + """secure/permission/person/add"""
    )
  )

  // @LINE:45
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

  // @LINE:46
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

  // @LINE:48
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

  // @LINE:49
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

  // @LINE:50
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

  // @LINE:52
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

  // @LINE:53
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

  // @LINE:55
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

  // @LINE:56
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

  // @LINE:57
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

  // @LINE:66
  private[this] lazy val controllers_PersonCreateController_developerRegistration31_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/developer")))
  )
  private[this] lazy val controllers_PersonCreateController_developerRegistration31_invoker = createInvoker(
    PersonCreateController_10.get.developerRegistration(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonCreateController",
      "developerRegistration",
      Nil,
      "POST",
      """Peron CRUD""",
      this.prefix + """coreClient/person/developer"""
    )
  )

  // @LINE:67
  private[this] lazy val controllers_PersonCreateController_standartRegistration32_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_standartRegistration32_invoker = createInvoker(
    PersonCreateController_10.get.standartRegistration(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonCreateController",
      "standartRegistration",
      Nil,
      "POST",
      """""",
      this.prefix + """coreClient/person/person"""
    )
  )

  // @LINE:68
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation33_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation33_invoker = createInvoker(
    PersonCreateController_10.get.updatePersonInformation(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonCreateController",
      "updatePersonInformation",
      Nil,
      "PUT",
      """""",
      this.prefix + """coreClient/person/person"""
    )
  )

  // @LINE:69
  private[this] lazy val controllers_PersonCreateController_getPerson34_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_getPerson34_invoker = createInvoker(
    PersonCreateController_10.get.getPerson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonCreateController",
      "getPerson",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """coreClient/person/person/$id<[^/]+>"""
    )
  )

  // @LINE:71
  private[this] lazy val controllers_PersonCreateController_deletePerson35_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_deletePerson35_invoker = createInvoker(
    PersonCreateController_10.get.deletePerson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonCreateController",
      "deletePerson",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """coreClient/person/person/$id<[^/]+>"""
    )
  )

  // @LINE:73
  private[this] lazy val controllers_PersonCreateController_emailPersonAuthentitaction36_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("emailPersonAuthentication/")))
  )
  private[this] lazy val controllers_PersonCreateController_emailPersonAuthentitaction36_invoker = createInvoker(
    PersonCreateController_10.get.emailPersonAuthentitaction(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonCreateController",
      "emailPersonAuthentitaction",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """emailPersonAuthentication/"""
    )
  )

  // @LINE:85
  private[this] lazy val controllers_OverFlowController_newPost37_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_newPost37_invoker = createInvoker(
    OverFlowController_9.get.newPost(),
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

  // @LINE:86
  private[this] lazy val controllers_OverFlowController_getPost38_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPost38_invoker = createInvoker(
    OverFlowController_9.get.getPost(fakeValue[String]),
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

  // @LINE:87
  private[this] lazy val controllers_OverFlowController_deletePost39_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost39_invoker = createInvoker(
    OverFlowController_9.get.deletePost(fakeValue[String]),
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

  // @LINE:88
  private[this] lazy val controllers_OverFlowController_editPost40_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_editPost40_invoker = createInvoker(
    OverFlowController_9.get.editPost(fakeValue[String]),
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

  // @LINE:89
  private[this] lazy val controllers_OverFlowController_getPostByFilter41_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postFilter")))
  )
  private[this] lazy val controllers_OverFlowController_getPostByFilter41_invoker = createInvoker(
    OverFlowController_9.get.getPostByFilter(),
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

  // @LINE:90
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers42_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/linkedAnswers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers42_invoker = createInvoker(
    OverFlowController_9.get.getPostLinkedAnswers(fakeValue[String]),
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

  // @LINE:92
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost43_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/hashTags/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost43_invoker = createInvoker(
    OverFlowController_9.get.hashTagsListOnPost(fakeValue[String]),
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

  // @LINE:93
  private[this] lazy val controllers_OverFlowController_commentsListOnPost44_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/comments/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_commentsListOnPost44_invoker = createInvoker(
    OverFlowController_9.get.commentsListOnPost(fakeValue[String]),
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

  // @LINE:94
  private[this] lazy val controllers_OverFlowController_answereListOnPost45_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/answers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_answereListOnPost45_invoker = createInvoker(
    OverFlowController_9.get.answereListOnPost(fakeValue[String]),
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

  // @LINE:95
  private[this] lazy val controllers_OverFlowController_textOfPost46_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/textOfPost/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_textOfPost46_invoker = createInvoker(
    OverFlowController_9.get.textOfPost(fakeValue[String]),
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

  // @LINE:97
  private[this] lazy val controllers_OverFlowController_newTypeOfPost47_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfPost47_invoker = createInvoker(
    OverFlowController_9.get.newTypeOfPost(),
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

  // @LINE:98
  private[this] lazy val controllers_OverFlowController_getTypeOfPost48_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfPost48_invoker = createInvoker(
    OverFlowController_9.get.getTypeOfPost(),
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

  // @LINE:100
  private[this] lazy val controllers_OverFlowController_newTypeOfConfirms49_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfConfirms49_invoker = createInvoker(
    OverFlowController_9.get.newTypeOfConfirms(),
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

  // @LINE:101
  private[this] lazy val controllers_OverFlowController_getTypeOfConfirms50_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfConfirms50_invoker = createInvoker(
    OverFlowController_9.get.getTypeOfConfirms(),
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

  // @LINE:102
  private[this] lazy val controllers_OverFlowController_putTypeOfConfirmToPost51_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm/"), DynamicPart("conf", """[^/]+""",true), StaticPart("/"), DynamicPart("pst", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_putTypeOfConfirmToPost51_invoker = createInvoker(
    OverFlowController_9.get.putTypeOfConfirmToPost(fakeValue[String], fakeValue[String]),
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

  // @LINE:104
  private[this] lazy val controllers_OverFlowController_addComment52_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment")))
  )
  private[this] lazy val controllers_OverFlowController_addComment52_invoker = createInvoker(
    OverFlowController_9.get.addComment(),
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

  // @LINE:105
  private[this] lazy val controllers_OverFlowController_updateComment53_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment53_invoker = createInvoker(
    OverFlowController_9.get.updateComment(fakeValue[String]),
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

  // @LINE:106
  private[this] lazy val controllers_OverFlowController_deletePost54_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost54_invoker = createInvoker(
    OverFlowController_9.get.deletePost(fakeValue[String]),
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

  // @LINE:108
  private[this] lazy val controllers_OverFlowController_addAnswer55_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer")))
  )
  private[this] lazy val controllers_OverFlowController_addAnswer55_invoker = createInvoker(
    OverFlowController_9.get.addAnswer(),
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

  // @LINE:109
  private[this] lazy val controllers_OverFlowController_updateComment56_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment56_invoker = createInvoker(
    OverFlowController_9.get.updateComment(fakeValue[String]),
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

  // @LINE:110
  private[this] lazy val controllers_OverFlowController_deletePost57_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost57_invoker = createInvoker(
    OverFlowController_9.get.deletePost(fakeValue[String]),
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

  // @LINE:112
  private[this] lazy val controllers_OverFlowController_likePlus58_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likePlus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likePlus58_invoker = createInvoker(
    OverFlowController_9.get.likePlus(fakeValue[String]),
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

  // @LINE:113
  private[this] lazy val controllers_OverFlowController_likeMinus59_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likeMinus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likeMinus59_invoker = createInvoker(
    OverFlowController_9.get.likeMinus(fakeValue[String]),
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

  // @LINE:114
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer60_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link")))
  )
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer60_invoker = createInvoker(
    OverFlowController_9.get.linkWithPreviousAnswer(),
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

  // @LINE:115
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer61_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer61_invoker = createInvoker(
    OverFlowController_9.get.unlinkWithPreviousAnswer(fakeValue[String]),
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

  // @LINE:116
  private[this] lazy val controllers_OverFlowController_removeHashTag62_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeLink")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag62_invoker = createInvoker(
    OverFlowController_9.get.removeHashTag(),
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

  // @LINE:117
  private[this] lazy val controllers_OverFlowController_addHashTag63_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/hashTag")))
  )
  private[this] lazy val controllers_OverFlowController_addHashTag63_invoker = createInvoker(
    OverFlowController_9.get.addHashTag(),
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

  // @LINE:118
  private[this] lazy val controllers_OverFlowController_removeHashTag64_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeHashTag")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag64_invoker = createInvoker(
    OverFlowController_9.get.removeHashTag(),
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

  // @LINE:127
  private[this] lazy val controllers_ProgramingPackageController_postNewProject65_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProject65_invoker = createInvoker(
    ProgramingPackageController_4.get.postNewProject(),
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

  // @LINE:128
  private[this] lazy val controllers_ProgramingPackageController_updateProject66_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateProject66_invoker = createInvoker(
    ProgramingPackageController_4.get.updateProject(fakeValue[String]),
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

  // @LINE:129
  private[this] lazy val controllers_ProgramingPackageController_getProject67_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProject67_invoker = createInvoker(
    ProgramingPackageController_4.get.getProject(fakeValue[String]),
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

  // @LINE:130
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount68_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount68_invoker = createInvoker(
    ProgramingPackageController_4.get.getProjectsByUserAccount(),
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

  // @LINE:131
  private[this] lazy val controllers_ProgramingPackageController_deleteProject69_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteProject69_invoker = createInvoker(
    ProgramingPackageController_4.get.deleteProject(fakeValue[String]),
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

  // @LINE:132
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers70_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/shareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers70_invoker = createInvoker(
    ProgramingPackageController_4.get.shareProjectWithUsers(fakeValue[String]),
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

  // @LINE:133
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers71_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/unshareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers71_invoker = createInvoker(
    ProgramingPackageController_4.get.unshareProjectWithUsers(fakeValue[String]),
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

  // @LINE:134
  private[this] lazy val controllers_ProgramingPackageController_getAll_b_Programs72_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/b_programs/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAll_b_Programs72_invoker = createInvoker(
    ProgramingPackageController_4.get.getAll_b_Programs(fakeValue[String]),
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

  // @LINE:135
  private[this] lazy val controllers_ProgramingPackageController_getAll_c_Programs73_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/c_programs/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAll_c_Programs73_invoker = createInvoker(
    ProgramingPackageController_4.get.getAll_c_Programs(fakeValue[String]),
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

  // @LINE:136
  private[this] lazy val controllers_ProgramingPackageController_getAll_m_Projects74_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/m_projects/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAll_m_Projects74_invoker = createInvoker(
    ProgramingPackageController_4.get.getAll_m_Projects(fakeValue[String]),
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

  // @LINE:138
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList75_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/homerList/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList75_invoker = createInvoker(
    ProgramingPackageController_4.get.getProgramhomerList(fakeValue[String]),
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

  // @LINE:139
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners76_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/owners/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners76_invoker = createInvoker(
    ProgramingPackageController_4.get.getProjectOwners(fakeValue[String]),
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

  // @LINE:142
  private[this] lazy val controllers_ProgramingPackageController_newHomer77_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newHomer77_invoker = createInvoker(
    ProgramingPackageController_4.get.newHomer(),
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

  // @LINE:143
  private[this] lazy val controllers_ProgramingPackageController_removeHomer78_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeHomer78_invoker = createInvoker(
    ProgramingPackageController_4.get.removeHomer(fakeValue[String]),
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

  // @LINE:144
  private[this] lazy val controllers_ProgramingPackageController_getHomer79_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getHomer79_invoker = createInvoker(
    ProgramingPackageController_4.get.getHomer(fakeValue[String]),
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

  // @LINE:145
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers80_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers80_invoker = createInvoker(
    ProgramingPackageController_4.get.getAllHomers(),
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

  // @LINE:146
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers81_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/getAllConnectedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers81_invoker = createInvoker(
    ProgramingPackageController_4.get.getConnectedHomers(fakeValue[String]),
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

  // @LINE:150
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject82_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject82_invoker = createInvoker(
    ProgramingPackageController_4.get.connectHomerWithProject(),
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

  // @LINE:151
  private[this] lazy val controllers_ProgramingPackageController_disconnectHomerWithProject83_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/disconnectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_disconnectHomerWithProject83_invoker = createInvoker(
    ProgramingPackageController_4.get.disconnectHomerWithProject(),
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

  // @LINE:155
  private[this] lazy val controllers_ProgramingPackageController_postNewBProgram84_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewBProgram84_invoker = createInvoker(
    ProgramingPackageController_4.get.postNewBProgram(),
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

  // @LINE:156
  private[this] lazy val controllers_ProgramingPackageController_getProgram85_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgram85_invoker = createInvoker(
    ProgramingPackageController_4.get.getProgram(fakeValue[String]),
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

  // @LINE:157
  private[this] lazy val controllers_ProgramingPackageController_editProgram86_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editProgram86_invoker = createInvoker(
    ProgramingPackageController_4.get.editProgram(fakeValue[String]),
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

  // @LINE:158
  private[this] lazy val controllers_ProgramingPackageController_update_b_program87_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/update/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_update_b_program87_invoker = createInvoker(
    ProgramingPackageController_4.get.update_b_program(fakeValue[String]),
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

  // @LINE:159
  private[this] lazy val controllers_ProgramingPackageController_remove_b_Program88_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_remove_b_Program88_invoker = createInvoker(
    ProgramingPackageController_4.get.remove_b_Program(fakeValue[String]),
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

  // @LINE:160
  private[this] lazy val controllers_ProgramingPackageController_getProgramInString89_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_programInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInString89_invoker = createInvoker(
    ProgramingPackageController_4.get.getProgramInString(fakeValue[String]),
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

  // @LINE:161
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately90_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/upload/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("ver", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately90_invoker = createInvoker(
    ProgramingPackageController_4.get.uploadProgramToHomer_Immediately(fakeValue[String], fakeValue[String]),
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

  // @LINE:162
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToCloud91_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/b_program/uploadToCloud/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("ver", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToCloud91_invoker = createInvoker(
    ProgramingPackageController_4.get.uploadProgramToCloud(fakeValue[String], fakeValue[String]),
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

  // @LINE:164
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers92_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfUploadedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers92_invoker = createInvoker(
    ProgramingPackageController_4.get.listOfUploadedHomers(fakeValue[String]),
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

  // @LINE:165
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload93_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfHomersWaitingForUpload/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload93_invoker = createInvoker(
    ProgramingPackageController_4.get.listOfHomersWaitingForUpload(fakeValue[String]),
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

  // @LINE:166
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard94_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard94_invoker = createInvoker(
    ProgramingPackageController_4.get.getProjectsBoard(fakeValue[String]),
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

  // @LINE:169
  private[this] lazy val controllers_ProgramingPackageController_newBlock95_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newBlock95_invoker = createInvoker(
    ProgramingPackageController_4.get.newBlock(),
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

  // @LINE:170
  private[this] lazy val controllers_ProgramingPackageController_updateOfBlock96_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateOfBlock96_invoker = createInvoker(
    ProgramingPackageController_4.get.updateOfBlock(fakeValue[String]),
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

  // @LINE:171
  private[this] lazy val controllers_ProgramingPackageController_editBlock97_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editBlock97_invoker = createInvoker(
    ProgramingPackageController_4.get.editBlock(fakeValue[String]),
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

  // @LINE:172
  private[this] lazy val controllers_ProgramingPackageController_getBlockBlock98_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlockBlock98_invoker = createInvoker(
    ProgramingPackageController_4.get.getBlockBlock(fakeValue[String]),
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

  // @LINE:173
  private[this] lazy val controllers_ProgramingPackageController_getBlockVersions99_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/versions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlockVersions99_invoker = createInvoker(
    ProgramingPackageController_4.get.getBlockVersions(fakeValue[String]),
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

  // @LINE:176
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions100_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/allPrevVersions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions100_invoker = createInvoker(
    ProgramingPackageController_4.get.allPrevVersions(fakeValue[String]),
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

  // @LINE:177
  private[this] lazy val controllers_ProgramingPackageController_deleteBlockVersion101_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlockVersion101_invoker = createInvoker(
    ProgramingPackageController_4.get.deleteBlockVersion(fakeValue[String]),
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

  // @LINE:178
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock102_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/block/id")))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock102_invoker = createInvoker(
    ProgramingPackageController_4.get.deleteBlock(fakeValue[String]),
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

  // @LINE:180
  private[this] lazy val controllers_ProgramingPackageController_newTypeOfBlock103_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newTypeOfBlock103_invoker = createInvoker(
    ProgramingPackageController_4.get.newTypeOfBlock(),
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

  // @LINE:181
  private[this] lazy val controllers_ProgramingPackageController_getByCategory104_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock/filter")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getByCategory104_invoker = createInvoker(
    ProgramingPackageController_4.get.getByCategory(),
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

  // @LINE:182
  private[this] lazy val controllers_ProgramingPackageController_editTypeOfBlock105_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editTypeOfBlock105_invoker = createInvoker(
    ProgramingPackageController_4.get.editTypeOfBlock(fakeValue[String]),
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

  // @LINE:183
  private[this] lazy val controllers_ProgramingPackageController_getAllTypeOfBlocks106_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllTypeOfBlocks106_invoker = createInvoker(
    ProgramingPackageController_4.get.getAllTypeOfBlocks(),
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

  // @LINE:184
  private[this] lazy val controllers_ProgramingPackageController_deleteTypeOfBlock107_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/typeOfBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteTypeOfBlock107_invoker = createInvoker(
    ProgramingPackageController_4.get.deleteTypeOfBlock(fakeValue[String]),
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

  // @LINE:196
  private[this] lazy val controllers_CompilationLibrariesController_create_C_Program108_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_create_C_Program108_invoker = createInvoker(
    CompilationLibrariesController_6.get.create_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "create_C_Program",
      Seq(classOf[String]),
      "POST",
      """C:Program""",
      this.prefix + """compilation/c_program"""
    )
  )

  // @LINE:197
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program109_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program109_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_C_Program",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/c_program"""
    )
  )

  // @LINE:198
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program_All_from_Project110_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/project")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_C_Program_All_from_Project110_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_C_Program_All_from_Project(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_C_Program_All_from_Project",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/c_program/project"""
    )
  )

  // @LINE:200
  private[this] lazy val controllers_CompilationLibrariesController_edit_C_Program_Description111_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/edit")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_edit_C_Program_Description111_invoker = createInvoker(
    CompilationLibrariesController_6.get.edit_C_Program_Description(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "edit_C_Program_Description",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/c_program/edit"""
    )
  )

  // @LINE:201
  private[this] lazy val controllers_CompilationLibrariesController_update_C_Program112_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/update")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_update_C_Program112_invoker = createInvoker(
    CompilationLibrariesController_6.get.update_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "update_C_Program",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/c_program/update"""
    )
  )

  // @LINE:203
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program113_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/c_program")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program113_invoker = createInvoker(
    CompilationLibrariesController_6.get.delete_C_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_C_Program",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/c_program/c_program"""
    )
  )

  // @LINE:204
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program_Version114_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/version")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_C_Program_Version114_invoker = createInvoker(
    CompilationLibrariesController_6.get.delete_C_Program_Version(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_C_Program_Version",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/c_program/version"""
    )
  )

  // @LINE:206
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse115_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/eclipse")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse115_invoker = createInvoker(
    CompilationLibrariesController_6.get.generateProjectForEclipse(),
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

  // @LINE:207
  private[this] lazy val controllers_CompilationLibrariesController_uploadCompilationToBoard116_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/upload/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("board", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadCompilationToBoard116_invoker = createInvoker(
    CompilationLibrariesController_6.get.uploadCompilationToBoard(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploadCompilationToBoard",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/c_program/upload/$id<[^/]+>/$board<[^/]+>"""
    )
  )

  // @LINE:208
  private[this] lazy val controllers_CompilationLibrariesController_uploadBinaryFileToBoard117_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/c_program/binary/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadBinaryFileToBoard117_invoker = createInvoker(
    CompilationLibrariesController_6.get.uploadBinaryFileToBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploadBinaryFileToBoard",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/c_program/binary/$id<[^/]+>"""
    )
  )

  // @LINE:210
  private[this] lazy val controllers_CompilationLibrariesController_getBoardsFromProject118_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardsFromProject118_invoker = createInvoker(
    CompilationLibrariesController_6.get.getBoardsFromProject(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getBoardsFromProject",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/project/board/$id<[^/]+>"""
    )
  )

  // @LINE:213
  private[this] lazy val controllers_CompilationLibrariesController_new_Processor119_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_Processor119_invoker = createInvoker(
    CompilationLibrariesController_6.get.new_Processor(),
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

  // @LINE:214
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor120_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor120_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_Processor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Processor",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/$id<[^/]+>"""
    )
  )

  // @LINE:215
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor_All121_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Processor_All121_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_Processor_All(),
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

  // @LINE:216
  private[this] lazy val controllers_CompilationLibrariesController_update_Processor122_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_update_Processor122_invoker = createInvoker(
    CompilationLibrariesController_6.get.update_Processor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "update_Processor",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/processor/$id<[^/]+>"""
    )
  )

  // @LINE:217
  private[this] lazy val controllers_CompilationLibrariesController_delete_Processor123_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_Processor123_invoker = createInvoker(
    CompilationLibrariesController_6.get.delete_Processor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_Processor",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/processor/$id<[^/]+>"""
    )
  )

  // @LINE:219
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary124_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/library/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary124_invoker = createInvoker(
    CompilationLibrariesController_6.get.connectProcessorWithLibrary(fakeValue[String], fakeValue[String]),
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

  // @LINE:220
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup125_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroup/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup125_invoker = createInvoker(
    CompilationLibrariesController_6.get.connectProcessorWithLibraryGroup(fakeValue[String], fakeValue[String]),
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

  // @LINE:221
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibrary126_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/library/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibrary126_invoker = createInvoker(
    CompilationLibrariesController_6.get.disconnectProcessorWithLibrary(fakeValue[String], fakeValue[String]),
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

  // @LINE:222
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup127_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroup/"), DynamicPart("processor_id", """[^/]+""",true), StaticPart("/"), DynamicPart("library_id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup127_invoker = createInvoker(
    CompilationLibrariesController_6.get.disconnectProcessorWithLibraryGroup(fakeValue[String], fakeValue[String]),
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

  // @LINE:224
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups128_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroups/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups128_invoker = createInvoker(
    CompilationLibrariesController_6.get.getProcessorLibraryGroups(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorLibraryGroups",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/libraryGroups/$id<[^/]+>"""
    )
  )

  // @LINE:225
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries129_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/singleLibrary/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries129_invoker = createInvoker(
    CompilationLibrariesController_6.get.getProcessorSingleLibraries(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorSingleLibraries",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/singleLibrary/$id<[^/]+>"""
    )
  )

  // @LINE:228
  private[this] lazy val controllers_CompilationLibrariesController_newBoard130_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newBoard130_invoker = createInvoker(
    CompilationLibrariesController_6.get.newBoard(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newBoard",
      Nil,
      "POST",
      """Board""",
      this.prefix + """compilation/board"""
    )
  )

  // @LINE:229
  private[this] lazy val controllers_CompilationLibrariesController_addUserDescription131_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_addUserDescription131_invoker = createInvoker(
    CompilationLibrariesController_6.get.addUserDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "addUserDescription",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/userDescription/$id<[^/]+>"""
    )
  )

  // @LINE:230
  private[this] lazy val controllers_CompilationLibrariesController_getBoardByFilter132_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardByFilter132_invoker = createInvoker(
    CompilationLibrariesController_6.get.getBoardByFilter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getBoardByFilter",
      Nil,
      "PUT",
      """""",
      this.prefix + """compilation/board/filter"""
    )
  )

  // @LINE:231
  private[this] lazy val controllers_CompilationLibrariesController_getBoard133_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoard133_invoker = createInvoker(
    CompilationLibrariesController_6.get.getBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getBoard",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/board/$id<[^/]+>"""
    )
  )

  // @LINE:232
  private[this] lazy val controllers_CompilationLibrariesController_deactivateBoard134_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/deactivateBoard"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deactivateBoard134_invoker = createInvoker(
    CompilationLibrariesController_6.get.deactivateBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deactivateBoard",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/board/deactivateBoard$id<[^/]+>"""
    )
  )

  // @LINE:233
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription135_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription135_invoker = createInvoker(
    CompilationLibrariesController_6.get.getUserDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getUserDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/board/userDescription/$id<[^/]+>"""
    )
  )

  // @LINE:234
  private[this] lazy val controllers_CompilationLibrariesController_connectBoardWthProject136_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/connect/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("pr", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectBoardWthProject136_invoker = createInvoker(
    CompilationLibrariesController_6.get.connectBoardWthProject(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "connectBoardWthProject",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/connect/$id<[^/]+>/$pr<[^/]+>"""
    )
  )

  // @LINE:235
  private[this] lazy val controllers_CompilationLibrariesController_disconnectBoardWthProject137_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/disconnect/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("pr", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_disconnectBoardWthProject137_invoker = createInvoker(
    CompilationLibrariesController_6.get.disconnectBoardWthProject(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "disconnectBoardWthProject",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/disconnect/$id<[^/]+>/$pr<[^/]+>"""
    )
  )

  // @LINE:236
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects138_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/projects/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects138_invoker = createInvoker(
    CompilationLibrariesController_6.get.getBoardProjects(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getBoardProjects",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/board/projects/$id<[^/]+>"""
    )
  )

  // @LINE:240
  private[this] lazy val controllers_CompilationLibrariesController_new_Producers139_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_Producers139_invoker = createInvoker(
    CompilationLibrariesController_6.get.new_Producers(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_Producers",
      Nil,
      "POST",
      """Producer""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:241
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers140_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers140_invoker = createInvoker(
    CompilationLibrariesController_6.get.updateProducers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateProducers",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:242
  private[this] lazy val controllers_CompilationLibrariesController_get_Producers141_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_Producers141_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_Producers(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_Producers",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:243
  private[this] lazy val controllers_CompilationLibrariesController_getProducer142_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducer142_invoker = createInvoker(
    CompilationLibrariesController_6.get.getProducer(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProducer",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:244
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription143_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/description")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription143_invoker = createInvoker(
    CompilationLibrariesController_6.get.getProducerDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProducerDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer/description"""
    )
  )

  // @LINE:245
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards144_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/typeOfBoards")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards144_invoker = createInvoker(
    CompilationLibrariesController_6.get.getProducerTypeOfBoards(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProducerTypeOfBoards",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer/typeOfBoards"""
    )
  )

  // @LINE:248
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard145_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard145_invoker = createInvoker(
    CompilationLibrariesController_6.get.newTypeOfBoard(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newTypeOfBoard",
      Nil,
      "POST",
      """TypeOfBoard""",
      this.prefix + """compilation/typeOfBoard"""
    )
  )

  // @LINE:249
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard146_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard146_invoker = createInvoker(
    CompilationLibrariesController_6.get.updateTypeOfBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateTypeOfBoard",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/typeOfBoard/$id<[^/]+>"""
    )
  )

  // @LINE:250
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards147_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards147_invoker = createInvoker(
    CompilationLibrariesController_6.get.getTypeOfBoards(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getTypeOfBoards",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard"""
    )
  )

  // @LINE:251
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard148_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard148_invoker = createInvoker(
    CompilationLibrariesController_6.get.getTypeOfBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getTypeOfBoard",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard/$id<[^/]+>"""
    )
  )

  // @LINE:252
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription149_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription149_invoker = createInvoker(
    CompilationLibrariesController_6.get.getTypeOfBoardDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getTypeOfBoardDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard/description/$id<[^/]+>"""
    )
  )

  // @LINE:253
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards150_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards150_invoker = createInvoker(
    CompilationLibrariesController_6.get.getTypeOfBoardAllBoards(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getTypeOfBoardAllBoards",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/typeOfBoard/boards/$id<[^/]+>"""
    )
  )

  // @LINE:256
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup151_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup151_invoker = createInvoker(
    CompilationLibrariesController_6.get.new_LibraryGroup(),
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

  // @LINE:257
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup152_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup152_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_LibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup"""
    )
  )

  // @LINE:258
  private[this] lazy val controllers_CompilationLibrariesController_delete_LibraryGroup153_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_LibraryGroup153_invoker = createInvoker(
    CompilationLibrariesController_6.get.delete_LibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_LibraryGroup",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/libraryGroup"""
    )
  )

  // @LINE:259
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Filter154_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Filter154_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_LibraryGroup_Filter(),
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

  // @LINE:260
  private[this] lazy val controllers_CompilationLibrariesController_editLibraryGroup155_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_editLibraryGroup155_invoker = createInvoker(
    CompilationLibrariesController_6.get.editLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "editLibraryGroup",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/libraryGroup"""
    )
  )

  // @LINE:261
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Description156_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/generalDescription")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Description156_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_LibraryGroup_Description(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Description",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/generalDescription"""
    )
  )

  // @LINE:262
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Processors157_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/processors")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Processors157_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_LibraryGroup_Processors(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Processors",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/processors"""
    )
  )

  // @LINE:263
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Libraries158_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/libraries")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Libraries158_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_LibraryGroup_Libraries(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Libraries",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/libraries"""
    )
  )

  // @LINE:264
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup_Version159_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/version")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_LibraryGroup_Version159_invoker = createInvoker(
    CompilationLibrariesController_6.get.new_LibraryGroup_Version(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_LibraryGroup_Version",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/version"""
    )
  )

  // @LINE:265
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version160_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/versions")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version160_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_LibraryGroup_Version(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_LibraryGroup_Version",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/versions"""
    )
  )

  // @LINE:266
  private[this] lazy val controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup161_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/upload")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup161_invoker = createInvoker(
    CompilationLibrariesController_6.get.upload_Library_To_LibraryGroup(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "upload_Library_To_LibraryGroup",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/upload"""
    )
  )

  // @LINE:269
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary162_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary162_invoker = createInvoker(
    CompilationLibrariesController_6.get.new_SingleLibrary(),
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

  // @LINE:270
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary_Version163_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/version")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_new_SingleLibrary_Version163_invoker = createInvoker(
    CompilationLibrariesController_6.get.new_SingleLibrary_Version(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "new_SingleLibrary_Version",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/library/version"""
    )
  )

  // @LINE:271
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Versions164_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/versions")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Versions164_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_SingleLibrary_Versions(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_SingleLibrary_Versions",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/versions"""
    )
  )

  // @LINE:272
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Filter165_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary_Filter165_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_SingleLibrary_Filter(),
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

  // @LINE:273
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary166_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_SingleLibrary166_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_SingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "get_SingleLibrary",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:275
  private[this] lazy val controllers_CompilationLibrariesController_edit_SingleLibrary167_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_edit_SingleLibrary167_invoker = createInvoker(
    CompilationLibrariesController_6.get.edit_SingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "edit_SingleLibrary",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:276
  private[this] lazy val controllers_CompilationLibrariesController_delete_SingleLibrary168_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_delete_SingleLibrary168_invoker = createInvoker(
    CompilationLibrariesController_6.get.delete_SingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "delete_SingleLibrary",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:277
  private[this] lazy val controllers_CompilationLibrariesController_upload_SingleLibrary_Version169_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/upload")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_upload_SingleLibrary_Version169_invoker = createInvoker(
    CompilationLibrariesController_6.get.upload_SingleLibrary_Version(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "upload_SingleLibrary_Version",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/library/upload"""
    )
  )

  // @LINE:280
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries170_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("file/listOfFiles/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries170_invoker = createInvoker(
    CompilationLibrariesController_6.get.get_LibraryGroup_Version_Libraries(fakeValue[String]),
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

  // @LINE:281
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord171_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("file/fileRecord/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord171_invoker = createInvoker(
    CompilationLibrariesController_6.get.fileRecord(fakeValue[String]),
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

  // @LINE:288
  private[this] lazy val controllers_GridController_new_M_Program172_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project")))
  )
  private[this] lazy val controllers_GridController_new_M_Program172_invoker = createInvoker(
    GridController_5.get.new_M_Program(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "new_M_Program",
      Nil,
      "POST",
      """M Project""",
      this.prefix + """grid/m_project"""
    )
  )

  // @LINE:289
  private[this] lazy val controllers_GridController_get_M_Program173_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_M_Program173_invoker = createInvoker(
    GridController_5.get.get_M_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Program",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/m_project/$id<[^/]+>"""
    )
  )

  // @LINE:290
  private[this] lazy val controllers_GridController_edit_M_Program174_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_edit_M_Program174_invoker = createInvoker(
    GridController_5.get.edit_M_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "edit_M_Program",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """grid/m_project/$id<[^/]+>"""
    )
  )

  // @LINE:291
  private[this] lazy val controllers_GridController_remove_M_Program175_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_remove_M_Program175_invoker = createInvoker(
    GridController_5.get.remove_M_Program(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "remove_M_Program",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """grid/m_project/$id<[^/]+>"""
    )
  )

  // @LINE:294
  private[this] lazy val controllers_GridController_new_M_Program_Screen176_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program")))
  )
  private[this] lazy val controllers_GridController_new_M_Program_Screen176_invoker = createInvoker(
    GridController_5.get.new_M_Program_Screen(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "new_M_Program_Screen",
      Nil,
      "POST",
      """M Program""",
      this.prefix + """grid/m_program"""
    )
  )

  // @LINE:295
  private[this] lazy val controllers_GridController_get_M_Program_Screen177_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_M_Program_Screen177_invoker = createInvoker(
    GridController_5.get.get_M_Program_Screen(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_M_Program_Screen",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/m_program/$id<[^/]+>"""
    )
  )

  // @LINE:296
  private[this] lazy val controllers_GridController_edit_M_Program_Screen178_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_edit_M_Program_Screen178_invoker = createInvoker(
    GridController_5.get.edit_M_Program_Screen(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "edit_M_Program_Screen",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """grid/m_program/$id<[^/]+>"""
    )
  )

  // @LINE:297
  private[this] lazy val controllers_GridController_remove_M_Program_Screen179_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/m_program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_remove_M_Program_Screen179_invoker = createInvoker(
    GridController_5.get.remove_M_Program_Screen(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "remove_M_Program_Screen",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """grid/m_program/$id<[^/]+>"""
    )
  )

  // @LINE:301
  private[this] lazy val controllers_GridController_new_Screen_Size_Type180_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type")))
  )
  private[this] lazy val controllers_GridController_new_Screen_Size_Type180_invoker = createInvoker(
    GridController_5.get.new_Screen_Size_Type(),
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

  // @LINE:303
  private[this] lazy val controllers_GridController_get_Screen_Size_Type_PublicList181_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/all")))
  )
  private[this] lazy val controllers_GridController_get_Screen_Size_Type_PublicList181_invoker = createInvoker(
    GridController_5.get.get_Screen_Size_Type_PublicList(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_Screen_Size_Type_PublicList",
      Nil,
      "GET",
      """""",
      this.prefix + """grid/screen_type/all"""
    )
  )

  // @LINE:304
  private[this] lazy val controllers_GridController_get_Screen_Size_Type_Combination182_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/all/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_Screen_Size_Type_Combination182_invoker = createInvoker(
    GridController_5.get.get_Screen_Size_Type_Combination(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_Screen_Size_Type_Combination",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/screen_type/all/$id<[^/]+>"""
    )
  )

  // @LINE:305
  private[this] lazy val controllers_GridController_get_Screen_Size_Type183_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_get_Screen_Size_Type183_invoker = createInvoker(
    GridController_5.get.get_Screen_Size_Type(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "get_Screen_Size_Type",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """grid/screen_type/$id<[^/]+>"""
    )
  )

  // @LINE:307
  private[this] lazy val controllers_GridController_edit_Screen_Size_Type184_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_edit_Screen_Size_Type184_invoker = createInvoker(
    GridController_5.get.edit_Screen_Size_Type(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "edit_Screen_Size_Type",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """grid/screen_type/$id<[^/]+>"""
    )
  )

  // @LINE:308
  private[this] lazy val controllers_GridController_remove_Screen_Size_Type185_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("grid/screen_type/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_GridController_remove_Screen_Size_Type185_invoker = createInvoker(
    GridController_5.get.remove_Screen_Size_Type(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.GridController",
      "remove_Screen_Size_Type",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """grid/screen_type/$id<[^/]+>"""
    )
  )

  // @LINE:317
  private[this] lazy val utilities_swagger_ApiHelpController_getResources186_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api-docs")))
  )
  private[this] lazy val utilities_swagger_ApiHelpController_getResources186_invoker = createInvoker(
    ApiHelpController_2.get.getResources,
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

  // @LINE:320
  private[this] lazy val controllers_SecurityController_optionLink187_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("all", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_optionLink187_invoker = createInvoker(
    SecurityController_3.get.optionLink(fakeValue[String]),
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

  // @LINE:323
  private[this] lazy val controllers_Assets_at188_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at188_invoker = createInvoker(
    Assets_8.at(fakeValue[String], fakeValue[String]),
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
        controllers_SecurityController_index0_invoker.call(SecurityController_3.get.index)
      }
  
    // @LINE:11
    case controllers_WikyController_test11_route(params) =>
      call { 
        controllers_WikyController_test11_invoker.call(WikyController_7.get.test1())
      }
  
    // @LINE:12
    case controllers_WikyController_test22_route(params) =>
      call { 
        controllers_WikyController_test22_invoker.call(WikyController_7.get.test2())
      }
  
    // @LINE:13
    case controllers_WikyController_test33_route(params) =>
      call { 
        controllers_WikyController_test33_invoker.call(WikyController_7.get.test3())
      }
  
    // @LINE:14
    case controllers_WikyController_test44_route(params) =>
      call(params.fromQuery[String]("projectId", None)) { (projectId) =>
        controllers_WikyController_test44_invoker.call(WikyController_7.get.test4(projectId))
      }
  
    // @LINE:15
    case controllers_WikyController_test55_route(params) =>
      call(params.fromQuery[String]("projectId", None)) { (projectId) =>
        controllers_WikyController_test55_invoker.call(WikyController_7.get.test5(projectId))
      }
  
    // @LINE:16
    case controllers_WikyController_test66_route(params) =>
      call { 
        controllers_WikyController_test66_invoker.call(WikyController_7.get.test6())
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
        controllers_SecurityController_login11_invoker.call(SecurityController_3.get.login())
      }
  
    // @LINE:31
    case controllers_SecurityController_logout12_route(params) =>
      call { 
        controllers_SecurityController_logout12_invoker.call(SecurityController_3.get.logout)
      }
  
    // @LINE:33
    case controllers_SecurityController_Facebook13_route(params) =>
      call(params.fromQuery[String]("returnLink", None)) { (returnLink) =>
        controllers_SecurityController_Facebook13_invoker.call(SecurityController_3.get.Facebook(returnLink))
      }
  
    // @LINE:34
    case controllers_SecurityController_Twitter14_route(params) =>
      call { 
        controllers_SecurityController_Twitter14_invoker.call(SecurityController_3.get.Twitter())
      }
  
    // @LINE:35
    case controllers_SecurityController_GitHub15_route(params) =>
      call(params.fromQuery[String]("returnLink", None)) { (returnLink) =>
        controllers_SecurityController_GitHub15_invoker.call(SecurityController_3.get.GitHub(returnLink))
      }
  
    // @LINE:36
    case controllers_SecurityController_Vkontakte16_route(params) =>
      call { 
        controllers_SecurityController_Vkontakte16_invoker.call(SecurityController_3.get.Vkontakte())
      }
  
    // @LINE:38
    case controllers_SecurityController_getPersonByToken17_route(params) =>
      call { 
        controllers_SecurityController_getPersonByToken17_invoker.call(SecurityController_3.get.getPersonByToken())
      }
  
    // @LINE:40
    case controllers_SecurityController_GET_facebook_oauth18_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_SecurityController_GET_facebook_oauth18_invoker.call(SecurityController_3.get.GET_facebook_oauth(url))
      }
  
    // @LINE:41
    case controllers_SecurityController_GET_github_oauth19_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_SecurityController_GET_github_oauth19_invoker.call(SecurityController_3.get.GET_github_oauth(url))
      }
  
    // @LINE:44
    case controllers_PermissionController_add_Permission_Person20_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("permission_id", None)) { (person_id, permission_id) =>
        controllers_PermissionController_add_Permission_Person20_invoker.call(PermissionController_1.get.add_Permission_Person(person_id, permission_id))
      }
  
    // @LINE:45
    case controllers_PermissionController_remove_Permission_Person21_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("permission_id", None)) { (person_id, permission_id) =>
        controllers_PermissionController_remove_Permission_Person21_invoker.call(PermissionController_1.get.remove_Permission_Person(person_id, permission_id))
      }
  
    // @LINE:46
    case controllers_PermissionController_get_Permission_All22_route(params) =>
      call { 
        controllers_PermissionController_get_Permission_All22_invoker.call(PermissionController_1.get.get_Permission_All())
      }
  
    // @LINE:48
    case controllers_PermissionController_add_Permission_to_Role23_route(params) =>
      call(params.fromQuery[String]("permission_id", None), params.fromQuery[String]("role_id", None)) { (permission_id, role_id) =>
        controllers_PermissionController_add_Permission_to_Role23_invoker.call(PermissionController_1.get.add_Permission_to_Role(permission_id, role_id))
      }
  
    // @LINE:49
    case controllers_PermissionController_get_Permission_in_Group24_route(params) =>
      call(params.fromQuery[String]("role_id", None)) { (role_id) =>
        controllers_PermissionController_get_Permission_in_Group24_invoker.call(PermissionController_1.get.get_Permission_in_Group(role_id))
      }
  
    // @LINE:50
    case controllers_PermissionController_remove_Permission_from_Role25_route(params) =>
      call(params.fromQuery[String]("permission_id", None), params.fromQuery[String]("role_id", None)) { (permission_id, role_id) =>
        controllers_PermissionController_remove_Permission_from_Role25_invoker.call(PermissionController_1.get.remove_Permission_from_Role(permission_id, role_id))
      }
  
    // @LINE:52
    case controllers_PermissionController_new_Role26_route(params) =>
      call { 
        controllers_PermissionController_new_Role26_invoker.call(PermissionController_1.get.new_Role())
      }
  
    // @LINE:53
    case controllers_PermissionController_delete_Role27_route(params) =>
      call(params.fromQuery[String]("role_id", None)) { (role_id) =>
        controllers_PermissionController_delete_Role27_invoker.call(PermissionController_1.get.delete_Role(role_id))
      }
  
    // @LINE:55
    case controllers_PermissionController_add_Role_Person28_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("role_id", None)) { (person_id, role_id) =>
        controllers_PermissionController_add_Role_Person28_invoker.call(PermissionController_1.get.add_Role_Person(person_id, role_id))
      }
  
    // @LINE:56
    case controllers_PermissionController_remove_Role_Person29_route(params) =>
      call(params.fromQuery[String]("person_id", None), params.fromQuery[String]("role_id", None)) { (person_id, role_id) =>
        controllers_PermissionController_remove_Role_Person29_invoker.call(PermissionController_1.get.remove_Role_Person(person_id, role_id))
      }
  
    // @LINE:57
    case controllers_PermissionController_get_Role_All30_route(params) =>
      call { 
        controllers_PermissionController_get_Role_All30_invoker.call(PermissionController_1.get.get_Role_All())
      }
  
    // @LINE:66
    case controllers_PersonCreateController_developerRegistration31_route(params) =>
      call { 
        controllers_PersonCreateController_developerRegistration31_invoker.call(PersonCreateController_10.get.developerRegistration())
      }
  
    // @LINE:67
    case controllers_PersonCreateController_standartRegistration32_route(params) =>
      call { 
        controllers_PersonCreateController_standartRegistration32_invoker.call(PersonCreateController_10.get.standartRegistration())
      }
  
    // @LINE:68
    case controllers_PersonCreateController_updatePersonInformation33_route(params) =>
      call { 
        controllers_PersonCreateController_updatePersonInformation33_invoker.call(PersonCreateController_10.get.updatePersonInformation())
      }
  
    // @LINE:69
    case controllers_PersonCreateController_getPerson34_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_getPerson34_invoker.call(PersonCreateController_10.get.getPerson(id))
      }
  
    // @LINE:71
    case controllers_PersonCreateController_deletePerson35_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_deletePerson35_invoker.call(PersonCreateController_10.get.deletePerson(id))
      }
  
    // @LINE:73
    case controllers_PersonCreateController_emailPersonAuthentitaction36_route(params) =>
      call(params.fromQuery[String]("mail", None), params.fromQuery[String]("authToken", None)) { (mail, authToken) =>
        controllers_PersonCreateController_emailPersonAuthentitaction36_invoker.call(PersonCreateController_10.get.emailPersonAuthentitaction(mail, authToken))
      }
  
    // @LINE:85
    case controllers_OverFlowController_newPost37_route(params) =>
      call { 
        controllers_OverFlowController_newPost37_invoker.call(OverFlowController_9.get.newPost())
      }
  
    // @LINE:86
    case controllers_OverFlowController_getPost38_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPost38_invoker.call(OverFlowController_9.get.getPost(id))
      }
  
    // @LINE:87
    case controllers_OverFlowController_deletePost39_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost39_invoker.call(OverFlowController_9.get.deletePost(id))
      }
  
    // @LINE:88
    case controllers_OverFlowController_editPost40_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_editPost40_invoker.call(OverFlowController_9.get.editPost(id))
      }
  
    // @LINE:89
    case controllers_OverFlowController_getPostByFilter41_route(params) =>
      call { 
        controllers_OverFlowController_getPostByFilter41_invoker.call(OverFlowController_9.get.getPostByFilter())
      }
  
    // @LINE:90
    case controllers_OverFlowController_getPostLinkedAnswers42_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPostLinkedAnswers42_invoker.call(OverFlowController_9.get.getPostLinkedAnswers(id))
      }
  
    // @LINE:92
    case controllers_OverFlowController_hashTagsListOnPost43_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_hashTagsListOnPost43_invoker.call(OverFlowController_9.get.hashTagsListOnPost(id))
      }
  
    // @LINE:93
    case controllers_OverFlowController_commentsListOnPost44_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_commentsListOnPost44_invoker.call(OverFlowController_9.get.commentsListOnPost(id))
      }
  
    // @LINE:94
    case controllers_OverFlowController_answereListOnPost45_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_answereListOnPost45_invoker.call(OverFlowController_9.get.answereListOnPost(id))
      }
  
    // @LINE:95
    case controllers_OverFlowController_textOfPost46_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_textOfPost46_invoker.call(OverFlowController_9.get.textOfPost(id))
      }
  
    // @LINE:97
    case controllers_OverFlowController_newTypeOfPost47_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfPost47_invoker.call(OverFlowController_9.get.newTypeOfPost())
      }
  
    // @LINE:98
    case controllers_OverFlowController_getTypeOfPost48_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfPost48_invoker.call(OverFlowController_9.get.getTypeOfPost())
      }
  
    // @LINE:100
    case controllers_OverFlowController_newTypeOfConfirms49_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfConfirms49_invoker.call(OverFlowController_9.get.newTypeOfConfirms())
      }
  
    // @LINE:101
    case controllers_OverFlowController_getTypeOfConfirms50_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfConfirms50_invoker.call(OverFlowController_9.get.getTypeOfConfirms())
      }
  
    // @LINE:102
    case controllers_OverFlowController_putTypeOfConfirmToPost51_route(params) =>
      call(params.fromPath[String]("conf", None), params.fromPath[String]("pst", None)) { (conf, pst) =>
        controllers_OverFlowController_putTypeOfConfirmToPost51_invoker.call(OverFlowController_9.get.putTypeOfConfirmToPost(conf, pst))
      }
  
    // @LINE:104
    case controllers_OverFlowController_addComment52_route(params) =>
      call { 
        controllers_OverFlowController_addComment52_invoker.call(OverFlowController_9.get.addComment())
      }
  
    // @LINE:105
    case controllers_OverFlowController_updateComment53_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment53_invoker.call(OverFlowController_9.get.updateComment(id))
      }
  
    // @LINE:106
    case controllers_OverFlowController_deletePost54_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost54_invoker.call(OverFlowController_9.get.deletePost(id))
      }
  
    // @LINE:108
    case controllers_OverFlowController_addAnswer55_route(params) =>
      call { 
        controllers_OverFlowController_addAnswer55_invoker.call(OverFlowController_9.get.addAnswer())
      }
  
    // @LINE:109
    case controllers_OverFlowController_updateComment56_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment56_invoker.call(OverFlowController_9.get.updateComment(id))
      }
  
    // @LINE:110
    case controllers_OverFlowController_deletePost57_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost57_invoker.call(OverFlowController_9.get.deletePost(id))
      }
  
    // @LINE:112
    case controllers_OverFlowController_likePlus58_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likePlus58_invoker.call(OverFlowController_9.get.likePlus(id))
      }
  
    // @LINE:113
    case controllers_OverFlowController_likeMinus59_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likeMinus59_invoker.call(OverFlowController_9.get.likeMinus(id))
      }
  
    // @LINE:114
    case controllers_OverFlowController_linkWithPreviousAnswer60_route(params) =>
      call { 
        controllers_OverFlowController_linkWithPreviousAnswer60_invoker.call(OverFlowController_9.get.linkWithPreviousAnswer())
      }
  
    // @LINE:115
    case controllers_OverFlowController_unlinkWithPreviousAnswer61_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_unlinkWithPreviousAnswer61_invoker.call(OverFlowController_9.get.unlinkWithPreviousAnswer(id))
      }
  
    // @LINE:116
    case controllers_OverFlowController_removeHashTag62_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag62_invoker.call(OverFlowController_9.get.removeHashTag())
      }
  
    // @LINE:117
    case controllers_OverFlowController_addHashTag63_route(params) =>
      call { 
        controllers_OverFlowController_addHashTag63_invoker.call(OverFlowController_9.get.addHashTag())
      }
  
    // @LINE:118
    case controllers_OverFlowController_removeHashTag64_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag64_invoker.call(OverFlowController_9.get.removeHashTag())
      }
  
    // @LINE:127
    case controllers_ProgramingPackageController_postNewProject65_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProject65_invoker.call(ProgramingPackageController_4.get.postNewProject())
      }
  
    // @LINE:128
    case controllers_ProgramingPackageController_updateProject66_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateProject66_invoker.call(ProgramingPackageController_4.get.updateProject(id))
      }
  
    // @LINE:129
    case controllers_ProgramingPackageController_getProject67_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProject67_invoker.call(ProgramingPackageController_4.get.getProject(id))
      }
  
    // @LINE:130
    case controllers_ProgramingPackageController_getProjectsByUserAccount68_route(params) =>
      call { 
        controllers_ProgramingPackageController_getProjectsByUserAccount68_invoker.call(ProgramingPackageController_4.get.getProjectsByUserAccount())
      }
  
    // @LINE:131
    case controllers_ProgramingPackageController_deleteProject69_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteProject69_invoker.call(ProgramingPackageController_4.get.deleteProject(id))
      }
  
    // @LINE:132
    case controllers_ProgramingPackageController_shareProjectWithUsers70_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_shareProjectWithUsers70_invoker.call(ProgramingPackageController_4.get.shareProjectWithUsers(id))
      }
  
    // @LINE:133
    case controllers_ProgramingPackageController_unshareProjectWithUsers71_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_unshareProjectWithUsers71_invoker.call(ProgramingPackageController_4.get.unshareProjectWithUsers(id))
      }
  
    // @LINE:134
    case controllers_ProgramingPackageController_getAll_b_Programs72_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAll_b_Programs72_invoker.call(ProgramingPackageController_4.get.getAll_b_Programs(id))
      }
  
    // @LINE:135
    case controllers_ProgramingPackageController_getAll_c_Programs73_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAll_c_Programs73_invoker.call(ProgramingPackageController_4.get.getAll_c_Programs(id))
      }
  
    // @LINE:136
    case controllers_ProgramingPackageController_getAll_m_Projects74_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAll_m_Projects74_invoker.call(ProgramingPackageController_4.get.getAll_m_Projects(id))
      }
  
    // @LINE:138
    case controllers_ProgramingPackageController_getProgramhomerList75_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramhomerList75_invoker.call(ProgramingPackageController_4.get.getProgramhomerList(id))
      }
  
    // @LINE:139
    case controllers_ProgramingPackageController_getProjectOwners76_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectOwners76_invoker.call(ProgramingPackageController_4.get.getProjectOwners(id))
      }
  
    // @LINE:142
    case controllers_ProgramingPackageController_newHomer77_route(params) =>
      call { 
        controllers_ProgramingPackageController_newHomer77_invoker.call(ProgramingPackageController_4.get.newHomer())
      }
  
    // @LINE:143
    case controllers_ProgramingPackageController_removeHomer78_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeHomer78_invoker.call(ProgramingPackageController_4.get.removeHomer(id))
      }
  
    // @LINE:144
    case controllers_ProgramingPackageController_getHomer79_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getHomer79_invoker.call(ProgramingPackageController_4.get.getHomer(id))
      }
  
    // @LINE:145
    case controllers_ProgramingPackageController_getAllHomers80_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllHomers80_invoker.call(ProgramingPackageController_4.get.getAllHomers())
      }
  
    // @LINE:146
    case controllers_ProgramingPackageController_getConnectedHomers81_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getConnectedHomers81_invoker.call(ProgramingPackageController_4.get.getConnectedHomers(id))
      }
  
    // @LINE:150
    case controllers_ProgramingPackageController_connectHomerWithProject82_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectHomerWithProject82_invoker.call(ProgramingPackageController_4.get.connectHomerWithProject())
      }
  
    // @LINE:151
    case controllers_ProgramingPackageController_disconnectHomerWithProject83_route(params) =>
      call { 
        controllers_ProgramingPackageController_disconnectHomerWithProject83_invoker.call(ProgramingPackageController_4.get.disconnectHomerWithProject())
      }
  
    // @LINE:155
    case controllers_ProgramingPackageController_postNewBProgram84_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewBProgram84_invoker.call(ProgramingPackageController_4.get.postNewBProgram())
      }
  
    // @LINE:156
    case controllers_ProgramingPackageController_getProgram85_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgram85_invoker.call(ProgramingPackageController_4.get.getProgram(id))
      }
  
    // @LINE:157
    case controllers_ProgramingPackageController_editProgram86_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editProgram86_invoker.call(ProgramingPackageController_4.get.editProgram(id))
      }
  
    // @LINE:158
    case controllers_ProgramingPackageController_update_b_program87_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_update_b_program87_invoker.call(ProgramingPackageController_4.get.update_b_program(id))
      }
  
    // @LINE:159
    case controllers_ProgramingPackageController_remove_b_Program88_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_remove_b_Program88_invoker.call(ProgramingPackageController_4.get.remove_b_Program(id))
      }
  
    // @LINE:160
    case controllers_ProgramingPackageController_getProgramInString89_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInString89_invoker.call(ProgramingPackageController_4.get.getProgramInString(id))
      }
  
    // @LINE:161
    case controllers_ProgramingPackageController_uploadProgramToHomer_Immediately90_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("ver", None)) { (id, ver) =>
        controllers_ProgramingPackageController_uploadProgramToHomer_Immediately90_invoker.call(ProgramingPackageController_4.get.uploadProgramToHomer_Immediately(id, ver))
      }
  
    // @LINE:162
    case controllers_ProgramingPackageController_uploadProgramToCloud91_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("ver", None)) { (id, ver) =>
        controllers_ProgramingPackageController_uploadProgramToCloud91_invoker.call(ProgramingPackageController_4.get.uploadProgramToCloud(id, ver))
      }
  
    // @LINE:164
    case controllers_ProgramingPackageController_listOfUploadedHomers92_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfUploadedHomers92_invoker.call(ProgramingPackageController_4.get.listOfUploadedHomers(id))
      }
  
    // @LINE:165
    case controllers_ProgramingPackageController_listOfHomersWaitingForUpload93_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfHomersWaitingForUpload93_invoker.call(ProgramingPackageController_4.get.listOfHomersWaitingForUpload(id))
      }
  
    // @LINE:166
    case controllers_ProgramingPackageController_getProjectsBoard94_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectsBoard94_invoker.call(ProgramingPackageController_4.get.getProjectsBoard(id))
      }
  
    // @LINE:169
    case controllers_ProgramingPackageController_newBlock95_route(params) =>
      call { 
        controllers_ProgramingPackageController_newBlock95_invoker.call(ProgramingPackageController_4.get.newBlock())
      }
  
    // @LINE:170
    case controllers_ProgramingPackageController_updateOfBlock96_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateOfBlock96_invoker.call(ProgramingPackageController_4.get.updateOfBlock(id))
      }
  
    // @LINE:171
    case controllers_ProgramingPackageController_editBlock97_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editBlock97_invoker.call(ProgramingPackageController_4.get.editBlock(id))
      }
  
    // @LINE:172
    case controllers_ProgramingPackageController_getBlockBlock98_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getBlockBlock98_invoker.call(ProgramingPackageController_4.get.getBlockBlock(id))
      }
  
    // @LINE:173
    case controllers_ProgramingPackageController_getBlockVersions99_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getBlockVersions99_invoker.call(ProgramingPackageController_4.get.getBlockVersions(id))
      }
  
    // @LINE:176
    case controllers_ProgramingPackageController_allPrevVersions100_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_allPrevVersions100_invoker.call(ProgramingPackageController_4.get.allPrevVersions(id))
      }
  
    // @LINE:177
    case controllers_ProgramingPackageController_deleteBlockVersion101_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteBlockVersion101_invoker.call(ProgramingPackageController_4.get.deleteBlockVersion(id))
      }
  
    // @LINE:178
    case controllers_ProgramingPackageController_deleteBlock102_route(params) =>
      call(params.fromQuery[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteBlock102_invoker.call(ProgramingPackageController_4.get.deleteBlock(id))
      }
  
    // @LINE:180
    case controllers_ProgramingPackageController_newTypeOfBlock103_route(params) =>
      call { 
        controllers_ProgramingPackageController_newTypeOfBlock103_invoker.call(ProgramingPackageController_4.get.newTypeOfBlock())
      }
  
    // @LINE:181
    case controllers_ProgramingPackageController_getByCategory104_route(params) =>
      call { 
        controllers_ProgramingPackageController_getByCategory104_invoker.call(ProgramingPackageController_4.get.getByCategory())
      }
  
    // @LINE:182
    case controllers_ProgramingPackageController_editTypeOfBlock105_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editTypeOfBlock105_invoker.call(ProgramingPackageController_4.get.editTypeOfBlock(id))
      }
  
    // @LINE:183
    case controllers_ProgramingPackageController_getAllTypeOfBlocks106_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllTypeOfBlocks106_invoker.call(ProgramingPackageController_4.get.getAllTypeOfBlocks())
      }
  
    // @LINE:184
    case controllers_ProgramingPackageController_deleteTypeOfBlock107_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteTypeOfBlock107_invoker.call(ProgramingPackageController_4.get.deleteTypeOfBlock(id))
      }
  
    // @LINE:196
    case controllers_CompilationLibrariesController_create_C_Program108_route(params) =>
      call(params.fromQuery[String]("project_id", None)) { (project_id) =>
        controllers_CompilationLibrariesController_create_C_Program108_invoker.call(CompilationLibrariesController_6.get.create_C_Program(project_id))
      }
  
    // @LINE:197
    case controllers_CompilationLibrariesController_get_C_Program109_route(params) =>
      call(params.fromQuery[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_get_C_Program109_invoker.call(CompilationLibrariesController_6.get.get_C_Program(c_program_id))
      }
  
    // @LINE:198
    case controllers_CompilationLibrariesController_get_C_Program_All_from_Project110_route(params) =>
      call(params.fromQuery[String]("project_id", None)) { (project_id) =>
        controllers_CompilationLibrariesController_get_C_Program_All_from_Project110_invoker.call(CompilationLibrariesController_6.get.get_C_Program_All_from_Project(project_id))
      }
  
    // @LINE:200
    case controllers_CompilationLibrariesController_edit_C_Program_Description111_route(params) =>
      call(params.fromQuery[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_edit_C_Program_Description111_invoker.call(CompilationLibrariesController_6.get.edit_C_Program_Description(c_program_id))
      }
  
    // @LINE:201
    case controllers_CompilationLibrariesController_update_C_Program112_route(params) =>
      call(params.fromQuery[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_update_C_Program112_invoker.call(CompilationLibrariesController_6.get.update_C_Program(c_program_id))
      }
  
    // @LINE:203
    case controllers_CompilationLibrariesController_delete_C_Program113_route(params) =>
      call(params.fromQuery[String]("c_program_id", None)) { (c_program_id) =>
        controllers_CompilationLibrariesController_delete_C_Program113_invoker.call(CompilationLibrariesController_6.get.delete_C_Program(c_program_id))
      }
  
    // @LINE:204
    case controllers_CompilationLibrariesController_delete_C_Program_Version114_route(params) =>
      call(params.fromQuery[String]("c_program_id", None), params.fromQuery[String]("version_id", None)) { (c_program_id, version_id) =>
        controllers_CompilationLibrariesController_delete_C_Program_Version114_invoker.call(CompilationLibrariesController_6.get.delete_C_Program_Version(c_program_id, version_id))
      }
  
    // @LINE:206
    case controllers_CompilationLibrariesController_generateProjectForEclipse115_route(params) =>
      call { 
        controllers_CompilationLibrariesController_generateProjectForEclipse115_invoker.call(CompilationLibrariesController_6.get.generateProjectForEclipse())
      }
  
    // @LINE:207
    case controllers_CompilationLibrariesController_uploadCompilationToBoard116_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("board", None)) { (id, board) =>
        controllers_CompilationLibrariesController_uploadCompilationToBoard116_invoker.call(CompilationLibrariesController_6.get.uploadCompilationToBoard(id, board))
      }
  
    // @LINE:208
    case controllers_CompilationLibrariesController_uploadBinaryFileToBoard117_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_uploadBinaryFileToBoard117_invoker.call(CompilationLibrariesController_6.get.uploadBinaryFileToBoard(id))
      }
  
    // @LINE:210
    case controllers_CompilationLibrariesController_getBoardsFromProject118_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoardsFromProject118_invoker.call(CompilationLibrariesController_6.get.getBoardsFromProject(id))
      }
  
    // @LINE:213
    case controllers_CompilationLibrariesController_new_Processor119_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_Processor119_invoker.call(CompilationLibrariesController_6.get.new_Processor())
      }
  
    // @LINE:214
    case controllers_CompilationLibrariesController_get_Processor120_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_get_Processor120_invoker.call(CompilationLibrariesController_6.get.get_Processor(id))
      }
  
    // @LINE:215
    case controllers_CompilationLibrariesController_get_Processor_All121_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_Processor_All121_invoker.call(CompilationLibrariesController_6.get.get_Processor_All())
      }
  
    // @LINE:216
    case controllers_CompilationLibrariesController_update_Processor122_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_update_Processor122_invoker.call(CompilationLibrariesController_6.get.update_Processor(id))
      }
  
    // @LINE:217
    case controllers_CompilationLibrariesController_delete_Processor123_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_delete_Processor123_invoker.call(CompilationLibrariesController_6.get.delete_Processor(id))
      }
  
    // @LINE:219
    case controllers_CompilationLibrariesController_connectProcessorWithLibrary124_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibrary124_invoker.call(CompilationLibrariesController_6.get.connectProcessorWithLibrary(processor_id, library_id))
      }
  
    // @LINE:220
    case controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup125_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup125_invoker.call(CompilationLibrariesController_6.get.connectProcessorWithLibraryGroup(processor_id, library_id))
      }
  
    // @LINE:221
    case controllers_CompilationLibrariesController_disconnectProcessorWithLibrary126_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_disconnectProcessorWithLibrary126_invoker.call(CompilationLibrariesController_6.get.disconnectProcessorWithLibrary(processor_id, library_id))
      }
  
    // @LINE:222
    case controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup127_route(params) =>
      call(params.fromPath[String]("processor_id", None), params.fromPath[String]("library_id", None)) { (processor_id, library_id) =>
        controllers_CompilationLibrariesController_disconnectProcessorWithLibraryGroup127_invoker.call(CompilationLibrariesController_6.get.disconnectProcessorWithLibraryGroup(processor_id, library_id))
      }
  
    // @LINE:224
    case controllers_CompilationLibrariesController_getProcessorLibraryGroups128_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorLibraryGroups128_invoker.call(CompilationLibrariesController_6.get.getProcessorLibraryGroups(id))
      }
  
    // @LINE:225
    case controllers_CompilationLibrariesController_getProcessorSingleLibraries129_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorSingleLibraries129_invoker.call(CompilationLibrariesController_6.get.getProcessorSingleLibraries(id))
      }
  
    // @LINE:228
    case controllers_CompilationLibrariesController_newBoard130_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newBoard130_invoker.call(CompilationLibrariesController_6.get.newBoard())
      }
  
    // @LINE:229
    case controllers_CompilationLibrariesController_addUserDescription131_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_addUserDescription131_invoker.call(CompilationLibrariesController_6.get.addUserDescription(id))
      }
  
    // @LINE:230
    case controllers_CompilationLibrariesController_getBoardByFilter132_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getBoardByFilter132_invoker.call(CompilationLibrariesController_6.get.getBoardByFilter())
      }
  
    // @LINE:231
    case controllers_CompilationLibrariesController_getBoard133_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoard133_invoker.call(CompilationLibrariesController_6.get.getBoard(id))
      }
  
    // @LINE:232
    case controllers_CompilationLibrariesController_deactivateBoard134_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deactivateBoard134_invoker.call(CompilationLibrariesController_6.get.deactivateBoard(id))
      }
  
    // @LINE:233
    case controllers_CompilationLibrariesController_getUserDescription135_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getUserDescription135_invoker.call(CompilationLibrariesController_6.get.getUserDescription(id))
      }
  
    // @LINE:234
    case controllers_CompilationLibrariesController_connectBoardWthProject136_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("pr", None)) { (id, pr) =>
        controllers_CompilationLibrariesController_connectBoardWthProject136_invoker.call(CompilationLibrariesController_6.get.connectBoardWthProject(id, pr))
      }
  
    // @LINE:235
    case controllers_CompilationLibrariesController_disconnectBoardWthProject137_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("pr", None)) { (id, pr) =>
        controllers_CompilationLibrariesController_disconnectBoardWthProject137_invoker.call(CompilationLibrariesController_6.get.disconnectBoardWthProject(id, pr))
      }
  
    // @LINE:236
    case controllers_CompilationLibrariesController_getBoardProjects138_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoardProjects138_invoker.call(CompilationLibrariesController_6.get.getBoardProjects(id))
      }
  
    // @LINE:240
    case controllers_CompilationLibrariesController_new_Producers139_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_Producers139_invoker.call(CompilationLibrariesController_6.get.new_Producers())
      }
  
    // @LINE:241
    case controllers_CompilationLibrariesController_updateProducers140_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_updateProducers140_invoker.call(CompilationLibrariesController_6.get.updateProducers(producer_id))
      }
  
    // @LINE:242
    case controllers_CompilationLibrariesController_get_Producers141_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_Producers141_invoker.call(CompilationLibrariesController_6.get.get_Producers())
      }
  
    // @LINE:243
    case controllers_CompilationLibrariesController_getProducer142_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_getProducer142_invoker.call(CompilationLibrariesController_6.get.getProducer(producer_id))
      }
  
    // @LINE:244
    case controllers_CompilationLibrariesController_getProducerDescription143_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_getProducerDescription143_invoker.call(CompilationLibrariesController_6.get.getProducerDescription(producer_id))
      }
  
    // @LINE:245
    case controllers_CompilationLibrariesController_getProducerTypeOfBoards144_route(params) =>
      call(params.fromQuery[String]("producer_id", None)) { (producer_id) =>
        controllers_CompilationLibrariesController_getProducerTypeOfBoards144_invoker.call(CompilationLibrariesController_6.get.getProducerTypeOfBoards(producer_id))
      }
  
    // @LINE:248
    case controllers_CompilationLibrariesController_newTypeOfBoard145_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newTypeOfBoard145_invoker.call(CompilationLibrariesController_6.get.newTypeOfBoard())
      }
  
    // @LINE:249
    case controllers_CompilationLibrariesController_updateTypeOfBoard146_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateTypeOfBoard146_invoker.call(CompilationLibrariesController_6.get.updateTypeOfBoard(id))
      }
  
    // @LINE:250
    case controllers_CompilationLibrariesController_getTypeOfBoards147_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getTypeOfBoards147_invoker.call(CompilationLibrariesController_6.get.getTypeOfBoards())
      }
  
    // @LINE:251
    case controllers_CompilationLibrariesController_getTypeOfBoard148_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoard148_invoker.call(CompilationLibrariesController_6.get.getTypeOfBoard(id))
      }
  
    // @LINE:252
    case controllers_CompilationLibrariesController_getTypeOfBoardDescription149_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardDescription149_invoker.call(CompilationLibrariesController_6.get.getTypeOfBoardDescription(id))
      }
  
    // @LINE:253
    case controllers_CompilationLibrariesController_getTypeOfBoardAllBoards150_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardAllBoards150_invoker.call(CompilationLibrariesController_6.get.getTypeOfBoardAllBoards(id))
      }
  
    // @LINE:256
    case controllers_CompilationLibrariesController_new_LibraryGroup151_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_LibraryGroup151_invoker.call(CompilationLibrariesController_6.get.new_LibraryGroup())
      }
  
    // @LINE:257
    case controllers_CompilationLibrariesController_get_LibraryGroup152_route(params) =>
      call(params.fromQuery[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup152_invoker.call(CompilationLibrariesController_6.get.get_LibraryGroup(libraryGroup_id))
      }
  
    // @LINE:258
    case controllers_CompilationLibrariesController_delete_LibraryGroup153_route(params) =>
      call(params.fromQuery[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_delete_LibraryGroup153_invoker.call(CompilationLibrariesController_6.get.delete_LibraryGroup(libraryGroup_id))
      }
  
    // @LINE:259
    case controllers_CompilationLibrariesController_get_LibraryGroup_Filter154_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_LibraryGroup_Filter154_invoker.call(CompilationLibrariesController_6.get.get_LibraryGroup_Filter())
      }
  
    // @LINE:260
    case controllers_CompilationLibrariesController_editLibraryGroup155_route(params) =>
      call(params.fromQuery[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_editLibraryGroup155_invoker.call(CompilationLibrariesController_6.get.editLibraryGroup(libraryGroup_id))
      }
  
    // @LINE:261
    case controllers_CompilationLibrariesController_get_LibraryGroup_Description156_route(params) =>
      call(params.fromQuery[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Description156_invoker.call(CompilationLibrariesController_6.get.get_LibraryGroup_Description(libraryGroup_id))
      }
  
    // @LINE:262
    case controllers_CompilationLibrariesController_get_LibraryGroup_Processors157_route(params) =>
      call(params.fromQuery[String]("libraryGroup_id", None)) { (libraryGroup_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Processors157_invoker.call(CompilationLibrariesController_6.get.get_LibraryGroup_Processors(libraryGroup_id))
      }
  
    // @LINE:263
    case controllers_CompilationLibrariesController_get_LibraryGroup_Libraries158_route(params) =>
      call(params.fromQuery[String]("libraryGroup_id", None), params.fromQuery[String]("version_id", None)) { (libraryGroup_id, version_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Libraries158_invoker.call(CompilationLibrariesController_6.get.get_LibraryGroup_Libraries(libraryGroup_id, version_id))
      }
  
    // @LINE:264
    case controllers_CompilationLibrariesController_new_LibraryGroup_Version159_route(params) =>
      call(params.fromQuery[String]("version_id", None)) { (version_id) =>
        controllers_CompilationLibrariesController_new_LibraryGroup_Version159_invoker.call(CompilationLibrariesController_6.get.new_LibraryGroup_Version(version_id))
      }
  
    // @LINE:265
    case controllers_CompilationLibrariesController_get_LibraryGroup_Version160_route(params) =>
      call(params.fromQuery[String]("version_id", None)) { (version_id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Version160_invoker.call(CompilationLibrariesController_6.get.get_LibraryGroup_Version(version_id))
      }
  
    // @LINE:266
    case controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup161_route(params) =>
      call(params.fromQuery[String]("libraryGroup_id", None), params.fromQuery[String]("version_id", None)) { (libraryGroup_id, version_id) =>
        controllers_CompilationLibrariesController_upload_Library_To_LibraryGroup161_invoker.call(CompilationLibrariesController_6.get.upload_Library_To_LibraryGroup(libraryGroup_id, version_id))
      }
  
    // @LINE:269
    case controllers_CompilationLibrariesController_new_SingleLibrary162_route(params) =>
      call { 
        controllers_CompilationLibrariesController_new_SingleLibrary162_invoker.call(CompilationLibrariesController_6.get.new_SingleLibrary())
      }
  
    // @LINE:270
    case controllers_CompilationLibrariesController_new_SingleLibrary_Version163_route(params) =>
      call(params.fromQuery[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_new_SingleLibrary_Version163_invoker.call(CompilationLibrariesController_6.get.new_SingleLibrary_Version(library_id))
      }
  
    // @LINE:271
    case controllers_CompilationLibrariesController_get_SingleLibrary_Versions164_route(params) =>
      call(params.fromQuery[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_get_SingleLibrary_Versions164_invoker.call(CompilationLibrariesController_6.get.get_SingleLibrary_Versions(library_id))
      }
  
    // @LINE:272
    case controllers_CompilationLibrariesController_get_SingleLibrary_Filter165_route(params) =>
      call { 
        controllers_CompilationLibrariesController_get_SingleLibrary_Filter165_invoker.call(CompilationLibrariesController_6.get.get_SingleLibrary_Filter())
      }
  
    // @LINE:273
    case controllers_CompilationLibrariesController_get_SingleLibrary166_route(params) =>
      call(params.fromQuery[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_get_SingleLibrary166_invoker.call(CompilationLibrariesController_6.get.get_SingleLibrary(library_id))
      }
  
    // @LINE:275
    case controllers_CompilationLibrariesController_edit_SingleLibrary167_route(params) =>
      call(params.fromQuery[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_edit_SingleLibrary167_invoker.call(CompilationLibrariesController_6.get.edit_SingleLibrary(library_id))
      }
  
    // @LINE:276
    case controllers_CompilationLibrariesController_delete_SingleLibrary168_route(params) =>
      call(params.fromQuery[String]("library_id", None)) { (library_id) =>
        controllers_CompilationLibrariesController_delete_SingleLibrary168_invoker.call(CompilationLibrariesController_6.get.delete_SingleLibrary(library_id))
      }
  
    // @LINE:277
    case controllers_CompilationLibrariesController_upload_SingleLibrary_Version169_route(params) =>
      call(params.fromQuery[String]("library_id", None), params.fromQuery[String]("version_id", None)) { (library_id, version_id) =>
        controllers_CompilationLibrariesController_upload_SingleLibrary_Version169_invoker.call(CompilationLibrariesController_6.get.upload_SingleLibrary_Version(library_id, version_id))
      }
  
    // @LINE:280
    case controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries170_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_get_LibraryGroup_Version_Libraries170_invoker.call(CompilationLibrariesController_6.get.get_LibraryGroup_Version_Libraries(id))
      }
  
    // @LINE:281
    case controllers_CompilationLibrariesController_fileRecord171_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_fileRecord171_invoker.call(CompilationLibrariesController_6.get.fileRecord(id))
      }
  
    // @LINE:288
    case controllers_GridController_new_M_Program172_route(params) =>
      call { 
        controllers_GridController_new_M_Program172_invoker.call(GridController_5.get.new_M_Program())
      }
  
    // @LINE:289
    case controllers_GridController_get_M_Program173_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_get_M_Program173_invoker.call(GridController_5.get.get_M_Program(id))
      }
  
    // @LINE:290
    case controllers_GridController_edit_M_Program174_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_edit_M_Program174_invoker.call(GridController_5.get.edit_M_Program(id))
      }
  
    // @LINE:291
    case controllers_GridController_remove_M_Program175_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_remove_M_Program175_invoker.call(GridController_5.get.remove_M_Program(id))
      }
  
    // @LINE:294
    case controllers_GridController_new_M_Program_Screen176_route(params) =>
      call { 
        controllers_GridController_new_M_Program_Screen176_invoker.call(GridController_5.get.new_M_Program_Screen())
      }
  
    // @LINE:295
    case controllers_GridController_get_M_Program_Screen177_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_get_M_Program_Screen177_invoker.call(GridController_5.get.get_M_Program_Screen(id))
      }
  
    // @LINE:296
    case controllers_GridController_edit_M_Program_Screen178_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_edit_M_Program_Screen178_invoker.call(GridController_5.get.edit_M_Program_Screen(id))
      }
  
    // @LINE:297
    case controllers_GridController_remove_M_Program_Screen179_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_remove_M_Program_Screen179_invoker.call(GridController_5.get.remove_M_Program_Screen(id))
      }
  
    // @LINE:301
    case controllers_GridController_new_Screen_Size_Type180_route(params) =>
      call { 
        controllers_GridController_new_Screen_Size_Type180_invoker.call(GridController_5.get.new_Screen_Size_Type())
      }
  
    // @LINE:303
    case controllers_GridController_get_Screen_Size_Type_PublicList181_route(params) =>
      call { 
        controllers_GridController_get_Screen_Size_Type_PublicList181_invoker.call(GridController_5.get.get_Screen_Size_Type_PublicList())
      }
  
    // @LINE:304
    case controllers_GridController_get_Screen_Size_Type_Combination182_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_get_Screen_Size_Type_Combination182_invoker.call(GridController_5.get.get_Screen_Size_Type_Combination(id))
      }
  
    // @LINE:305
    case controllers_GridController_get_Screen_Size_Type183_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_get_Screen_Size_Type183_invoker.call(GridController_5.get.get_Screen_Size_Type(id))
      }
  
    // @LINE:307
    case controllers_GridController_edit_Screen_Size_Type184_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_edit_Screen_Size_Type184_invoker.call(GridController_5.get.edit_Screen_Size_Type(id))
      }
  
    // @LINE:308
    case controllers_GridController_remove_Screen_Size_Type185_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_GridController_remove_Screen_Size_Type185_invoker.call(GridController_5.get.remove_Screen_Size_Type(id))
      }
  
    // @LINE:317
    case utilities_swagger_ApiHelpController_getResources186_route(params) =>
      call { 
        utilities_swagger_ApiHelpController_getResources186_invoker.call(ApiHelpController_2.get.getResources)
      }
  
    // @LINE:320
    case controllers_SecurityController_optionLink187_route(params) =>
      call(params.fromPath[String]("all", None)) { (all) =>
        controllers_SecurityController_optionLink187_invoker.call(SecurityController_3.get.optionLink(all))
      }
  
    // @LINE:323
    case controllers_Assets_at188_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at188_invoker.call(Assets_8.at(path, file))
      }
  }
}
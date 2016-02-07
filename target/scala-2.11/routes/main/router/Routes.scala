
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Sun Feb 07 23:38:53 CET 2016

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
  // @LINE:9
  WikyController_6: javax.inject.Provider[controllers.WikyController],
  // @LINE:14
  WebSocketController_1: javax.inject.Provider[controllers.WebSocketController],
  // @LINE:39
  PersonCreateController_9: javax.inject.Provider[controllers.PersonCreateController],
  // @LINE:53
  PermissionController_0: javax.inject.Provider[controllers.PermissionController],
  // @LINE:64
  OverFlowController_8: javax.inject.Provider[controllers.OverFlowController],
  // @LINE:107
  ProgramingPackageController_4: javax.inject.Provider[controllers.ProgramingPackageController],
  // @LINE:171
  CompilationLibrariesController_5: javax.inject.Provider[controllers.CompilationLibrariesController],
  // @LINE:261
  ApiHelpController_2: javax.inject.Provider[utilities.swagger.ApiHelpController],
  // @LINE:267
  Assets_7: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:8
    SecurityController_3: javax.inject.Provider[controllers.SecurityController],
    // @LINE:9
    WikyController_6: javax.inject.Provider[controllers.WikyController],
    // @LINE:14
    WebSocketController_1: javax.inject.Provider[controllers.WebSocketController],
    // @LINE:39
    PersonCreateController_9: javax.inject.Provider[controllers.PersonCreateController],
    // @LINE:53
    PermissionController_0: javax.inject.Provider[controllers.PermissionController],
    // @LINE:64
    OverFlowController_8: javax.inject.Provider[controllers.OverFlowController],
    // @LINE:107
    ProgramingPackageController_4: javax.inject.Provider[controllers.ProgramingPackageController],
    // @LINE:171
    CompilationLibrariesController_5: javax.inject.Provider[controllers.CompilationLibrariesController],
    // @LINE:261
    ApiHelpController_2: javax.inject.Provider[utilities.swagger.ApiHelpController],
    // @LINE:267
    Assets_7: controllers.Assets
  ) = this(errorHandler, SecurityController_3, WikyController_6, WebSocketController_1, PersonCreateController_9, PermissionController_0, OverFlowController_8, ProgramingPackageController_4, CompilationLibrariesController_5, ApiHelpController_2, Assets_7, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, SecurityController_3, WikyController_6, WebSocketController_1, PersonCreateController_9, PermissionController_0, OverFlowController_8, ProgramingPackageController_4, CompilationLibrariesController_5, ApiHelpController_2, Assets_7, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """@controllers.SecurityController@.index"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test""", """@controllers.WikyController@.test"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test2""", """@controllers.WikyController@.test2(fields:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/$id<[^/]+>""", """@controllers.WebSocketController@.connection(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/webSocketStats""", """@controllers.WebSocketController@.getWebSocketStats()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/sendTo/$id<[^/]+>""", """@controllers.WebSocketController@.sendTo(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/permission/login""", """@controllers.SecurityController@.login()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/permission/logout""", """@controllers.SecurityController@.logout"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/facebook""", """@controllers.SecurityController@.Facebook()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/twitter""", """@controllers.SecurityController@.Twitter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/github""", """@controllers.SecurityController@.GitHub()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/vkontakte""", """@controllers.SecurityController@.Vkontakte()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/person""", """@controllers.SecurityController@.getPersonByToken()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """oauth_callback""", """@controllers.SecurityController@.GET_facebook_oauth(code:String, state:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/facebook/oauth_callback""", """@controllers.SecurityController@.GET_facebook_oauth(code:String, state:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """login/github/oauth_callback""", """@controllers.SecurityController@.GET_github_oauth(code:String, state:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/developer""", """@controllers.PersonCreateController@.developerRegistration()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonCreateController@.standartRegistration()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonCreateController@.updatePersonInformation()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonCreateController@.getPerson(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonCreateController@.deletePerson(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """emailPersonAuthentication/""", """@controllers.PersonCreateController@.emailPersonAuthentitaction(mail:String, authToken:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """permission/permisionKeys""", """@controllers.PermissionController@.getAllPermissions()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """permission/permisionGroups""", """@controllers.PermissionController@.getAllGroups()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """permission/group""", """@controllers.PermissionController@.createGroup()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """permission/personPermission""", """@controllers.PermissionController@.getAllPersonPermission()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """permission/personPermission""", """@controllers.PermissionController@.removeAllPersonPermission()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """permission/personPermission""", """@controllers.PermissionController@.addAllPersonPermission()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post""", """@controllers.OverFlowController@.newPost()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/$id<[^/]+>""", """@controllers.OverFlowController@.getPost(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post/$id<[^/]+>""", """@controllers.OverFlowController@.deletePost(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/post""", """@controllers.OverFlowController@.editPost()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/postAll""", """@controllers.OverFlowController@.getLatestPost()"""),
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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/programs/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramPrograms(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/homerList/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramhomerList(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/owners/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProjectOwners(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer""", """@controllers.ProgramingPackageController@.newHomer()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/$id<[^/]+>""", """@controllers.ProgramingPackageController@.removeHomer(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getHomer(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer""", """@controllers.ProgramingPackageController@.getAllHomers()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/getAllConnectedHomers/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getConnectedHomers(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/connectHomerWithProject""", """@controllers.ProgramingPackageController@.connectHomerWithProject()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/unconnectHomerWithProject""", """@controllers.ProgramingPackageController@.unConnectHomerWithProject()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/program""", """@controllers.ProgramingPackageController@.postNewProgram()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/program/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgram(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/program/$id<[^/]+>""", """@controllers.ProgramingPackageController@.editProgram(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/program/$id<[^/]+>""", """@controllers.ProgramingPackageController@.removeProgram(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/programInJson/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramInJson(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/getallprograms/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getAllPrograms(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/listOfUploadedHomers/$id<[^/]+>""", """@controllers.ProgramingPackageController@.listOfUploadedHomers(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/listOfHomersWaitingForUpload/$id<[^/]+>""", """@controllers.ProgramingPackageController@.listOfHomersWaitingForUpload(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/getProgramInJson/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramInJson(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/boards/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProjectsBoard(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/uploudtohomerImmediately""", """@controllers.ProgramingPackageController@.uploadProgramToHomer_Immediately()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/uploudtohomerAsSoonAsPossible""", """@controllers.ProgramingPackageController@.uploadProgramToHomer_AsSoonAsPossible()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/uploudtohomerGivenTime""", """@controllers.ProgramingPackageController@.uploadProgramToHomer_GivenTimeAsSoonAsPossible()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock""", """@controllers.ProgramingPackageController@.newBlock()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$id<[^/]+>""", """@controllers.ProgramingPackageController@.newVersionOfBlock(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/logicJson/$id<[^/]+>/$version<[^/]+>""", """@controllers.ProgramingPackageController@.logicJsonVersion(id:String, version:Double)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/designJson/$id<[^/]+>/$version<[^/]+>""", """@controllers.ProgramingPackageController@.designJsonVersion(id:String, version:Double)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/logicJson/$id<[^/]+>""", """@controllers.ProgramingPackageController@.logicJsonLast(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/designJson/$id<[^/]+>""", """@controllers.ProgramingPackageController@.designJsonLast(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/generalDescription/$id<[^/]+>""", """@controllers.ProgramingPackageController@.generalDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/versionDescription/$id<[^/]+>""", """@controllers.ProgramingPackageController@.versionDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$id<[^/]+>/$version<[^/]+>""", """@controllers.ProgramingPackageController@.getBlockVersion(id:String, version:Double)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getBlockLast(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/allPrevVersions/$id<[^/]+>""", """@controllers.ProgramingPackageController@.allPrevVersions(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$url<.+>""", """@controllers.ProgramingPackageController@.deleteBlock(url:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/filter""", """@controllers.ProgramingPackageController@.getByFilter()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/program""", """@controllers.CompilationLibrariesController@.newCProgram()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/program/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getCProgram(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/program/project/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.gellAllProgramFromProject(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/program/update/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateCProgramDescription(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/program/newVersion/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.newVersionOfCProgram(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/program/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteCProgram(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/program/$id<[^/]+>/$version<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteVersionOfCProgram(id:String, version:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/project/eclipse""", """@controllers.CompilationLibrariesController@.generateProjectForEclipse()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/project/uploud/$id<[^/]+>/$board<[^/]+>""", """@controllers.CompilationLibrariesController@.uploudCompilationToBoard(id:String, board:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/project/binary/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.uploudBinaryFileToBoard(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor""", """@controllers.CompilationLibrariesController@.newProcessor()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessor(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor""", """@controllers.CompilationLibrariesController@.getProcessorAll()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateProcessor(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteProcessor(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/lbr/$id<[^/]+>/$lbrId<[^/]+>""", """@controllers.CompilationLibrariesController@.connectProcessorWithLibrary(id:String, lbrId:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/lbrgrp/$id<[^/]+>/$lbrgId<[^/]+>""", """@controllers.CompilationLibrariesController@.connectProcessorWithLibraryGroup(id:String, lbrgId:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/lbr/$id<[^/]+>/$lbrId<[^/]+>""", """@controllers.CompilationLibrariesController@.unconnectProcessorWithLibrary(id:String, lbrId:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/lbrgrp/$id<[^/]+>/$lbrgId<[^/]+>""", """@controllers.CompilationLibrariesController@.unconnectProcessorWithLibraryGroup(id:String, lbrgId:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroups/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorLibraryGroups(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/singleLibrary/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorSingleLibraries(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board""", """@controllers.CompilationLibrariesController@.newBoard()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/userDescription/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.addUserDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoard(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/deactivateBoard$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deactivateBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/userDescription/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getUserDescription(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/connect/$id<[^/]+>/$pr<[^/]+>""", """@controllers.CompilationLibrariesController@.connectBoardWthProject(id:String, pr:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/unconnect/$id<[^/]+>/$pr<[^/]+>""", """@controllers.CompilationLibrariesController@.unconnectBoardWthProject(id:String, pr:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/projects/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoardProjects(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.newProducers()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateProducers(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer""", """@controllers.CompilationLibrariesController@.getProducers()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProducer(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProducerDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/producer/typeOfBoards/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProducerTypeOfBoards(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.newTypeOfBoard()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateTypeOfBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard""", """@controllers.CompilationLibrariesController@.getTypeOfBoards()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getTypeOfBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getTypeOfBoardDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/typeOfBoard/boards/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getTypeOfBoardAllBoards(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.newLibraryGroup()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroup(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteLibraryGroup(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.getLibraryGroupAll()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateLibraryGroup(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/generalDescription/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroupDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/processors/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroupProcessors(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/libraries/$libraryId<[^/]+>/$version<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroupLibraries(libraryId:String, version:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/versions/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.createNewVersionLibraryGroup(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/versions/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getVersionLibraryGroup(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/upload/$libraryId<[^/]+>/$version<[^/]+>""", """@controllers.CompilationLibrariesController@.uploudLibraryToLibraryGroup(libraryId:String, version:Double)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/listOfFiles/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.listOfFilesInVersion(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/fileRecord/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.fileRecord(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.newSingleLibrary()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/version/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.newVersionSingleLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/version/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getAllVersionSingleLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/filter""", """@controllers.CompilationLibrariesController@.getSingleLibraryFilter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getSingleLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.getSingleLibraryAll()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateSingleLibrary(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteSingleLibrary(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/uploud/$id<[^/]+>/$version<[^/]+>""", """@controllers.CompilationLibrariesController@.uploadSingleLibraryWithVersion(id:String, version:Double)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/uploud/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.uploadSingleLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getSingleLibraryDescription(id:String)"""),
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

  // @LINE:9
  private[this] lazy val controllers_WikyController_test1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test")))
  )
  private[this] lazy val controllers_WikyController_test1_invoker = createInvoker(
    WikyController_6.get.test,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test",
      Nil,
      "GET",
      """""",
      this.prefix + """test"""
    )
  )

  // @LINE:10
  private[this] lazy val controllers_WikyController_test22_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test2")))
  )
  private[this] lazy val controllers_WikyController_test22_invoker = createInvoker(
    WikyController_6.get.test2(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WikyController",
      "test2",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """test2"""
    )
  )

  // @LINE:14
  private[this] lazy val controllers_WebSocketController_connection3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_WebSocketController_connection3_invoker = createInvoker(
    WebSocketController_1.get.connection(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WebSocketController",
      "connection",
      Seq(classOf[String]),
      "GET",
      """  WEB SOCET  //////////////////////////////////////////////////////////////////////////////""",
      this.prefix + """websocket/$id<[^/]+>"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_WebSocketController_getWebSocketStats4_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/webSocketStats")))
  )
  private[this] lazy val controllers_WebSocketController_getWebSocketStats4_invoker = createInvoker(
    WebSocketController_1.get.getWebSocketStats(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WebSocketController",
      "getWebSocketStats",
      Nil,
      "POST",
      """""",
      this.prefix + """websocket/webSocketStats"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_WebSocketController_sendTo5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/sendTo/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_WebSocketController_sendTo5_invoker = createInvoker(
    WebSocketController_1.get.sendTo(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.WebSocketController",
      "sendTo",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """websocket/sendTo/$id<[^/]+>"""
    )
  )

  // @LINE:20
  private[this] lazy val controllers_SecurityController_login6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/login")))
  )
  private[this] lazy val controllers_SecurityController_login6_invoker = createInvoker(
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

  // @LINE:21
  private[this] lazy val controllers_SecurityController_logout7_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/logout")))
  )
  private[this] lazy val controllers_SecurityController_logout7_invoker = createInvoker(
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

  // @LINE:23
  private[this] lazy val controllers_SecurityController_Facebook8_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/facebook")))
  )
  private[this] lazy val controllers_SecurityController_Facebook8_invoker = createInvoker(
    SecurityController_3.get.Facebook(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "Facebook",
      Nil,
      "GET",
      """""",
      this.prefix + """login/facebook"""
    )
  )

  // @LINE:24
  private[this] lazy val controllers_SecurityController_Twitter9_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/twitter")))
  )
  private[this] lazy val controllers_SecurityController_Twitter9_invoker = createInvoker(
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

  // @LINE:25
  private[this] lazy val controllers_SecurityController_GitHub10_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/github")))
  )
  private[this] lazy val controllers_SecurityController_GitHub10_invoker = createInvoker(
    SecurityController_3.get.GitHub(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "GitHub",
      Nil,
      "GET",
      """""",
      this.prefix + """login/github"""
    )
  )

  // @LINE:26
  private[this] lazy val controllers_SecurityController_Vkontakte11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/vkontakte")))
  )
  private[this] lazy val controllers_SecurityController_Vkontakte11_invoker = createInvoker(
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

  // @LINE:28
  private[this] lazy val controllers_SecurityController_getPersonByToken12_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/person")))
  )
  private[this] lazy val controllers_SecurityController_getPersonByToken12_invoker = createInvoker(
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

  // @LINE:30
  private[this] lazy val controllers_SecurityController_GET_facebook_oauth13_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("oauth_callback")))
  )
  private[this] lazy val controllers_SecurityController_GET_facebook_oauth13_invoker = createInvoker(
    SecurityController_3.get.GET_facebook_oauth(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "GET_facebook_oauth",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """oauth_callback"""
    )
  )

  // @LINE:31
  private[this] lazy val controllers_SecurityController_GET_facebook_oauth14_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/facebook/oauth_callback")))
  )
  private[this] lazy val controllers_SecurityController_GET_facebook_oauth14_invoker = createInvoker(
    SecurityController_3.get.GET_facebook_oauth(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "GET_facebook_oauth",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """login/facebook/oauth_callback"""
    )
  )

  // @LINE:33
  private[this] lazy val controllers_SecurityController_GET_github_oauth15_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("login/github/oauth_callback")))
  )
  private[this] lazy val controllers_SecurityController_GET_github_oauth15_invoker = createInvoker(
    SecurityController_3.get.GET_github_oauth(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "GET_github_oauth",
      Seq(classOf[String], classOf[String]),
      "GET",
      """GET             /login/twitter/oauth_callback              @controllers.SecurityController.GET_twitter_oauth(code, state)""",
      this.prefix + """login/github/oauth_callback"""
    )
  )

  // @LINE:39
  private[this] lazy val controllers_PersonCreateController_developerRegistration16_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/developer")))
  )
  private[this] lazy val controllers_PersonCreateController_developerRegistration16_invoker = createInvoker(
    PersonCreateController_9.get.developerRegistration(),
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

  // @LINE:40
  private[this] lazy val controllers_PersonCreateController_standartRegistration17_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_standartRegistration17_invoker = createInvoker(
    PersonCreateController_9.get.standartRegistration(),
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

  // @LINE:41
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation18_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation18_invoker = createInvoker(
    PersonCreateController_9.get.updatePersonInformation(),
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

  // @LINE:42
  private[this] lazy val controllers_PersonCreateController_getPerson19_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_getPerson19_invoker = createInvoker(
    PersonCreateController_9.get.getPerson(fakeValue[String]),
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

  // @LINE:44
  private[this] lazy val controllers_PersonCreateController_deletePerson20_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_deletePerson20_invoker = createInvoker(
    PersonCreateController_9.get.deletePerson(fakeValue[String]),
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

  // @LINE:46
  private[this] lazy val controllers_PersonCreateController_emailPersonAuthentitaction21_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("emailPersonAuthentication/")))
  )
  private[this] lazy val controllers_PersonCreateController_emailPersonAuthentitaction21_invoker = createInvoker(
    PersonCreateController_9.get.emailPersonAuthentitaction(fakeValue[String], fakeValue[String]),
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

  // @LINE:53
  private[this] lazy val controllers_PermissionController_getAllPermissions22_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionKeys")))
  )
  private[this] lazy val controllers_PermissionController_getAllPermissions22_invoker = createInvoker(
    PermissionController_0.get.getAllPermissions(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "getAllPermissions",
      Nil,
      "GET",
      """## PERMISSION ############ PERMISSION ############### PERMISSION ################ PERMISSION ################# PERMISSION #####################################################
###############################################################################################################################################################################""",
      this.prefix + """permission/permisionKeys"""
    )
  )

  // @LINE:54
  private[this] lazy val controllers_PermissionController_getAllGroups23_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionGroups")))
  )
  private[this] lazy val controllers_PermissionController_getAllGroups23_invoker = createInvoker(
    PermissionController_0.get.getAllGroups(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "getAllGroups",
      Nil,
      "GET",
      """""",
      this.prefix + """permission/permisionGroups"""
    )
  )

  // @LINE:55
  private[this] lazy val controllers_PermissionController_createGroup24_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/group")))
  )
  private[this] lazy val controllers_PermissionController_createGroup24_invoker = createInvoker(
    PermissionController_0.get.createGroup(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "createGroup",
      Nil,
      "POST",
      """""",
      this.prefix + """permission/group"""
    )
  )

  // @LINE:57
  private[this] lazy val controllers_PermissionController_getAllPersonPermission25_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_getAllPersonPermission25_invoker = createInvoker(
    PermissionController_0.get.getAllPersonPermission(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "getAllPersonPermission",
      Nil,
      "GET",
      """""",
      this.prefix + """permission/personPermission"""
    )
  )

  // @LINE:58
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission26_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission26_invoker = createInvoker(
    PermissionController_0.get.removeAllPersonPermission(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "removeAllPersonPermission",
      Nil,
      "DELETE",
      """""",
      this.prefix + """permission/personPermission"""
    )
  )

  // @LINE:59
  private[this] lazy val controllers_PermissionController_addAllPersonPermission27_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_addAllPersonPermission27_invoker = createInvoker(
    PermissionController_0.get.addAllPersonPermission(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PermissionController",
      "addAllPersonPermission",
      Nil,
      "PUT",
      """""",
      this.prefix + """permission/personPermission"""
    )
  )

  // @LINE:64
  private[this] lazy val controllers_OverFlowController_newPost28_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_newPost28_invoker = createInvoker(
    OverFlowController_8.get.newPost(),
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

  // @LINE:65
  private[this] lazy val controllers_OverFlowController_getPost29_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPost29_invoker = createInvoker(
    OverFlowController_8.get.getPost(fakeValue[String]),
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

  // @LINE:66
  private[this] lazy val controllers_OverFlowController_deletePost30_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost30_invoker = createInvoker(
    OverFlowController_8.get.deletePost(fakeValue[String]),
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

  // @LINE:67
  private[this] lazy val controllers_OverFlowController_editPost31_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_editPost31_invoker = createInvoker(
    OverFlowController_8.get.editPost(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "editPost",
      Nil,
      "PUT",
      """""",
      this.prefix + """overflow/post"""
    )
  )

  // @LINE:68
  private[this] lazy val controllers_OverFlowController_getLatestPost32_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postAll")))
  )
  private[this] lazy val controllers_OverFlowController_getLatestPost32_invoker = createInvoker(
    OverFlowController_8.get.getLatestPost(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "getLatestPost",
      Nil,
      "GET",
      """""",
      this.prefix + """overflow/postAll"""
    )
  )

  // @LINE:69
  private[this] lazy val controllers_OverFlowController_getPostByFilter33_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postFilter")))
  )
  private[this] lazy val controllers_OverFlowController_getPostByFilter33_invoker = createInvoker(
    OverFlowController_8.get.getPostByFilter(),
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

  // @LINE:70
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers34_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/linkedAnswers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers34_invoker = createInvoker(
    OverFlowController_8.get.getPostLinkedAnswers(fakeValue[String]),
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

  // @LINE:72
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost35_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/hashTags/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost35_invoker = createInvoker(
    OverFlowController_8.get.hashTagsListOnPost(fakeValue[String]),
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

  // @LINE:73
  private[this] lazy val controllers_OverFlowController_commentsListOnPost36_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/comments/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_commentsListOnPost36_invoker = createInvoker(
    OverFlowController_8.get.commentsListOnPost(fakeValue[String]),
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

  // @LINE:74
  private[this] lazy val controllers_OverFlowController_answereListOnPost37_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/answers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_answereListOnPost37_invoker = createInvoker(
    OverFlowController_8.get.answereListOnPost(fakeValue[String]),
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

  // @LINE:75
  private[this] lazy val controllers_OverFlowController_textOfPost38_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/textOfPost/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_textOfPost38_invoker = createInvoker(
    OverFlowController_8.get.textOfPost(fakeValue[String]),
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

  // @LINE:77
  private[this] lazy val controllers_OverFlowController_newTypeOfPost39_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfPost39_invoker = createInvoker(
    OverFlowController_8.get.newTypeOfPost(),
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

  // @LINE:78
  private[this] lazy val controllers_OverFlowController_getTypeOfPost40_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfPost40_invoker = createInvoker(
    OverFlowController_8.get.getTypeOfPost(),
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

  // @LINE:80
  private[this] lazy val controllers_OverFlowController_newTypeOfConfirms41_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfConfirms41_invoker = createInvoker(
    OverFlowController_8.get.newTypeOfConfirms(),
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

  // @LINE:81
  private[this] lazy val controllers_OverFlowController_getTypeOfConfirms42_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfConfirms42_invoker = createInvoker(
    OverFlowController_8.get.getTypeOfConfirms(),
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

  // @LINE:82
  private[this] lazy val controllers_OverFlowController_putTypeOfConfirmToPost43_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfConfirm/"), DynamicPart("conf", """[^/]+""",true), StaticPart("/"), DynamicPart("pst", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_putTypeOfConfirmToPost43_invoker = createInvoker(
    OverFlowController_8.get.putTypeOfConfirmToPost(fakeValue[String], fakeValue[String]),
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

  // @LINE:84
  private[this] lazy val controllers_OverFlowController_addComment44_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment")))
  )
  private[this] lazy val controllers_OverFlowController_addComment44_invoker = createInvoker(
    OverFlowController_8.get.addComment(),
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

  // @LINE:85
  private[this] lazy val controllers_OverFlowController_updateComment45_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment45_invoker = createInvoker(
    OverFlowController_8.get.updateComment(fakeValue[String]),
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

  // @LINE:86
  private[this] lazy val controllers_OverFlowController_deletePost46_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost46_invoker = createInvoker(
    OverFlowController_8.get.deletePost(fakeValue[String]),
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

  // @LINE:88
  private[this] lazy val controllers_OverFlowController_addAnswer47_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer")))
  )
  private[this] lazy val controllers_OverFlowController_addAnswer47_invoker = createInvoker(
    OverFlowController_8.get.addAnswer(),
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

  // @LINE:89
  private[this] lazy val controllers_OverFlowController_updateComment48_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment48_invoker = createInvoker(
    OverFlowController_8.get.updateComment(fakeValue[String]),
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

  // @LINE:90
  private[this] lazy val controllers_OverFlowController_deletePost49_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost49_invoker = createInvoker(
    OverFlowController_8.get.deletePost(fakeValue[String]),
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

  // @LINE:92
  private[this] lazy val controllers_OverFlowController_likePlus50_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likePlus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likePlus50_invoker = createInvoker(
    OverFlowController_8.get.likePlus(fakeValue[String]),
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

  // @LINE:93
  private[this] lazy val controllers_OverFlowController_likeMinus51_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likeMinus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likeMinus51_invoker = createInvoker(
    OverFlowController_8.get.likeMinus(fakeValue[String]),
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

  // @LINE:94
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer52_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link")))
  )
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer52_invoker = createInvoker(
    OverFlowController_8.get.linkWithPreviousAnswer(),
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

  // @LINE:95
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer53_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer53_invoker = createInvoker(
    OverFlowController_8.get.unlinkWithPreviousAnswer(fakeValue[String]),
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

  // @LINE:96
  private[this] lazy val controllers_OverFlowController_removeHashTag54_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeLink")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag54_invoker = createInvoker(
    OverFlowController_8.get.removeHashTag(),
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

  // @LINE:97
  private[this] lazy val controllers_OverFlowController_addHashTag55_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/hashTag")))
  )
  private[this] lazy val controllers_OverFlowController_addHashTag55_invoker = createInvoker(
    OverFlowController_8.get.addHashTag(),
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

  // @LINE:98
  private[this] lazy val controllers_OverFlowController_removeHashTag56_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeHashTag")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag56_invoker = createInvoker(
    OverFlowController_8.get.removeHashTag(),
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

  // @LINE:107
  private[this] lazy val controllers_ProgramingPackageController_postNewProject57_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProject57_invoker = createInvoker(
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

  // @LINE:108
  private[this] lazy val controllers_ProgramingPackageController_updateProject58_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateProject58_invoker = createInvoker(
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

  // @LINE:109
  private[this] lazy val controllers_ProgramingPackageController_getProject59_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProject59_invoker = createInvoker(
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

  // @LINE:110
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount60_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount60_invoker = createInvoker(
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

  // @LINE:111
  private[this] lazy val controllers_ProgramingPackageController_deleteProject61_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteProject61_invoker = createInvoker(
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

  // @LINE:112
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers62_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/shareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers62_invoker = createInvoker(
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

  // @LINE:113
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers63_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/unshareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers63_invoker = createInvoker(
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

  // @LINE:114
  private[this] lazy val controllers_ProgramingPackageController_getProgramPrograms64_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/programs/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramPrograms64_invoker = createInvoker(
    ProgramingPackageController_4.get.getProgramPrograms(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgramPrograms",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/programs/$id<[^/]+>"""
    )
  )

  // @LINE:115
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList65_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/homerList/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList65_invoker = createInvoker(
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

  // @LINE:116
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners66_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/owners/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners66_invoker = createInvoker(
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

  // @LINE:119
  private[this] lazy val controllers_ProgramingPackageController_newHomer67_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newHomer67_invoker = createInvoker(
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

  // @LINE:120
  private[this] lazy val controllers_ProgramingPackageController_removeHomer68_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeHomer68_invoker = createInvoker(
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

  // @LINE:121
  private[this] lazy val controllers_ProgramingPackageController_getHomer69_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getHomer69_invoker = createInvoker(
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

  // @LINE:122
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers70_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers70_invoker = createInvoker(
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

  // @LINE:123
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers71_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/getAllConnectedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers71_invoker = createInvoker(
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

  // @LINE:128
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject72_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject72_invoker = createInvoker(
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

  // @LINE:129
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject73_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/unconnectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject73_invoker = createInvoker(
    ProgramingPackageController_4.get.unConnectHomerWithProject(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "unConnectHomerWithProject",
      Nil,
      "PUT",
      """""",
      this.prefix + """project/unconnectHomerWithProject"""
    )
  )

  // @LINE:132
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram74_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram74_invoker = createInvoker(
    ProgramingPackageController_4.get.postNewProgram(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "postNewProgram",
      Nil,
      "POST",
      """Program""",
      this.prefix + """project/program"""
    )
  )

  // @LINE:133
  private[this] lazy val controllers_ProgramingPackageController_getProgram75_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgram75_invoker = createInvoker(
    ProgramingPackageController_4.get.getProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgram",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/program/$id<[^/]+>"""
    )
  )

  // @LINE:134
  private[this] lazy val controllers_ProgramingPackageController_editProgram76_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editProgram76_invoker = createInvoker(
    ProgramingPackageController_4.get.editProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "editProgram",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/program/$id<[^/]+>"""
    )
  )

  // @LINE:135
  private[this] lazy val controllers_ProgramingPackageController_removeProgram77_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeProgram77_invoker = createInvoker(
    ProgramingPackageController_4.get.removeProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "removeProgram",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/program/$id<[^/]+>"""
    )
  )

  // @LINE:136
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson78_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/programInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson78_invoker = createInvoker(
    ProgramingPackageController_4.get.getProgramInJson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgramInJson",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/programInJson/$id<[^/]+>"""
    )
  )

  // @LINE:139
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms79_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getallprograms/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms79_invoker = createInvoker(
    ProgramingPackageController_4.get.getAllPrograms(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getAllPrograms",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/getallprograms/$id<[^/]+>"""
    )
  )

  // @LINE:140
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers80_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfUploadedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers80_invoker = createInvoker(
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

  // @LINE:141
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload81_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfHomersWaitingForUpload/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload81_invoker = createInvoker(
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

  // @LINE:142
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson82_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getProgramInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson82_invoker = createInvoker(
    ProgramingPackageController_4.get.getProgramInJson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgramInJson",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/getProgramInJson/$id<[^/]+>"""
    )
  )

  // @LINE:143
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard83_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard83_invoker = createInvoker(
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

  // @LINE:144
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately84_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerImmediately")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately84_invoker = createInvoker(
    ProgramingPackageController_4.get.uploadProgramToHomer_Immediately(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "uploadProgramToHomer_Immediately",
      Nil,
      "PUT",
      """""",
      this.prefix + """project/uploudtohomerImmediately"""
    )
  )

  // @LINE:145
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible85_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerAsSoonAsPossible")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible85_invoker = createInvoker(
    ProgramingPackageController_4.get.uploadProgramToHomer_AsSoonAsPossible(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "uploadProgramToHomer_AsSoonAsPossible",
      Nil,
      "PUT",
      """""",
      this.prefix + """project/uploudtohomerAsSoonAsPossible"""
    )
  )

  // @LINE:146
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible86_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerGivenTime")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible86_invoker = createInvoker(
    ProgramingPackageController_4.get.uploadProgramToHomer_GivenTimeAsSoonAsPossible(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "uploadProgramToHomer_GivenTimeAsSoonAsPossible",
      Nil,
      "PUT",
      """""",
      this.prefix + """project/uploudtohomerGivenTime"""
    )
  )

  // @LINE:149
  private[this] lazy val controllers_ProgramingPackageController_newBlock87_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newBlock87_invoker = createInvoker(
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

  // @LINE:150
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock88_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock88_invoker = createInvoker(
    ProgramingPackageController_4.get.newVersionOfBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "newVersionOfBlock",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """project/blockoBlock/$id<[^/]+>"""
    )
  )

  // @LINE:151
  private[this] lazy val controllers_ProgramingPackageController_logicJsonVersion89_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/logicJson/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_logicJsonVersion89_invoker = createInvoker(
    ProgramingPackageController_4.get.logicJsonVersion(fakeValue[String], fakeValue[Double]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "logicJsonVersion",
      Seq(classOf[String], classOf[Double]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/logicJson/$id<[^/]+>/$version<[^/]+>"""
    )
  )

  // @LINE:152
  private[this] lazy val controllers_ProgramingPackageController_designJsonVersion90_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/designJson/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_designJsonVersion90_invoker = createInvoker(
    ProgramingPackageController_4.get.designJsonVersion(fakeValue[String], fakeValue[Double]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "designJsonVersion",
      Seq(classOf[String], classOf[Double]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/designJson/$id<[^/]+>/$version<[^/]+>"""
    )
  )

  // @LINE:153
  private[this] lazy val controllers_ProgramingPackageController_logicJsonLast91_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/logicJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_logicJsonLast91_invoker = createInvoker(
    ProgramingPackageController_4.get.logicJsonLast(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "logicJsonLast",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/logicJson/$id<[^/]+>"""
    )
  )

  // @LINE:154
  private[this] lazy val controllers_ProgramingPackageController_designJsonLast92_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/designJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_designJsonLast92_invoker = createInvoker(
    ProgramingPackageController_4.get.designJsonLast(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "designJsonLast",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/designJson/$id<[^/]+>"""
    )
  )

  // @LINE:155
  private[this] lazy val controllers_ProgramingPackageController_generalDescription93_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_generalDescription93_invoker = createInvoker(
    ProgramingPackageController_4.get.generalDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "generalDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/generalDescription/$id<[^/]+>"""
    )
  )

  // @LINE:156
  private[this] lazy val controllers_ProgramingPackageController_versionDescription94_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/versionDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_versionDescription94_invoker = createInvoker(
    ProgramingPackageController_4.get.versionDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "versionDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/versionDescription/$id<[^/]+>"""
    )
  )

  // @LINE:157
  private[this] lazy val controllers_ProgramingPackageController_getBlockVersion95_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlockVersion95_invoker = createInvoker(
    ProgramingPackageController_4.get.getBlockVersion(fakeValue[String], fakeValue[Double]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getBlockVersion",
      Seq(classOf[String], classOf[Double]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/$id<[^/]+>/$version<[^/]+>"""
    )
  )

  // @LINE:158
  private[this] lazy val controllers_ProgramingPackageController_getBlockLast96_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlockLast96_invoker = createInvoker(
    ProgramingPackageController_4.get.getBlockLast(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getBlockLast",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/$id<[^/]+>"""
    )
  )

  // @LINE:160
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions97_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/allPrevVersions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions97_invoker = createInvoker(
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

  // @LINE:161
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock98_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock98_invoker = createInvoker(
    ProgramingPackageController_4.get.deleteBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "deleteBlock",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """project/blockoBlock/$url<.+>"""
    )
  )

  // @LINE:162
  private[this] lazy val controllers_ProgramingPackageController_getByFilter99_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/filter")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getByFilter99_invoker = createInvoker(
    ProgramingPackageController_4.get.getByFilter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getByFilter",
      Nil,
      "POST",
      """""",
      this.prefix + """project/blockoBlock/filter"""
    )
  )

  // @LINE:171
  private[this] lazy val controllers_CompilationLibrariesController_newCProgram100_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/program")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newCProgram100_invoker = createInvoker(
    CompilationLibrariesController_5.get.newCProgram(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newCProgram",
      Nil,
      "POST",
      """C:Program""",
      this.prefix + """compilation/program"""
    )
  )

  // @LINE:172
  private[this] lazy val controllers_CompilationLibrariesController_getCProgram101_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getCProgram101_invoker = createInvoker(
    CompilationLibrariesController_5.get.getCProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getCProgram",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/program/$id<[^/]+>"""
    )
  )

  // @LINE:173
  private[this] lazy val controllers_CompilationLibrariesController_gellAllProgramFromProject102_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/program/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_gellAllProgramFromProject102_invoker = createInvoker(
    CompilationLibrariesController_5.get.gellAllProgramFromProject(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "gellAllProgramFromProject",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/program/project/$id<[^/]+>"""
    )
  )

  // @LINE:175
  private[this] lazy val controllers_CompilationLibrariesController_updateCProgramDescription103_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/program/update/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateCProgramDescription103_invoker = createInvoker(
    CompilationLibrariesController_5.get.updateCProgramDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateCProgramDescription",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/program/update/$id<[^/]+>"""
    )
  )

  // @LINE:176
  private[this] lazy val controllers_CompilationLibrariesController_newVersionOfCProgram104_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/program/newVersion/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newVersionOfCProgram104_invoker = createInvoker(
    CompilationLibrariesController_5.get.newVersionOfCProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newVersionOfCProgram",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/program/newVersion/$id<[^/]+>"""
    )
  )

  // @LINE:178
  private[this] lazy val controllers_CompilationLibrariesController_deleteCProgram105_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteCProgram105_invoker = createInvoker(
    CompilationLibrariesController_5.get.deleteCProgram(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deleteCProgram",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/program/$id<[^/]+>"""
    )
  )

  // @LINE:179
  private[this] lazy val controllers_CompilationLibrariesController_deleteVersionOfCProgram106_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/program/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteVersionOfCProgram106_invoker = createInvoker(
    CompilationLibrariesController_5.get.deleteVersionOfCProgram(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deleteVersionOfCProgram",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/program/$id<[^/]+>/$version<[^/]+>"""
    )
  )

  // @LINE:181
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse107_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/eclipse")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse107_invoker = createInvoker(
    CompilationLibrariesController_5.get.generateProjectForEclipse(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "generateProjectForEclipse",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/project/eclipse"""
    )
  )

  // @LINE:182
  private[this] lazy val controllers_CompilationLibrariesController_uploudCompilationToBoard108_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/uploud/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("board", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploudCompilationToBoard108_invoker = createInvoker(
    CompilationLibrariesController_5.get.uploudCompilationToBoard(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploudCompilationToBoard",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/project/uploud/$id<[^/]+>/$board<[^/]+>"""
    )
  )

  // @LINE:183
  private[this] lazy val controllers_CompilationLibrariesController_uploudBinaryFileToBoard109_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/binary/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploudBinaryFileToBoard109_invoker = createInvoker(
    CompilationLibrariesController_5.get.uploudBinaryFileToBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploudBinaryFileToBoard",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/project/binary/$id<[^/]+>"""
    )
  )

  // @LINE:186
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor110_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor110_invoker = createInvoker(
    CompilationLibrariesController_5.get.newProcessor(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newProcessor",
      Nil,
      "POST",
      """Processor""",
      this.prefix + """compilation/processor"""
    )
  )

  // @LINE:187
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor111_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor111_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProcessor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessor",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/$id<[^/]+>"""
    )
  )

  // @LINE:188
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll112_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll112_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProcessorAll(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorAll",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/processor"""
    )
  )

  // @LINE:189
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor113_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor113_invoker = createInvoker(
    CompilationLibrariesController_5.get.updateProcessor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateProcessor",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/processor/$id<[^/]+>"""
    )
  )

  // @LINE:190
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor114_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor114_invoker = createInvoker(
    CompilationLibrariesController_5.get.deleteProcessor(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deleteProcessor",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/processor/$id<[^/]+>"""
    )
  )

  // @LINE:192
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary115_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbr/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary115_invoker = createInvoker(
    CompilationLibrariesController_5.get.connectProcessorWithLibrary(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "connectProcessorWithLibrary",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/processor/lbr/$id<[^/]+>/$lbrId<[^/]+>"""
    )
  )

  // @LINE:193
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup116_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbrgrp/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrgId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup116_invoker = createInvoker(
    CompilationLibrariesController_5.get.connectProcessorWithLibraryGroup(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "connectProcessorWithLibraryGroup",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/processor/lbrgrp/$id<[^/]+>/$lbrgId<[^/]+>"""
    )
  )

  // @LINE:194
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibrary117_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbr/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibrary117_invoker = createInvoker(
    CompilationLibrariesController_5.get.unconnectProcessorWithLibrary(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "unconnectProcessorWithLibrary",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/processor/lbr/$id<[^/]+>/$lbrId<[^/]+>"""
    )
  )

  // @LINE:195
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup118_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbrgrp/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrgId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup118_invoker = createInvoker(
    CompilationLibrariesController_5.get.unconnectProcessorWithLibraryGroup(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "unconnectProcessorWithLibraryGroup",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/processor/lbrgrp/$id<[^/]+>/$lbrgId<[^/]+>"""
    )
  )

  // @LINE:197
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription119_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription119_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProcessorDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/processor/description/$id<[^/]+>"""
    )
  )

  // @LINE:198
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups120_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroups/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups120_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProcessorLibraryGroups(fakeValue[String]),
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

  // @LINE:199
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries121_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/singleLibrary/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries121_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProcessorSingleLibraries(fakeValue[String]),
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

  // @LINE:202
  private[this] lazy val controllers_CompilationLibrariesController_newBoard122_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newBoard122_invoker = createInvoker(
    CompilationLibrariesController_5.get.newBoard(),
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

  // @LINE:203
  private[this] lazy val controllers_CompilationLibrariesController_addUserDescription123_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_addUserDescription123_invoker = createInvoker(
    CompilationLibrariesController_5.get.addUserDescription(fakeValue[String]),
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

  // @LINE:204
  private[this] lazy val controllers_CompilationLibrariesController_getBoard124_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoard124_invoker = createInvoker(
    CompilationLibrariesController_5.get.getBoard(fakeValue[String]),
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

  // @LINE:205
  private[this] lazy val controllers_CompilationLibrariesController_deactivateBoard125_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/deactivateBoard"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deactivateBoard125_invoker = createInvoker(
    CompilationLibrariesController_5.get.deactivateBoard(fakeValue[String]),
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

  // @LINE:206
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription126_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription126_invoker = createInvoker(
    CompilationLibrariesController_5.get.getUserDescription(fakeValue[String]),
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

  // @LINE:207
  private[this] lazy val controllers_CompilationLibrariesController_connectBoardWthProject127_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/connect/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("pr", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectBoardWthProject127_invoker = createInvoker(
    CompilationLibrariesController_5.get.connectBoardWthProject(fakeValue[String], fakeValue[String]),
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

  // @LINE:208
  private[this] lazy val controllers_CompilationLibrariesController_unconnectBoardWthProject128_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/unconnect/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("pr", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_unconnectBoardWthProject128_invoker = createInvoker(
    CompilationLibrariesController_5.get.unconnectBoardWthProject(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "unconnectBoardWthProject",
      Seq(classOf[String], classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/unconnect/$id<[^/]+>/$pr<[^/]+>"""
    )
  )

  // @LINE:209
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects129_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/projects/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects129_invoker = createInvoker(
    CompilationLibrariesController_5.get.getBoardProjects(fakeValue[String]),
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

  // @LINE:212
  private[this] lazy val controllers_CompilationLibrariesController_newProducers130_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProducers130_invoker = createInvoker(
    CompilationLibrariesController_5.get.newProducers(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newProducers",
      Nil,
      "POST",
      """Producer""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:213
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers131_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers131_invoker = createInvoker(
    CompilationLibrariesController_5.get.updateProducers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateProducers",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/producer/$id<[^/]+>"""
    )
  )

  // @LINE:214
  private[this] lazy val controllers_CompilationLibrariesController_getProducers132_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducers132_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProducers(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProducers",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/producer"""
    )
  )

  // @LINE:215
  private[this] lazy val controllers_CompilationLibrariesController_getProducer133_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducer133_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProducer(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProducer",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer/$id<[^/]+>"""
    )
  )

  // @LINE:216
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription134_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription134_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProducerDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProducerDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer/description/$id<[^/]+>"""
    )
  )

  // @LINE:217
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards135_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/typeOfBoards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards135_invoker = createInvoker(
    CompilationLibrariesController_5.get.getProducerTypeOfBoards(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProducerTypeOfBoards",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/producer/typeOfBoards/$id<[^/]+>"""
    )
  )

  // @LINE:220
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard136_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard136_invoker = createInvoker(
    CompilationLibrariesController_5.get.newTypeOfBoard(),
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

  // @LINE:221
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard137_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard137_invoker = createInvoker(
    CompilationLibrariesController_5.get.updateTypeOfBoard(fakeValue[String]),
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

  // @LINE:222
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards138_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards138_invoker = createInvoker(
    CompilationLibrariesController_5.get.getTypeOfBoards(),
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

  // @LINE:223
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard139_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard139_invoker = createInvoker(
    CompilationLibrariesController_5.get.getTypeOfBoard(fakeValue[String]),
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

  // @LINE:224
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription140_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription140_invoker = createInvoker(
    CompilationLibrariesController_5.get.getTypeOfBoardDescription(fakeValue[String]),
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

  // @LINE:225
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards141_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards141_invoker = createInvoker(
    CompilationLibrariesController_5.get.getTypeOfBoardAllBoards(fakeValue[String]),
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

  // @LINE:228
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup142_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup142_invoker = createInvoker(
    CompilationLibrariesController_5.get.newLibraryGroup(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newLibraryGroup",
      Nil,
      "POST",
      """LibraryGroups""",
      this.prefix + """compilation/libraryGroup"""
    )
  )

  // @LINE:229
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup143_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup143_invoker = createInvoker(
    CompilationLibrariesController_5.get.getLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroup",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/$id<[^/]+>"""
    )
  )

  // @LINE:230
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibraryGroup144_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibraryGroup144_invoker = createInvoker(
    CompilationLibrariesController_5.get.deleteLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deleteLibraryGroup",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/libraryGroup/$id<[^/]+>"""
    )
  )

  // @LINE:231
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll145_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll145_invoker = createInvoker(
    CompilationLibrariesController_5.get.getLibraryGroupAll(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroupAll",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup"""
    )
  )

  // @LINE:232
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup146_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup146_invoker = createInvoker(
    CompilationLibrariesController_5.get.updateLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateLibraryGroup",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/libraryGroup/$id<[^/]+>"""
    )
  )

  // @LINE:233
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription147_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription147_invoker = createInvoker(
    CompilationLibrariesController_5.get.getLibraryGroupDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroupDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/generalDescription/$id<[^/]+>"""
    )
  )

  // @LINE:234
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors148_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/processors/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors148_invoker = createInvoker(
    CompilationLibrariesController_5.get.getLibraryGroupProcessors(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroupProcessors",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/processors/$id<[^/]+>"""
    )
  )

  // @LINE:235
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries149_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/libraries/"), DynamicPart("libraryId", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries149_invoker = createInvoker(
    CompilationLibrariesController_5.get.getLibraryGroupLibraries(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroupLibraries",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/libraries/$libraryId<[^/]+>/$version<[^/]+>"""
    )
  )

  // @LINE:236
  private[this] lazy val controllers_CompilationLibrariesController_createNewVersionLibraryGroup150_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/versions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_createNewVersionLibraryGroup150_invoker = createInvoker(
    CompilationLibrariesController_5.get.createNewVersionLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "createNewVersionLibraryGroup",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/versions/$id<[^/]+>"""
    )
  )

  // @LINE:237
  private[this] lazy val controllers_CompilationLibrariesController_getVersionLibraryGroup151_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/versions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getVersionLibraryGroup151_invoker = createInvoker(
    CompilationLibrariesController_5.get.getVersionLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getVersionLibraryGroup",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/libraryGroup/versions/$id<[^/]+>"""
    )
  )

  // @LINE:238
  private[this] lazy val controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup152_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/upload/"), DynamicPart("libraryId", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup152_invoker = createInvoker(
    CompilationLibrariesController_5.get.uploudLibraryToLibraryGroup(fakeValue[String], fakeValue[Double]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploudLibraryToLibraryGroup",
      Seq(classOf[String], classOf[Double]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/upload/$libraryId<[^/]+>/$version<[^/]+>"""
    )
  )

  // @LINE:240
  private[this] lazy val controllers_CompilationLibrariesController_listOfFilesInVersion153_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/listOfFiles/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_listOfFilesInVersion153_invoker = createInvoker(
    CompilationLibrariesController_5.get.listOfFilesInVersion(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "listOfFilesInVersion",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/listOfFiles/$id<[^/]+>"""
    )
  )

  // @LINE:241
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord154_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/fileRecord/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord154_invoker = createInvoker(
    CompilationLibrariesController_5.get.fileRecord(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "fileRecord",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/fileRecord/$id<[^/]+>"""
    )
  )

  // @LINE:244
  private[this] lazy val controllers_CompilationLibrariesController_newSingleLibrary155_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newSingleLibrary155_invoker = createInvoker(
    CompilationLibrariesController_5.get.newSingleLibrary(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newSingleLibrary",
      Nil,
      "POST",
      """FileRecord""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:245
  private[this] lazy val controllers_CompilationLibrariesController_newVersionSingleLibrary156_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newVersionSingleLibrary156_invoker = createInvoker(
    CompilationLibrariesController_5.get.newVersionSingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newVersionSingleLibrary",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/library/version/$id<[^/]+>"""
    )
  )

  // @LINE:246
  private[this] lazy val controllers_CompilationLibrariesController_getAllVersionSingleLibrary157_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getAllVersionSingleLibrary157_invoker = createInvoker(
    CompilationLibrariesController_5.get.getAllVersionSingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getAllVersionSingleLibrary",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/version/$id<[^/]+>"""
    )
  )

  // @LINE:247
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryFilter158_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryFilter158_invoker = createInvoker(
    CompilationLibrariesController_5.get.getSingleLibraryFilter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getSingleLibraryFilter",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/library/filter"""
    )
  )

  // @LINE:248
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibrary159_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibrary159_invoker = createInvoker(
    CompilationLibrariesController_5.get.getSingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getSingleLibrary",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/$id<[^/]+>"""
    )
  )

  // @LINE:249
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryAll160_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryAll160_invoker = createInvoker(
    CompilationLibrariesController_5.get.getSingleLibraryAll(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getSingleLibraryAll",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:251
  private[this] lazy val controllers_CompilationLibrariesController_updateSingleLibrary161_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateSingleLibrary161_invoker = createInvoker(
    CompilationLibrariesController_5.get.updateSingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateSingleLibrary",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/library/$id<[^/]+>"""
    )
  )

  // @LINE:252
  private[this] lazy val controllers_CompilationLibrariesController_deleteSingleLibrary162_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteSingleLibrary162_invoker = createInvoker(
    CompilationLibrariesController_5.get.deleteSingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deleteSingleLibrary",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/library/$id<[^/]+>"""
    )
  )

  // @LINE:253
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion163_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/uploud/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion163_invoker = createInvoker(
    CompilationLibrariesController_5.get.uploadSingleLibraryWithVersion(fakeValue[String], fakeValue[Double]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploadSingleLibraryWithVersion",
      Seq(classOf[String], classOf[Double]),
      "POST",
      """""",
      this.prefix + """compilation/library/uploud/$id<[^/]+>/$version<[^/]+>"""
    )
  )

  // @LINE:254
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibrary164_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/uploud/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibrary164_invoker = createInvoker(
    CompilationLibrariesController_5.get.uploadSingleLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "uploadSingleLibrary",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/library/uploud/$id<[^/]+>"""
    )
  )

  // @LINE:255
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryDescription165_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryDescription165_invoker = createInvoker(
    CompilationLibrariesController_5.get.getSingleLibraryDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getSingleLibraryDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/description/$id<[^/]+>"""
    )
  )

  // @LINE:261
  private[this] lazy val utilities_swagger_ApiHelpController_getResources166_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api-docs")))
  )
  private[this] lazy val utilities_swagger_ApiHelpController_getResources166_invoker = createInvoker(
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

  // @LINE:264
  private[this] lazy val controllers_SecurityController_optionLink167_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("all", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_optionLink167_invoker = createInvoker(
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

  // @LINE:267
  private[this] lazy val controllers_Assets_at168_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at168_invoker = createInvoker(
    Assets_7.at(fakeValue[String], fakeValue[String]),
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
  
    // @LINE:9
    case controllers_WikyController_test1_route(params) =>
      call { 
        controllers_WikyController_test1_invoker.call(WikyController_6.get.test)
      }
  
    // @LINE:10
    case controllers_WikyController_test22_route(params) =>
      call(params.fromQuery[String]("fields", None)) { (fields) =>
        controllers_WikyController_test22_invoker.call(WikyController_6.get.test2(fields))
      }
  
    // @LINE:14
    case controllers_WebSocketController_connection3_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_WebSocketController_connection3_invoker.call(WebSocketController_1.get.connection(id))
      }
  
    // @LINE:15
    case controllers_WebSocketController_getWebSocketStats4_route(params) =>
      call { 
        controllers_WebSocketController_getWebSocketStats4_invoker.call(WebSocketController_1.get.getWebSocketStats())
      }
  
    // @LINE:16
    case controllers_WebSocketController_sendTo5_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_WebSocketController_sendTo5_invoker.call(WebSocketController_1.get.sendTo(id))
      }
  
    // @LINE:20
    case controllers_SecurityController_login6_route(params) =>
      call { 
        controllers_SecurityController_login6_invoker.call(SecurityController_3.get.login())
      }
  
    // @LINE:21
    case controllers_SecurityController_logout7_route(params) =>
      call { 
        controllers_SecurityController_logout7_invoker.call(SecurityController_3.get.logout)
      }
  
    // @LINE:23
    case controllers_SecurityController_Facebook8_route(params) =>
      call { 
        controllers_SecurityController_Facebook8_invoker.call(SecurityController_3.get.Facebook())
      }
  
    // @LINE:24
    case controllers_SecurityController_Twitter9_route(params) =>
      call { 
        controllers_SecurityController_Twitter9_invoker.call(SecurityController_3.get.Twitter())
      }
  
    // @LINE:25
    case controllers_SecurityController_GitHub10_route(params) =>
      call { 
        controllers_SecurityController_GitHub10_invoker.call(SecurityController_3.get.GitHub())
      }
  
    // @LINE:26
    case controllers_SecurityController_Vkontakte11_route(params) =>
      call { 
        controllers_SecurityController_Vkontakte11_invoker.call(SecurityController_3.get.Vkontakte())
      }
  
    // @LINE:28
    case controllers_SecurityController_getPersonByToken12_route(params) =>
      call { 
        controllers_SecurityController_getPersonByToken12_invoker.call(SecurityController_3.get.getPersonByToken())
      }
  
    // @LINE:30
    case controllers_SecurityController_GET_facebook_oauth13_route(params) =>
      call(params.fromQuery[String]("code", None), params.fromQuery[String]("state", None)) { (code, state) =>
        controllers_SecurityController_GET_facebook_oauth13_invoker.call(SecurityController_3.get.GET_facebook_oauth(code, state))
      }
  
    // @LINE:31
    case controllers_SecurityController_GET_facebook_oauth14_route(params) =>
      call(params.fromQuery[String]("code", None), params.fromQuery[String]("state", None)) { (code, state) =>
        controllers_SecurityController_GET_facebook_oauth14_invoker.call(SecurityController_3.get.GET_facebook_oauth(code, state))
      }
  
    // @LINE:33
    case controllers_SecurityController_GET_github_oauth15_route(params) =>
      call(params.fromQuery[String]("code", None), params.fromQuery[String]("state", None)) { (code, state) =>
        controllers_SecurityController_GET_github_oauth15_invoker.call(SecurityController_3.get.GET_github_oauth(code, state))
      }
  
    // @LINE:39
    case controllers_PersonCreateController_developerRegistration16_route(params) =>
      call { 
        controllers_PersonCreateController_developerRegistration16_invoker.call(PersonCreateController_9.get.developerRegistration())
      }
  
    // @LINE:40
    case controllers_PersonCreateController_standartRegistration17_route(params) =>
      call { 
        controllers_PersonCreateController_standartRegistration17_invoker.call(PersonCreateController_9.get.standartRegistration())
      }
  
    // @LINE:41
    case controllers_PersonCreateController_updatePersonInformation18_route(params) =>
      call { 
        controllers_PersonCreateController_updatePersonInformation18_invoker.call(PersonCreateController_9.get.updatePersonInformation())
      }
  
    // @LINE:42
    case controllers_PersonCreateController_getPerson19_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_getPerson19_invoker.call(PersonCreateController_9.get.getPerson(id))
      }
  
    // @LINE:44
    case controllers_PersonCreateController_deletePerson20_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_deletePerson20_invoker.call(PersonCreateController_9.get.deletePerson(id))
      }
  
    // @LINE:46
    case controllers_PersonCreateController_emailPersonAuthentitaction21_route(params) =>
      call(params.fromQuery[String]("mail", None), params.fromQuery[String]("authToken", None)) { (mail, authToken) =>
        controllers_PersonCreateController_emailPersonAuthentitaction21_invoker.call(PersonCreateController_9.get.emailPersonAuthentitaction(mail, authToken))
      }
  
    // @LINE:53
    case controllers_PermissionController_getAllPermissions22_route(params) =>
      call { 
        controllers_PermissionController_getAllPermissions22_invoker.call(PermissionController_0.get.getAllPermissions())
      }
  
    // @LINE:54
    case controllers_PermissionController_getAllGroups23_route(params) =>
      call { 
        controllers_PermissionController_getAllGroups23_invoker.call(PermissionController_0.get.getAllGroups())
      }
  
    // @LINE:55
    case controllers_PermissionController_createGroup24_route(params) =>
      call { 
        controllers_PermissionController_createGroup24_invoker.call(PermissionController_0.get.createGroup())
      }
  
    // @LINE:57
    case controllers_PermissionController_getAllPersonPermission25_route(params) =>
      call { 
        controllers_PermissionController_getAllPersonPermission25_invoker.call(PermissionController_0.get.getAllPersonPermission())
      }
  
    // @LINE:58
    case controllers_PermissionController_removeAllPersonPermission26_route(params) =>
      call { 
        controllers_PermissionController_removeAllPersonPermission26_invoker.call(PermissionController_0.get.removeAllPersonPermission())
      }
  
    // @LINE:59
    case controllers_PermissionController_addAllPersonPermission27_route(params) =>
      call { 
        controllers_PermissionController_addAllPersonPermission27_invoker.call(PermissionController_0.get.addAllPersonPermission())
      }
  
    // @LINE:64
    case controllers_OverFlowController_newPost28_route(params) =>
      call { 
        controllers_OverFlowController_newPost28_invoker.call(OverFlowController_8.get.newPost())
      }
  
    // @LINE:65
    case controllers_OverFlowController_getPost29_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPost29_invoker.call(OverFlowController_8.get.getPost(id))
      }
  
    // @LINE:66
    case controllers_OverFlowController_deletePost30_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost30_invoker.call(OverFlowController_8.get.deletePost(id))
      }
  
    // @LINE:67
    case controllers_OverFlowController_editPost31_route(params) =>
      call { 
        controllers_OverFlowController_editPost31_invoker.call(OverFlowController_8.get.editPost())
      }
  
    // @LINE:68
    case controllers_OverFlowController_getLatestPost32_route(params) =>
      call { 
        controllers_OverFlowController_getLatestPost32_invoker.call(OverFlowController_8.get.getLatestPost())
      }
  
    // @LINE:69
    case controllers_OverFlowController_getPostByFilter33_route(params) =>
      call { 
        controllers_OverFlowController_getPostByFilter33_invoker.call(OverFlowController_8.get.getPostByFilter())
      }
  
    // @LINE:70
    case controllers_OverFlowController_getPostLinkedAnswers34_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPostLinkedAnswers34_invoker.call(OverFlowController_8.get.getPostLinkedAnswers(id))
      }
  
    // @LINE:72
    case controllers_OverFlowController_hashTagsListOnPost35_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_hashTagsListOnPost35_invoker.call(OverFlowController_8.get.hashTagsListOnPost(id))
      }
  
    // @LINE:73
    case controllers_OverFlowController_commentsListOnPost36_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_commentsListOnPost36_invoker.call(OverFlowController_8.get.commentsListOnPost(id))
      }
  
    // @LINE:74
    case controllers_OverFlowController_answereListOnPost37_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_answereListOnPost37_invoker.call(OverFlowController_8.get.answereListOnPost(id))
      }
  
    // @LINE:75
    case controllers_OverFlowController_textOfPost38_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_textOfPost38_invoker.call(OverFlowController_8.get.textOfPost(id))
      }
  
    // @LINE:77
    case controllers_OverFlowController_newTypeOfPost39_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfPost39_invoker.call(OverFlowController_8.get.newTypeOfPost())
      }
  
    // @LINE:78
    case controllers_OverFlowController_getTypeOfPost40_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfPost40_invoker.call(OverFlowController_8.get.getTypeOfPost())
      }
  
    // @LINE:80
    case controllers_OverFlowController_newTypeOfConfirms41_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfConfirms41_invoker.call(OverFlowController_8.get.newTypeOfConfirms())
      }
  
    // @LINE:81
    case controllers_OverFlowController_getTypeOfConfirms42_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfConfirms42_invoker.call(OverFlowController_8.get.getTypeOfConfirms())
      }
  
    // @LINE:82
    case controllers_OverFlowController_putTypeOfConfirmToPost43_route(params) =>
      call(params.fromPath[String]("conf", None), params.fromPath[String]("pst", None)) { (conf, pst) =>
        controllers_OverFlowController_putTypeOfConfirmToPost43_invoker.call(OverFlowController_8.get.putTypeOfConfirmToPost(conf, pst))
      }
  
    // @LINE:84
    case controllers_OverFlowController_addComment44_route(params) =>
      call { 
        controllers_OverFlowController_addComment44_invoker.call(OverFlowController_8.get.addComment())
      }
  
    // @LINE:85
    case controllers_OverFlowController_updateComment45_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment45_invoker.call(OverFlowController_8.get.updateComment(id))
      }
  
    // @LINE:86
    case controllers_OverFlowController_deletePost46_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost46_invoker.call(OverFlowController_8.get.deletePost(id))
      }
  
    // @LINE:88
    case controllers_OverFlowController_addAnswer47_route(params) =>
      call { 
        controllers_OverFlowController_addAnswer47_invoker.call(OverFlowController_8.get.addAnswer())
      }
  
    // @LINE:89
    case controllers_OverFlowController_updateComment48_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment48_invoker.call(OverFlowController_8.get.updateComment(id))
      }
  
    // @LINE:90
    case controllers_OverFlowController_deletePost49_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost49_invoker.call(OverFlowController_8.get.deletePost(id))
      }
  
    // @LINE:92
    case controllers_OverFlowController_likePlus50_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likePlus50_invoker.call(OverFlowController_8.get.likePlus(id))
      }
  
    // @LINE:93
    case controllers_OverFlowController_likeMinus51_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likeMinus51_invoker.call(OverFlowController_8.get.likeMinus(id))
      }
  
    // @LINE:94
    case controllers_OverFlowController_linkWithPreviousAnswer52_route(params) =>
      call { 
        controllers_OverFlowController_linkWithPreviousAnswer52_invoker.call(OverFlowController_8.get.linkWithPreviousAnswer())
      }
  
    // @LINE:95
    case controllers_OverFlowController_unlinkWithPreviousAnswer53_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_unlinkWithPreviousAnswer53_invoker.call(OverFlowController_8.get.unlinkWithPreviousAnswer(id))
      }
  
    // @LINE:96
    case controllers_OverFlowController_removeHashTag54_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag54_invoker.call(OverFlowController_8.get.removeHashTag())
      }
  
    // @LINE:97
    case controllers_OverFlowController_addHashTag55_route(params) =>
      call { 
        controllers_OverFlowController_addHashTag55_invoker.call(OverFlowController_8.get.addHashTag())
      }
  
    // @LINE:98
    case controllers_OverFlowController_removeHashTag56_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag56_invoker.call(OverFlowController_8.get.removeHashTag())
      }
  
    // @LINE:107
    case controllers_ProgramingPackageController_postNewProject57_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProject57_invoker.call(ProgramingPackageController_4.get.postNewProject())
      }
  
    // @LINE:108
    case controllers_ProgramingPackageController_updateProject58_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateProject58_invoker.call(ProgramingPackageController_4.get.updateProject(id))
      }
  
    // @LINE:109
    case controllers_ProgramingPackageController_getProject59_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProject59_invoker.call(ProgramingPackageController_4.get.getProject(id))
      }
  
    // @LINE:110
    case controllers_ProgramingPackageController_getProjectsByUserAccount60_route(params) =>
      call { 
        controllers_ProgramingPackageController_getProjectsByUserAccount60_invoker.call(ProgramingPackageController_4.get.getProjectsByUserAccount())
      }
  
    // @LINE:111
    case controllers_ProgramingPackageController_deleteProject61_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteProject61_invoker.call(ProgramingPackageController_4.get.deleteProject(id))
      }
  
    // @LINE:112
    case controllers_ProgramingPackageController_shareProjectWithUsers62_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_shareProjectWithUsers62_invoker.call(ProgramingPackageController_4.get.shareProjectWithUsers(id))
      }
  
    // @LINE:113
    case controllers_ProgramingPackageController_unshareProjectWithUsers63_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_unshareProjectWithUsers63_invoker.call(ProgramingPackageController_4.get.unshareProjectWithUsers(id))
      }
  
    // @LINE:114
    case controllers_ProgramingPackageController_getProgramPrograms64_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramPrograms64_invoker.call(ProgramingPackageController_4.get.getProgramPrograms(id))
      }
  
    // @LINE:115
    case controllers_ProgramingPackageController_getProgramhomerList65_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramhomerList65_invoker.call(ProgramingPackageController_4.get.getProgramhomerList(id))
      }
  
    // @LINE:116
    case controllers_ProgramingPackageController_getProjectOwners66_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectOwners66_invoker.call(ProgramingPackageController_4.get.getProjectOwners(id))
      }
  
    // @LINE:119
    case controllers_ProgramingPackageController_newHomer67_route(params) =>
      call { 
        controllers_ProgramingPackageController_newHomer67_invoker.call(ProgramingPackageController_4.get.newHomer())
      }
  
    // @LINE:120
    case controllers_ProgramingPackageController_removeHomer68_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeHomer68_invoker.call(ProgramingPackageController_4.get.removeHomer(id))
      }
  
    // @LINE:121
    case controllers_ProgramingPackageController_getHomer69_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getHomer69_invoker.call(ProgramingPackageController_4.get.getHomer(id))
      }
  
    // @LINE:122
    case controllers_ProgramingPackageController_getAllHomers70_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllHomers70_invoker.call(ProgramingPackageController_4.get.getAllHomers())
      }
  
    // @LINE:123
    case controllers_ProgramingPackageController_getConnectedHomers71_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getConnectedHomers71_invoker.call(ProgramingPackageController_4.get.getConnectedHomers(id))
      }
  
    // @LINE:128
    case controllers_ProgramingPackageController_connectHomerWithProject72_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectHomerWithProject72_invoker.call(ProgramingPackageController_4.get.connectHomerWithProject())
      }
  
    // @LINE:129
    case controllers_ProgramingPackageController_unConnectHomerWithProject73_route(params) =>
      call { 
        controllers_ProgramingPackageController_unConnectHomerWithProject73_invoker.call(ProgramingPackageController_4.get.unConnectHomerWithProject())
      }
  
    // @LINE:132
    case controllers_ProgramingPackageController_postNewProgram74_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProgram74_invoker.call(ProgramingPackageController_4.get.postNewProgram())
      }
  
    // @LINE:133
    case controllers_ProgramingPackageController_getProgram75_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgram75_invoker.call(ProgramingPackageController_4.get.getProgram(id))
      }
  
    // @LINE:134
    case controllers_ProgramingPackageController_editProgram76_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editProgram76_invoker.call(ProgramingPackageController_4.get.editProgram(id))
      }
  
    // @LINE:135
    case controllers_ProgramingPackageController_removeProgram77_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeProgram77_invoker.call(ProgramingPackageController_4.get.removeProgram(id))
      }
  
    // @LINE:136
    case controllers_ProgramingPackageController_getProgramInJson78_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson78_invoker.call(ProgramingPackageController_4.get.getProgramInJson(id))
      }
  
    // @LINE:139
    case controllers_ProgramingPackageController_getAllPrograms79_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAllPrograms79_invoker.call(ProgramingPackageController_4.get.getAllPrograms(id))
      }
  
    // @LINE:140
    case controllers_ProgramingPackageController_listOfUploadedHomers80_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfUploadedHomers80_invoker.call(ProgramingPackageController_4.get.listOfUploadedHomers(id))
      }
  
    // @LINE:141
    case controllers_ProgramingPackageController_listOfHomersWaitingForUpload81_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfHomersWaitingForUpload81_invoker.call(ProgramingPackageController_4.get.listOfHomersWaitingForUpload(id))
      }
  
    // @LINE:142
    case controllers_ProgramingPackageController_getProgramInJson82_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson82_invoker.call(ProgramingPackageController_4.get.getProgramInJson(id))
      }
  
    // @LINE:143
    case controllers_ProgramingPackageController_getProjectsBoard83_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectsBoard83_invoker.call(ProgramingPackageController_4.get.getProjectsBoard(id))
      }
  
    // @LINE:144
    case controllers_ProgramingPackageController_uploadProgramToHomer_Immediately84_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_Immediately84_invoker.call(ProgramingPackageController_4.get.uploadProgramToHomer_Immediately())
      }
  
    // @LINE:145
    case controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible85_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible85_invoker.call(ProgramingPackageController_4.get.uploadProgramToHomer_AsSoonAsPossible())
      }
  
    // @LINE:146
    case controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible86_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible86_invoker.call(ProgramingPackageController_4.get.uploadProgramToHomer_GivenTimeAsSoonAsPossible())
      }
  
    // @LINE:149
    case controllers_ProgramingPackageController_newBlock87_route(params) =>
      call { 
        controllers_ProgramingPackageController_newBlock87_invoker.call(ProgramingPackageController_4.get.newBlock())
      }
  
    // @LINE:150
    case controllers_ProgramingPackageController_newVersionOfBlock88_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_newVersionOfBlock88_invoker.call(ProgramingPackageController_4.get.newVersionOfBlock(id))
      }
  
    // @LINE:151
    case controllers_ProgramingPackageController_logicJsonVersion89_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[Double]("version", None)) { (id, version) =>
        controllers_ProgramingPackageController_logicJsonVersion89_invoker.call(ProgramingPackageController_4.get.logicJsonVersion(id, version))
      }
  
    // @LINE:152
    case controllers_ProgramingPackageController_designJsonVersion90_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[Double]("version", None)) { (id, version) =>
        controllers_ProgramingPackageController_designJsonVersion90_invoker.call(ProgramingPackageController_4.get.designJsonVersion(id, version))
      }
  
    // @LINE:153
    case controllers_ProgramingPackageController_logicJsonLast91_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_logicJsonLast91_invoker.call(ProgramingPackageController_4.get.logicJsonLast(id))
      }
  
    // @LINE:154
    case controllers_ProgramingPackageController_designJsonLast92_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_designJsonLast92_invoker.call(ProgramingPackageController_4.get.designJsonLast(id))
      }
  
    // @LINE:155
    case controllers_ProgramingPackageController_generalDescription93_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_generalDescription93_invoker.call(ProgramingPackageController_4.get.generalDescription(id))
      }
  
    // @LINE:156
    case controllers_ProgramingPackageController_versionDescription94_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_versionDescription94_invoker.call(ProgramingPackageController_4.get.versionDescription(id))
      }
  
    // @LINE:157
    case controllers_ProgramingPackageController_getBlockVersion95_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[Double]("version", None)) { (id, version) =>
        controllers_ProgramingPackageController_getBlockVersion95_invoker.call(ProgramingPackageController_4.get.getBlockVersion(id, version))
      }
  
    // @LINE:158
    case controllers_ProgramingPackageController_getBlockLast96_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getBlockLast96_invoker.call(ProgramingPackageController_4.get.getBlockLast(id))
      }
  
    // @LINE:160
    case controllers_ProgramingPackageController_allPrevVersions97_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_allPrevVersions97_invoker.call(ProgramingPackageController_4.get.allPrevVersions(id))
      }
  
    // @LINE:161
    case controllers_ProgramingPackageController_deleteBlock98_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_deleteBlock98_invoker.call(ProgramingPackageController_4.get.deleteBlock(url))
      }
  
    // @LINE:162
    case controllers_ProgramingPackageController_getByFilter99_route(params) =>
      call { 
        controllers_ProgramingPackageController_getByFilter99_invoker.call(ProgramingPackageController_4.get.getByFilter())
      }
  
    // @LINE:171
    case controllers_CompilationLibrariesController_newCProgram100_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newCProgram100_invoker.call(CompilationLibrariesController_5.get.newCProgram())
      }
  
    // @LINE:172
    case controllers_CompilationLibrariesController_getCProgram101_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getCProgram101_invoker.call(CompilationLibrariesController_5.get.getCProgram(id))
      }
  
    // @LINE:173
    case controllers_CompilationLibrariesController_gellAllProgramFromProject102_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_gellAllProgramFromProject102_invoker.call(CompilationLibrariesController_5.get.gellAllProgramFromProject(id))
      }
  
    // @LINE:175
    case controllers_CompilationLibrariesController_updateCProgramDescription103_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateCProgramDescription103_invoker.call(CompilationLibrariesController_5.get.updateCProgramDescription(id))
      }
  
    // @LINE:176
    case controllers_CompilationLibrariesController_newVersionOfCProgram104_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_newVersionOfCProgram104_invoker.call(CompilationLibrariesController_5.get.newVersionOfCProgram(id))
      }
  
    // @LINE:178
    case controllers_CompilationLibrariesController_deleteCProgram105_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteCProgram105_invoker.call(CompilationLibrariesController_5.get.deleteCProgram(id))
      }
  
    // @LINE:179
    case controllers_CompilationLibrariesController_deleteVersionOfCProgram106_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("version", None)) { (id, version) =>
        controllers_CompilationLibrariesController_deleteVersionOfCProgram106_invoker.call(CompilationLibrariesController_5.get.deleteVersionOfCProgram(id, version))
      }
  
    // @LINE:181
    case controllers_CompilationLibrariesController_generateProjectForEclipse107_route(params) =>
      call { 
        controllers_CompilationLibrariesController_generateProjectForEclipse107_invoker.call(CompilationLibrariesController_5.get.generateProjectForEclipse())
      }
  
    // @LINE:182
    case controllers_CompilationLibrariesController_uploudCompilationToBoard108_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("board", None)) { (id, board) =>
        controllers_CompilationLibrariesController_uploudCompilationToBoard108_invoker.call(CompilationLibrariesController_5.get.uploudCompilationToBoard(id, board))
      }
  
    // @LINE:183
    case controllers_CompilationLibrariesController_uploudBinaryFileToBoard109_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_uploudBinaryFileToBoard109_invoker.call(CompilationLibrariesController_5.get.uploudBinaryFileToBoard(id))
      }
  
    // @LINE:186
    case controllers_CompilationLibrariesController_newProcessor110_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProcessor110_invoker.call(CompilationLibrariesController_5.get.newProcessor())
      }
  
    // @LINE:187
    case controllers_CompilationLibrariesController_getProcessor111_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessor111_invoker.call(CompilationLibrariesController_5.get.getProcessor(id))
      }
  
    // @LINE:188
    case controllers_CompilationLibrariesController_getProcessorAll112_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProcessorAll112_invoker.call(CompilationLibrariesController_5.get.getProcessorAll())
      }
  
    // @LINE:189
    case controllers_CompilationLibrariesController_updateProcessor113_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProcessor113_invoker.call(CompilationLibrariesController_5.get.updateProcessor(id))
      }
  
    // @LINE:190
    case controllers_CompilationLibrariesController_deleteProcessor114_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteProcessor114_invoker.call(CompilationLibrariesController_5.get.deleteProcessor(id))
      }
  
    // @LINE:192
    case controllers_CompilationLibrariesController_connectProcessorWithLibrary115_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrId", None)) { (id, lbrId) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibrary115_invoker.call(CompilationLibrariesController_5.get.connectProcessorWithLibrary(id, lbrId))
      }
  
    // @LINE:193
    case controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup116_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrgId", None)) { (id, lbrgId) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup116_invoker.call(CompilationLibrariesController_5.get.connectProcessorWithLibraryGroup(id, lbrgId))
      }
  
    // @LINE:194
    case controllers_CompilationLibrariesController_unconnectProcessorWithLibrary117_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrId", None)) { (id, lbrId) =>
        controllers_CompilationLibrariesController_unconnectProcessorWithLibrary117_invoker.call(CompilationLibrariesController_5.get.unconnectProcessorWithLibrary(id, lbrId))
      }
  
    // @LINE:195
    case controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup118_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrgId", None)) { (id, lbrgId) =>
        controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup118_invoker.call(CompilationLibrariesController_5.get.unconnectProcessorWithLibraryGroup(id, lbrgId))
      }
  
    // @LINE:197
    case controllers_CompilationLibrariesController_getProcessorDescription119_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorDescription119_invoker.call(CompilationLibrariesController_5.get.getProcessorDescription(id))
      }
  
    // @LINE:198
    case controllers_CompilationLibrariesController_getProcessorLibraryGroups120_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorLibraryGroups120_invoker.call(CompilationLibrariesController_5.get.getProcessorLibraryGroups(id))
      }
  
    // @LINE:199
    case controllers_CompilationLibrariesController_getProcessorSingleLibraries121_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorSingleLibraries121_invoker.call(CompilationLibrariesController_5.get.getProcessorSingleLibraries(id))
      }
  
    // @LINE:202
    case controllers_CompilationLibrariesController_newBoard122_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newBoard122_invoker.call(CompilationLibrariesController_5.get.newBoard())
      }
  
    // @LINE:203
    case controllers_CompilationLibrariesController_addUserDescription123_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_addUserDescription123_invoker.call(CompilationLibrariesController_5.get.addUserDescription(id))
      }
  
    // @LINE:204
    case controllers_CompilationLibrariesController_getBoard124_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoard124_invoker.call(CompilationLibrariesController_5.get.getBoard(id))
      }
  
    // @LINE:205
    case controllers_CompilationLibrariesController_deactivateBoard125_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deactivateBoard125_invoker.call(CompilationLibrariesController_5.get.deactivateBoard(id))
      }
  
    // @LINE:206
    case controllers_CompilationLibrariesController_getUserDescription126_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getUserDescription126_invoker.call(CompilationLibrariesController_5.get.getUserDescription(id))
      }
  
    // @LINE:207
    case controllers_CompilationLibrariesController_connectBoardWthProject127_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("pr", None)) { (id, pr) =>
        controllers_CompilationLibrariesController_connectBoardWthProject127_invoker.call(CompilationLibrariesController_5.get.connectBoardWthProject(id, pr))
      }
  
    // @LINE:208
    case controllers_CompilationLibrariesController_unconnectBoardWthProject128_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("pr", None)) { (id, pr) =>
        controllers_CompilationLibrariesController_unconnectBoardWthProject128_invoker.call(CompilationLibrariesController_5.get.unconnectBoardWthProject(id, pr))
      }
  
    // @LINE:209
    case controllers_CompilationLibrariesController_getBoardProjects129_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoardProjects129_invoker.call(CompilationLibrariesController_5.get.getBoardProjects(id))
      }
  
    // @LINE:212
    case controllers_CompilationLibrariesController_newProducers130_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProducers130_invoker.call(CompilationLibrariesController_5.get.newProducers())
      }
  
    // @LINE:213
    case controllers_CompilationLibrariesController_updateProducers131_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProducers131_invoker.call(CompilationLibrariesController_5.get.updateProducers(id))
      }
  
    // @LINE:214
    case controllers_CompilationLibrariesController_getProducers132_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProducers132_invoker.call(CompilationLibrariesController_5.get.getProducers())
      }
  
    // @LINE:215
    case controllers_CompilationLibrariesController_getProducer133_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducer133_invoker.call(CompilationLibrariesController_5.get.getProducer(id))
      }
  
    // @LINE:216
    case controllers_CompilationLibrariesController_getProducerDescription134_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerDescription134_invoker.call(CompilationLibrariesController_5.get.getProducerDescription(id))
      }
  
    // @LINE:217
    case controllers_CompilationLibrariesController_getProducerTypeOfBoards135_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerTypeOfBoards135_invoker.call(CompilationLibrariesController_5.get.getProducerTypeOfBoards(id))
      }
  
    // @LINE:220
    case controllers_CompilationLibrariesController_newTypeOfBoard136_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newTypeOfBoard136_invoker.call(CompilationLibrariesController_5.get.newTypeOfBoard())
      }
  
    // @LINE:221
    case controllers_CompilationLibrariesController_updateTypeOfBoard137_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateTypeOfBoard137_invoker.call(CompilationLibrariesController_5.get.updateTypeOfBoard(id))
      }
  
    // @LINE:222
    case controllers_CompilationLibrariesController_getTypeOfBoards138_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getTypeOfBoards138_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoards())
      }
  
    // @LINE:223
    case controllers_CompilationLibrariesController_getTypeOfBoard139_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoard139_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoard(id))
      }
  
    // @LINE:224
    case controllers_CompilationLibrariesController_getTypeOfBoardDescription140_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardDescription140_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoardDescription(id))
      }
  
    // @LINE:225
    case controllers_CompilationLibrariesController_getTypeOfBoardAllBoards141_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardAllBoards141_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoardAllBoards(id))
      }
  
    // @LINE:228
    case controllers_CompilationLibrariesController_newLibraryGroup142_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newLibraryGroup142_invoker.call(CompilationLibrariesController_5.get.newLibraryGroup())
      }
  
    // @LINE:229
    case controllers_CompilationLibrariesController_getLibraryGroup143_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroup143_invoker.call(CompilationLibrariesController_5.get.getLibraryGroup(id))
      }
  
    // @LINE:230
    case controllers_CompilationLibrariesController_deleteLibraryGroup144_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteLibraryGroup144_invoker.call(CompilationLibrariesController_5.get.deleteLibraryGroup(id))
      }
  
    // @LINE:231
    case controllers_CompilationLibrariesController_getLibraryGroupAll145_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getLibraryGroupAll145_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupAll())
      }
  
    // @LINE:232
    case controllers_CompilationLibrariesController_updateLibraryGroup146_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateLibraryGroup146_invoker.call(CompilationLibrariesController_5.get.updateLibraryGroup(id))
      }
  
    // @LINE:233
    case controllers_CompilationLibrariesController_getLibraryGroupDescription147_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupDescription147_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupDescription(id))
      }
  
    // @LINE:234
    case controllers_CompilationLibrariesController_getLibraryGroupProcessors148_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupProcessors148_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupProcessors(id))
      }
  
    // @LINE:235
    case controllers_CompilationLibrariesController_getLibraryGroupLibraries149_route(params) =>
      call(params.fromPath[String]("libraryId", None), params.fromPath[String]("version", None)) { (libraryId, version) =>
        controllers_CompilationLibrariesController_getLibraryGroupLibraries149_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupLibraries(libraryId, version))
      }
  
    // @LINE:236
    case controllers_CompilationLibrariesController_createNewVersionLibraryGroup150_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_createNewVersionLibraryGroup150_invoker.call(CompilationLibrariesController_5.get.createNewVersionLibraryGroup(id))
      }
  
    // @LINE:237
    case controllers_CompilationLibrariesController_getVersionLibraryGroup151_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getVersionLibraryGroup151_invoker.call(CompilationLibrariesController_5.get.getVersionLibraryGroup(id))
      }
  
    // @LINE:238
    case controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup152_route(params) =>
      call(params.fromPath[String]("libraryId", None), params.fromPath[Double]("version", None)) { (libraryId, version) =>
        controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup152_invoker.call(CompilationLibrariesController_5.get.uploudLibraryToLibraryGroup(libraryId, version))
      }
  
    // @LINE:240
    case controllers_CompilationLibrariesController_listOfFilesInVersion153_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_listOfFilesInVersion153_invoker.call(CompilationLibrariesController_5.get.listOfFilesInVersion(id))
      }
  
    // @LINE:241
    case controllers_CompilationLibrariesController_fileRecord154_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_fileRecord154_invoker.call(CompilationLibrariesController_5.get.fileRecord(id))
      }
  
    // @LINE:244
    case controllers_CompilationLibrariesController_newSingleLibrary155_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newSingleLibrary155_invoker.call(CompilationLibrariesController_5.get.newSingleLibrary())
      }
  
    // @LINE:245
    case controllers_CompilationLibrariesController_newVersionSingleLibrary156_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_newVersionSingleLibrary156_invoker.call(CompilationLibrariesController_5.get.newVersionSingleLibrary(id))
      }
  
    // @LINE:246
    case controllers_CompilationLibrariesController_getAllVersionSingleLibrary157_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getAllVersionSingleLibrary157_invoker.call(CompilationLibrariesController_5.get.getAllVersionSingleLibrary(id))
      }
  
    // @LINE:247
    case controllers_CompilationLibrariesController_getSingleLibraryFilter158_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getSingleLibraryFilter158_invoker.call(CompilationLibrariesController_5.get.getSingleLibraryFilter())
      }
  
    // @LINE:248
    case controllers_CompilationLibrariesController_getSingleLibrary159_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getSingleLibrary159_invoker.call(CompilationLibrariesController_5.get.getSingleLibrary(id))
      }
  
    // @LINE:249
    case controllers_CompilationLibrariesController_getSingleLibraryAll160_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getSingleLibraryAll160_invoker.call(CompilationLibrariesController_5.get.getSingleLibraryAll())
      }
  
    // @LINE:251
    case controllers_CompilationLibrariesController_updateSingleLibrary161_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateSingleLibrary161_invoker.call(CompilationLibrariesController_5.get.updateSingleLibrary(id))
      }
  
    // @LINE:252
    case controllers_CompilationLibrariesController_deleteSingleLibrary162_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteSingleLibrary162_invoker.call(CompilationLibrariesController_5.get.deleteSingleLibrary(id))
      }
  
    // @LINE:253
    case controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion163_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[Double]("version", None)) { (id, version) =>
        controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion163_invoker.call(CompilationLibrariesController_5.get.uploadSingleLibraryWithVersion(id, version))
      }
  
    // @LINE:254
    case controllers_CompilationLibrariesController_uploadSingleLibrary164_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_uploadSingleLibrary164_invoker.call(CompilationLibrariesController_5.get.uploadSingleLibrary(id))
      }
  
    // @LINE:255
    case controllers_CompilationLibrariesController_getSingleLibraryDescription165_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getSingleLibraryDescription165_invoker.call(CompilationLibrariesController_5.get.getSingleLibraryDescription(id))
      }
  
    // @LINE:261
    case utilities_swagger_ApiHelpController_getResources166_route(params) =>
      call { 
        utilities_swagger_ApiHelpController_getResources166_invoker.call(ApiHelpController_2.get.getResources)
      }
  
    // @LINE:264
    case controllers_SecurityController_optionLink167_route(params) =>
      call(params.fromPath[String]("all", None)) { (all) =>
        controllers_SecurityController_optionLink167_invoker.call(SecurityController_3.get.optionLink(all))
      }
  
    // @LINE:267
    case controllers_Assets_at168_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at168_invoker.call(Assets_7.at(path, file))
      }
  }
}
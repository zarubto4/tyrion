
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Fri Jan 22 11:11:57 CET 2016

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:6
  Application_6: javax.inject.Provider[controllers.Application],
  // @LINE:9
  OutsideCommunicationPackageController_2: javax.inject.Provider[webSocket.controllers.OutsideCommunicationPackageController],
  // @LINE:15
  SecurityController_3: javax.inject.Provider[controllers.SecurityController],
  // @LINE:24
  PersonCreateController_8: javax.inject.Provider[controllers.PersonCreateController],
  // @LINE:33
  PermissionController_0: javax.inject.Provider[controllers.PermissionController],
  // @LINE:44
  OverFlowController_7: javax.inject.Provider[controllers.OverFlowController],
  // @LINE:85
  ProgramingPackageController_4: javax.inject.Provider[controllers.ProgramingPackageController],
  // @LINE:145
  CompilationLibrariesController_5: javax.inject.Provider[controllers.CompilationLibrariesController],
  // @LINE:223
  ApiHelpController_1: javax.inject.Provider[utilities.swagger.ApiHelpController],
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    Application_6: javax.inject.Provider[controllers.Application],
    // @LINE:9
    OutsideCommunicationPackageController_2: javax.inject.Provider[webSocket.controllers.OutsideCommunicationPackageController],
    // @LINE:15
    SecurityController_3: javax.inject.Provider[controllers.SecurityController],
    // @LINE:24
    PersonCreateController_8: javax.inject.Provider[controllers.PersonCreateController],
    // @LINE:33
    PermissionController_0: javax.inject.Provider[controllers.PermissionController],
    // @LINE:44
    OverFlowController_7: javax.inject.Provider[controllers.OverFlowController],
    // @LINE:85
    ProgramingPackageController_4: javax.inject.Provider[controllers.ProgramingPackageController],
    // @LINE:145
    CompilationLibrariesController_5: javax.inject.Provider[controllers.CompilationLibrariesController],
    // @LINE:223
    ApiHelpController_1: javax.inject.Provider[utilities.swagger.ApiHelpController]
  ) = this(errorHandler, Application_6, OutsideCommunicationPackageController_2, SecurityController_3, PersonCreateController_8, PermissionController_0, OverFlowController_7, ProgramingPackageController_4, CompilationLibrariesController_5, ApiHelpController_1, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, Application_6, OutsideCommunicationPackageController_2, SecurityController_3, PersonCreateController_8, PermissionController_0, OverFlowController_7, ProgramingPackageController_4, CompilationLibrariesController_5, ApiHelpController_1, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """@controllers.Application@.index"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """websocket/$id<[^/]+>""", """@webSocket.controllers.OutsideCommunicationPackageController@.connection(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/permission/login""", """@controllers.SecurityController@.login()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/permission/logout""", """@controllers.SecurityController@.logout"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonCreateController@.createNewPerson()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person""", """@controllers.PersonCreateController@.updatePersonInformation()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonCreateController@.getPerson(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """coreClient/person/person/$id<[^/]+>""", """@controllers.PersonCreateController@.deletePerson(id:String)"""),
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
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/confirm/$id<[^/]+>""", """@controllers.OverFlowController@.addConfirmType(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """overflow/confirm/$id<[^/]+>""", """@controllers.OverFlowController@.removeConfirmType(id:String)"""),
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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/getAllConnectedHomers/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getConnectedHomers(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer""", """@controllers.ProgramingPackageController@.getAllHomers()"""),
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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/logicJson/$url<.+>""", """@controllers.ProgramingPackageController@.logicJson(url:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/designJson/$url<.+>""", """@controllers.ProgramingPackageController@.designJson(url:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/generalDescription/$id<[^/]+>""", """@controllers.ProgramingPackageController@.generalDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/versionDescription/$id<[^/]+>""", """@controllers.ProgramingPackageController@.versionDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$url<.+>""", """@controllers.ProgramingPackageController@.getBlock(url:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/allPrevVersions/$id<[^/]+>""", """@controllers.ProgramingPackageController@.allPrevVersions(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/$url<.+>""", """@controllers.ProgramingPackageController@.deleteBlock(url:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/blockoBlock/filter""", """@controllers.ProgramingPackageController@.getByFilter()"""),
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
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/version/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.createNewVersionLibraryGroup(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/versions/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getVersionLibraryGroup(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/upload/$libraryId<[^/]+>/$version<[^/]+>""", """@controllers.CompilationLibrariesController@.uploudLibraryToLibraryGroup(libraryId:String, version:Double)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/listOfFiles/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.listOfFilesInVersion(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/fileRecord/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.fileRecord(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.newSingleLibrary()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/version/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.newVersionSingleLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/filter""", """@controllers.CompilationLibrariesController@.getSingleLibraryFilter()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getSingleLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.getSingleLibraryAll()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateSingleLibrary(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteSingleLibrary(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/uploud/$id<[^/]+>/$version<[^/]+>""", """@controllers.CompilationLibrariesController@.uploadSingleLibraryWithVersion(id:String, version:Double)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getSingleLibraryDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/project/eclipse""", """@controllers.CompilationLibrariesController@.generateProjectForEclipse()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api-docs""", """@utilities.swagger.ApiHelpController@.getResources"""),
    ("""OPTIONS""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """$all<.+>""", """@controllers.SecurityController@.optionLink(all:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:6
  private[this] lazy val controllers_Application_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_Application_index0_invoker = createInvoker(
    Application_6.get.index,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "index",
      Nil,
      "GET",
      """ Home page - for testing connected device""",
      this.prefix + """"""
    )
  )

  // @LINE:9
  private[this] lazy val webSocket_controllers_OutsideCommunicationPackageController_connection1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val webSocket_controllers_OutsideCommunicationPackageController_connection1_invoker = createInvoker(
    OutsideCommunicationPackageController_2.get.connection(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "webSocket.controllers.OutsideCommunicationPackageController",
      "connection",
      Seq(classOf[String]),
      "GET",
      """  WEB SOCET  //////////////////////////////////////////////////////////////////////////////""",
      this.prefix + """websocket/$id<[^/]+>"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_SecurityController_login2_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/login")))
  )
  private[this] lazy val controllers_SecurityController_login2_invoker = createInvoker(
    SecurityController_3.get.login(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "login",
      Nil,
      "POST",
      """Login page
 TODO -> Přihlášení pomocí všech verzí a parametrů""",
      this.prefix + """coreClient/person/permission/login"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_SecurityController_logout3_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/logout")))
  )
  private[this] lazy val controllers_SecurityController_logout3_invoker = createInvoker(
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

  // @LINE:24
  private[this] lazy val controllers_PersonCreateController_createNewPerson4_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_createNewPerson4_invoker = createInvoker(
    PersonCreateController_8.get.createNewPerson(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PersonCreateController",
      "createNewPerson",
      Nil,
      "POST",
      """Peron CRUD""",
      this.prefix + """coreClient/person/person"""
    )
  )

  // @LINE:25
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation5_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation5_invoker = createInvoker(
    PersonCreateController_8.get.updatePersonInformation(),
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

  // @LINE:26
  private[this] lazy val controllers_PersonCreateController_getPerson6_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_getPerson6_invoker = createInvoker(
    PersonCreateController_8.get.getPerson(fakeValue[String]),
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

  // @LINE:27
  private[this] lazy val controllers_PersonCreateController_deletePerson7_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_deletePerson7_invoker = createInvoker(
    PersonCreateController_8.get.deletePerson(fakeValue[String]),
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

  // @LINE:33
  private[this] lazy val controllers_PermissionController_getAllPermissions8_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionKeys")))
  )
  private[this] lazy val controllers_PermissionController_getAllPermissions8_invoker = createInvoker(
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

  // @LINE:34
  private[this] lazy val controllers_PermissionController_getAllGroups9_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionGroups")))
  )
  private[this] lazy val controllers_PermissionController_getAllGroups9_invoker = createInvoker(
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

  // @LINE:35
  private[this] lazy val controllers_PermissionController_createGroup10_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/group")))
  )
  private[this] lazy val controllers_PermissionController_createGroup10_invoker = createInvoker(
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

  // @LINE:37
  private[this] lazy val controllers_PermissionController_getAllPersonPermission11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_getAllPersonPermission11_invoker = createInvoker(
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

  // @LINE:38
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission12_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission12_invoker = createInvoker(
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

  // @LINE:39
  private[this] lazy val controllers_PermissionController_addAllPersonPermission13_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_addAllPersonPermission13_invoker = createInvoker(
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

  // @LINE:44
  private[this] lazy val controllers_OverFlowController_newPost14_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_newPost14_invoker = createInvoker(
    OverFlowController_7.get.newPost(),
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

  // @LINE:45
  private[this] lazy val controllers_OverFlowController_getPost15_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPost15_invoker = createInvoker(
    OverFlowController_7.get.getPost(fakeValue[String]),
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

  // @LINE:46
  private[this] lazy val controllers_OverFlowController_deletePost16_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost16_invoker = createInvoker(
    OverFlowController_7.get.deletePost(fakeValue[String]),
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

  // @LINE:47
  private[this] lazy val controllers_OverFlowController_editPost17_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_editPost17_invoker = createInvoker(
    OverFlowController_7.get.editPost(),
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

  // @LINE:48
  private[this] lazy val controllers_OverFlowController_getLatestPost18_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postAll")))
  )
  private[this] lazy val controllers_OverFlowController_getLatestPost18_invoker = createInvoker(
    OverFlowController_7.get.getLatestPost(),
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

  // @LINE:49
  private[this] lazy val controllers_OverFlowController_getPostByFilter19_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postFilter")))
  )
  private[this] lazy val controllers_OverFlowController_getPostByFilter19_invoker = createInvoker(
    OverFlowController_7.get.getPostByFilter(),
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

  // @LINE:50
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers20_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/linkedAnswers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers20_invoker = createInvoker(
    OverFlowController_7.get.getPostLinkedAnswers(fakeValue[String]),
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

  // @LINE:52
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost21_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/hashTags/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost21_invoker = createInvoker(
    OverFlowController_7.get.hashTagsListOnPost(fakeValue[String]),
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

  // @LINE:53
  private[this] lazy val controllers_OverFlowController_commentsListOnPost22_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/comments/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_commentsListOnPost22_invoker = createInvoker(
    OverFlowController_7.get.commentsListOnPost(fakeValue[String]),
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

  // @LINE:54
  private[this] lazy val controllers_OverFlowController_answereListOnPost23_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/answers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_answereListOnPost23_invoker = createInvoker(
    OverFlowController_7.get.answereListOnPost(fakeValue[String]),
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

  // @LINE:55
  private[this] lazy val controllers_OverFlowController_textOfPost24_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/textOfPost/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_textOfPost24_invoker = createInvoker(
    OverFlowController_7.get.textOfPost(fakeValue[String]),
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

  // @LINE:57
  private[this] lazy val controllers_OverFlowController_newTypeOfPost25_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfPost25_invoker = createInvoker(
    OverFlowController_7.get.newTypeOfPost(),
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

  // @LINE:58
  private[this] lazy val controllers_OverFlowController_getTypeOfPost26_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfPost26_invoker = createInvoker(
    OverFlowController_7.get.getTypeOfPost(),
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

  // @LINE:61
  private[this] lazy val controllers_OverFlowController_addComment27_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment")))
  )
  private[this] lazy val controllers_OverFlowController_addComment27_invoker = createInvoker(
    OverFlowController_7.get.addComment(),
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

  // @LINE:62
  private[this] lazy val controllers_OverFlowController_updateComment28_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment28_invoker = createInvoker(
    OverFlowController_7.get.updateComment(fakeValue[String]),
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

  // @LINE:63
  private[this] lazy val controllers_OverFlowController_deletePost29_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost29_invoker = createInvoker(
    OverFlowController_7.get.deletePost(fakeValue[String]),
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

  // @LINE:65
  private[this] lazy val controllers_OverFlowController_addAnswer30_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer")))
  )
  private[this] lazy val controllers_OverFlowController_addAnswer30_invoker = createInvoker(
    OverFlowController_7.get.addAnswer(),
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

  // @LINE:66
  private[this] lazy val controllers_OverFlowController_updateComment31_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment31_invoker = createInvoker(
    OverFlowController_7.get.updateComment(fakeValue[String]),
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

  // @LINE:67
  private[this] lazy val controllers_OverFlowController_deletePost32_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost32_invoker = createInvoker(
    OverFlowController_7.get.deletePost(fakeValue[String]),
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

  // @LINE:69
  private[this] lazy val controllers_OverFlowController_likePlus33_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likePlus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likePlus33_invoker = createInvoker(
    OverFlowController_7.get.likePlus(fakeValue[String]),
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

  // @LINE:70
  private[this] lazy val controllers_OverFlowController_likeMinus34_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likeMinus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likeMinus34_invoker = createInvoker(
    OverFlowController_7.get.likeMinus(fakeValue[String]),
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

  // @LINE:71
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer35_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link")))
  )
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer35_invoker = createInvoker(
    OverFlowController_7.get.linkWithPreviousAnswer(),
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

  // @LINE:72
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer36_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer36_invoker = createInvoker(
    OverFlowController_7.get.unlinkWithPreviousAnswer(fakeValue[String]),
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

  // @LINE:73
  private[this] lazy val controllers_OverFlowController_removeHashTag37_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeLink")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag37_invoker = createInvoker(
    OverFlowController_7.get.removeHashTag(),
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

  // @LINE:74
  private[this] lazy val controllers_OverFlowController_addHashTag38_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/hashTag")))
  )
  private[this] lazy val controllers_OverFlowController_addHashTag38_invoker = createInvoker(
    OverFlowController_7.get.addHashTag(),
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

  // @LINE:75
  private[this] lazy val controllers_OverFlowController_removeHashTag39_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeHashTag")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag39_invoker = createInvoker(
    OverFlowController_7.get.removeHashTag(),
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

  // @LINE:76
  private[this] lazy val controllers_OverFlowController_addConfirmType40_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/confirm/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_addConfirmType40_invoker = createInvoker(
    OverFlowController_7.get.addConfirmType(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "addConfirmType",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """overflow/confirm/$id<[^/]+>"""
    )
  )

  // @LINE:77
  private[this] lazy val controllers_OverFlowController_removeConfirmType41_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/confirm/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_removeConfirmType41_invoker = createInvoker(
    OverFlowController_7.get.removeConfirmType(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.OverFlowController",
      "removeConfirmType",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """overflow/confirm/$id<[^/]+>"""
    )
  )

  // @LINE:85
  private[this] lazy val controllers_ProgramingPackageController_postNewProject42_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProject42_invoker = createInvoker(
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

  // @LINE:86
  private[this] lazy val controllers_ProgramingPackageController_updateProject43_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateProject43_invoker = createInvoker(
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

  // @LINE:87
  private[this] lazy val controllers_ProgramingPackageController_getProject44_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProject44_invoker = createInvoker(
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

  // @LINE:88
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount45_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount45_invoker = createInvoker(
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

  // @LINE:89
  private[this] lazy val controllers_ProgramingPackageController_deleteProject46_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteProject46_invoker = createInvoker(
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

  // @LINE:90
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers47_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/shareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_shareProjectWithUsers47_invoker = createInvoker(
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

  // @LINE:91
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers48_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/unshareProject/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_unshareProjectWithUsers48_invoker = createInvoker(
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

  // @LINE:92
  private[this] lazy val controllers_ProgramingPackageController_getProgramPrograms49_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/programs/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramPrograms49_invoker = createInvoker(
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

  // @LINE:93
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList50_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/homerList/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList50_invoker = createInvoker(
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

  // @LINE:94
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners51_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/owners/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectOwners51_invoker = createInvoker(
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

  // @LINE:97
  private[this] lazy val controllers_ProgramingPackageController_newHomer52_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newHomer52_invoker = createInvoker(
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

  // @LINE:98
  private[this] lazy val controllers_ProgramingPackageController_removeHomer53_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeHomer53_invoker = createInvoker(
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

  // @LINE:99
  private[this] lazy val controllers_ProgramingPackageController_getHomer54_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getHomer54_invoker = createInvoker(
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

  // @LINE:100
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers55_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/getAllConnectedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers55_invoker = createInvoker(
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

  // @LINE:101
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers56_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers56_invoker = createInvoker(
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

  // @LINE:105
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject57_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject57_invoker = createInvoker(
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

  // @LINE:106
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject58_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/unconnectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject58_invoker = createInvoker(
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

  // @LINE:109
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram59_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram59_invoker = createInvoker(
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

  // @LINE:110
  private[this] lazy val controllers_ProgramingPackageController_getProgram60_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgram60_invoker = createInvoker(
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

  // @LINE:111
  private[this] lazy val controllers_ProgramingPackageController_editProgram61_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editProgram61_invoker = createInvoker(
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

  // @LINE:112
  private[this] lazy val controllers_ProgramingPackageController_removeProgram62_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeProgram62_invoker = createInvoker(
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

  // @LINE:113
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson63_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/programInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson63_invoker = createInvoker(
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

  // @LINE:116
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms64_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getallprograms/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms64_invoker = createInvoker(
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

  // @LINE:117
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers65_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfUploadedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers65_invoker = createInvoker(
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

  // @LINE:118
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload66_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfHomersWaitingForUpload/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload66_invoker = createInvoker(
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

  // @LINE:119
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson67_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getProgramInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson67_invoker = createInvoker(
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

  // @LINE:120
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard68_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsBoard68_invoker = createInvoker(
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

  // @LINE:121
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately69_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerImmediately")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately69_invoker = createInvoker(
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

  // @LINE:122
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible70_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerAsSoonAsPossible")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible70_invoker = createInvoker(
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

  // @LINE:123
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible71_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerGivenTime")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible71_invoker = createInvoker(
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

  // @LINE:126
  private[this] lazy val controllers_ProgramingPackageController_newBlock72_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newBlock72_invoker = createInvoker(
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

  // @LINE:127
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock73_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock73_invoker = createInvoker(
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

  // @LINE:128
  private[this] lazy val controllers_ProgramingPackageController_logicJson74_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/logicJson/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_logicJson74_invoker = createInvoker(
    ProgramingPackageController_4.get.logicJson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "logicJson",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/logicJson/$url<.+>"""
    )
  )

  // @LINE:129
  private[this] lazy val controllers_ProgramingPackageController_designJson75_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/designJson/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_designJson75_invoker = createInvoker(
    ProgramingPackageController_4.get.designJson(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "designJson",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/designJson/$url<.+>"""
    )
  )

  // @LINE:130
  private[this] lazy val controllers_ProgramingPackageController_generalDescription76_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_generalDescription76_invoker = createInvoker(
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

  // @LINE:131
  private[this] lazy val controllers_ProgramingPackageController_versionDescription77_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/versionDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_versionDescription77_invoker = createInvoker(
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

  // @LINE:132
  private[this] lazy val controllers_ProgramingPackageController_getBlock78_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlock78_invoker = createInvoker(
    ProgramingPackageController_4.get.getBlock(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getBlock",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/blockoBlock/$url<.+>"""
    )
  )

  // @LINE:134
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions79_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/allPrevVersions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions79_invoker = createInvoker(
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

  // @LINE:135
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock80_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock80_invoker = createInvoker(
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

  // @LINE:136
  private[this] lazy val controllers_ProgramingPackageController_getByFilter81_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/filter")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getByFilter81_invoker = createInvoker(
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

  // @LINE:145
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor82_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor82_invoker = createInvoker(
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

  // @LINE:146
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor83_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor83_invoker = createInvoker(
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

  // @LINE:147
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll84_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll84_invoker = createInvoker(
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

  // @LINE:148
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor85_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor85_invoker = createInvoker(
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

  // @LINE:149
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor86_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor86_invoker = createInvoker(
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

  // @LINE:151
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary87_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbr/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibrary87_invoker = createInvoker(
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

  // @LINE:152
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup88_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbrgrp/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrgId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup88_invoker = createInvoker(
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

  // @LINE:153
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibrary89_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbr/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibrary89_invoker = createInvoker(
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

  // @LINE:154
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup90_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/lbrgrp/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("lbrgId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup90_invoker = createInvoker(
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

  // @LINE:156
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription91_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription91_invoker = createInvoker(
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

  // @LINE:157
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups92_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroups/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups92_invoker = createInvoker(
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

  // @LINE:158
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries93_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/singleLibrary/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorSingleLibraries93_invoker = createInvoker(
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

  // @LINE:162
  private[this] lazy val controllers_CompilationLibrariesController_newBoard94_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newBoard94_invoker = createInvoker(
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

  // @LINE:163
  private[this] lazy val controllers_CompilationLibrariesController_addUserDescription95_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_addUserDescription95_invoker = createInvoker(
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

  // @LINE:164
  private[this] lazy val controllers_CompilationLibrariesController_getBoard96_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoard96_invoker = createInvoker(
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

  // @LINE:165
  private[this] lazy val controllers_CompilationLibrariesController_deactivateBoard97_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/deactivateBoard"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deactivateBoard97_invoker = createInvoker(
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

  // @LINE:166
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription98_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription98_invoker = createInvoker(
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

  // @LINE:167
  private[this] lazy val controllers_CompilationLibrariesController_connectBoardWthProject99_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/connect/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("pr", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_connectBoardWthProject99_invoker = createInvoker(
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

  // @LINE:168
  private[this] lazy val controllers_CompilationLibrariesController_unconnectBoardWthProject100_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/unconnect/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("pr", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_unconnectBoardWthProject100_invoker = createInvoker(
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

  // @LINE:169
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects101_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/projects/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardProjects101_invoker = createInvoker(
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

  // @LINE:173
  private[this] lazy val controllers_CompilationLibrariesController_newProducers102_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProducers102_invoker = createInvoker(
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

  // @LINE:174
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers103_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers103_invoker = createInvoker(
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

  // @LINE:175
  private[this] lazy val controllers_CompilationLibrariesController_getProducers104_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducers104_invoker = createInvoker(
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

  // @LINE:176
  private[this] lazy val controllers_CompilationLibrariesController_getProducer105_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducer105_invoker = createInvoker(
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

  // @LINE:177
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription106_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription106_invoker = createInvoker(
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

  // @LINE:178
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards107_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/typeOfBoards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards107_invoker = createInvoker(
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

  // @LINE:181
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard108_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard108_invoker = createInvoker(
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

  // @LINE:182
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard109_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard109_invoker = createInvoker(
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

  // @LINE:183
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards110_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards110_invoker = createInvoker(
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

  // @LINE:184
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard111_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard111_invoker = createInvoker(
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

  // @LINE:185
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription112_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription112_invoker = createInvoker(
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

  // @LINE:186
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards113_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards113_invoker = createInvoker(
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

  // @LINE:190
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup114_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup114_invoker = createInvoker(
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

  // @LINE:191
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup115_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup115_invoker = createInvoker(
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

  // @LINE:192
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibraryGroup116_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibraryGroup116_invoker = createInvoker(
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

  // @LINE:193
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll117_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll117_invoker = createInvoker(
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

  // @LINE:194
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup118_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup118_invoker = createInvoker(
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

  // @LINE:195
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription119_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription119_invoker = createInvoker(
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

  // @LINE:196
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors120_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/processors/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors120_invoker = createInvoker(
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

  // @LINE:197
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries121_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/libraries/"), DynamicPart("libraryId", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries121_invoker = createInvoker(
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

  // @LINE:198
  private[this] lazy val controllers_CompilationLibrariesController_createNewVersionLibraryGroup122_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_createNewVersionLibraryGroup122_invoker = createInvoker(
    CompilationLibrariesController_5.get.createNewVersionLibraryGroup(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "createNewVersionLibraryGroup",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/version/$id<[^/]+>"""
    )
  )

  // @LINE:199
  private[this] lazy val controllers_CompilationLibrariesController_getVersionLibraryGroup123_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/versions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getVersionLibraryGroup123_invoker = createInvoker(
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

  // @LINE:200
  private[this] lazy val controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup124_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/upload/"), DynamicPart("libraryId", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup124_invoker = createInvoker(
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

  // @LINE:202
  private[this] lazy val controllers_CompilationLibrariesController_listOfFilesInVersion125_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/listOfFiles/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_listOfFilesInVersion125_invoker = createInvoker(
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

  // @LINE:203
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord126_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/fileRecord/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord126_invoker = createInvoker(
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

  // @LINE:206
  private[this] lazy val controllers_CompilationLibrariesController_newSingleLibrary127_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newSingleLibrary127_invoker = createInvoker(
    CompilationLibrariesController_5.get.newSingleLibrary(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newSingleLibrary",
      Nil,
      "POST",
      """LibraryRecord""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:207
  private[this] lazy val controllers_CompilationLibrariesController_newVersionSingleLibrary128_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newVersionSingleLibrary128_invoker = createInvoker(
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

  // @LINE:208
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryFilter129_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryFilter129_invoker = createInvoker(
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

  // @LINE:209
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibrary130_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibrary130_invoker = createInvoker(
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

  // @LINE:210
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryAll131_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryAll131_invoker = createInvoker(
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

  // @LINE:212
  private[this] lazy val controllers_CompilationLibrariesController_updateSingleLibrary132_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateSingleLibrary132_invoker = createInvoker(
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

  // @LINE:213
  private[this] lazy val controllers_CompilationLibrariesController_deleteSingleLibrary133_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteSingleLibrary133_invoker = createInvoker(
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

  // @LINE:214
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion134_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/uploud/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion134_invoker = createInvoker(
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

  // @LINE:215
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryDescription135_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryDescription135_invoker = createInvoker(
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

  // @LINE:217
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse136_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/eclipse")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse136_invoker = createInvoker(
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

  // @LINE:223
  private[this] lazy val utilities_swagger_ApiHelpController_getResources137_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api-docs")))
  )
  private[this] lazy val utilities_swagger_ApiHelpController_getResources137_invoker = createInvoker(
    ApiHelpController_1.get.getResources,
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

  // @LINE:226
  private[this] lazy val controllers_SecurityController_optionLink138_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("all", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_optionLink138_invoker = createInvoker(
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


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:6
    case controllers_Application_index0_route(params) =>
      call { 
        controllers_Application_index0_invoker.call(Application_6.get.index)
      }
  
    // @LINE:9
    case webSocket_controllers_OutsideCommunicationPackageController_connection1_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        webSocket_controllers_OutsideCommunicationPackageController_connection1_invoker.call(OutsideCommunicationPackageController_2.get.connection(id))
      }
  
    // @LINE:15
    case controllers_SecurityController_login2_route(params) =>
      call { 
        controllers_SecurityController_login2_invoker.call(SecurityController_3.get.login())
      }
  
    // @LINE:16
    case controllers_SecurityController_logout3_route(params) =>
      call { 
        controllers_SecurityController_logout3_invoker.call(SecurityController_3.get.logout)
      }
  
    // @LINE:24
    case controllers_PersonCreateController_createNewPerson4_route(params) =>
      call { 
        controllers_PersonCreateController_createNewPerson4_invoker.call(PersonCreateController_8.get.createNewPerson())
      }
  
    // @LINE:25
    case controllers_PersonCreateController_updatePersonInformation5_route(params) =>
      call { 
        controllers_PersonCreateController_updatePersonInformation5_invoker.call(PersonCreateController_8.get.updatePersonInformation())
      }
  
    // @LINE:26
    case controllers_PersonCreateController_getPerson6_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_getPerson6_invoker.call(PersonCreateController_8.get.getPerson(id))
      }
  
    // @LINE:27
    case controllers_PersonCreateController_deletePerson7_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_deletePerson7_invoker.call(PersonCreateController_8.get.deletePerson(id))
      }
  
    // @LINE:33
    case controllers_PermissionController_getAllPermissions8_route(params) =>
      call { 
        controllers_PermissionController_getAllPermissions8_invoker.call(PermissionController_0.get.getAllPermissions())
      }
  
    // @LINE:34
    case controllers_PermissionController_getAllGroups9_route(params) =>
      call { 
        controllers_PermissionController_getAllGroups9_invoker.call(PermissionController_0.get.getAllGroups())
      }
  
    // @LINE:35
    case controllers_PermissionController_createGroup10_route(params) =>
      call { 
        controllers_PermissionController_createGroup10_invoker.call(PermissionController_0.get.createGroup())
      }
  
    // @LINE:37
    case controllers_PermissionController_getAllPersonPermission11_route(params) =>
      call { 
        controllers_PermissionController_getAllPersonPermission11_invoker.call(PermissionController_0.get.getAllPersonPermission())
      }
  
    // @LINE:38
    case controllers_PermissionController_removeAllPersonPermission12_route(params) =>
      call { 
        controllers_PermissionController_removeAllPersonPermission12_invoker.call(PermissionController_0.get.removeAllPersonPermission())
      }
  
    // @LINE:39
    case controllers_PermissionController_addAllPersonPermission13_route(params) =>
      call { 
        controllers_PermissionController_addAllPersonPermission13_invoker.call(PermissionController_0.get.addAllPersonPermission())
      }
  
    // @LINE:44
    case controllers_OverFlowController_newPost14_route(params) =>
      call { 
        controllers_OverFlowController_newPost14_invoker.call(OverFlowController_7.get.newPost())
      }
  
    // @LINE:45
    case controllers_OverFlowController_getPost15_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPost15_invoker.call(OverFlowController_7.get.getPost(id))
      }
  
    // @LINE:46
    case controllers_OverFlowController_deletePost16_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost16_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:47
    case controllers_OverFlowController_editPost17_route(params) =>
      call { 
        controllers_OverFlowController_editPost17_invoker.call(OverFlowController_7.get.editPost())
      }
  
    // @LINE:48
    case controllers_OverFlowController_getLatestPost18_route(params) =>
      call { 
        controllers_OverFlowController_getLatestPost18_invoker.call(OverFlowController_7.get.getLatestPost())
      }
  
    // @LINE:49
    case controllers_OverFlowController_getPostByFilter19_route(params) =>
      call { 
        controllers_OverFlowController_getPostByFilter19_invoker.call(OverFlowController_7.get.getPostByFilter())
      }
  
    // @LINE:50
    case controllers_OverFlowController_getPostLinkedAnswers20_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPostLinkedAnswers20_invoker.call(OverFlowController_7.get.getPostLinkedAnswers(id))
      }
  
    // @LINE:52
    case controllers_OverFlowController_hashTagsListOnPost21_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_hashTagsListOnPost21_invoker.call(OverFlowController_7.get.hashTagsListOnPost(id))
      }
  
    // @LINE:53
    case controllers_OverFlowController_commentsListOnPost22_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_commentsListOnPost22_invoker.call(OverFlowController_7.get.commentsListOnPost(id))
      }
  
    // @LINE:54
    case controllers_OverFlowController_answereListOnPost23_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_answereListOnPost23_invoker.call(OverFlowController_7.get.answereListOnPost(id))
      }
  
    // @LINE:55
    case controllers_OverFlowController_textOfPost24_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_textOfPost24_invoker.call(OverFlowController_7.get.textOfPost(id))
      }
  
    // @LINE:57
    case controllers_OverFlowController_newTypeOfPost25_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfPost25_invoker.call(OverFlowController_7.get.newTypeOfPost())
      }
  
    // @LINE:58
    case controllers_OverFlowController_getTypeOfPost26_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfPost26_invoker.call(OverFlowController_7.get.getTypeOfPost())
      }
  
    // @LINE:61
    case controllers_OverFlowController_addComment27_route(params) =>
      call { 
        controllers_OverFlowController_addComment27_invoker.call(OverFlowController_7.get.addComment())
      }
  
    // @LINE:62
    case controllers_OverFlowController_updateComment28_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment28_invoker.call(OverFlowController_7.get.updateComment(id))
      }
  
    // @LINE:63
    case controllers_OverFlowController_deletePost29_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost29_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:65
    case controllers_OverFlowController_addAnswer30_route(params) =>
      call { 
        controllers_OverFlowController_addAnswer30_invoker.call(OverFlowController_7.get.addAnswer())
      }
  
    // @LINE:66
    case controllers_OverFlowController_updateComment31_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment31_invoker.call(OverFlowController_7.get.updateComment(id))
      }
  
    // @LINE:67
    case controllers_OverFlowController_deletePost32_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost32_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:69
    case controllers_OverFlowController_likePlus33_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likePlus33_invoker.call(OverFlowController_7.get.likePlus(id))
      }
  
    // @LINE:70
    case controllers_OverFlowController_likeMinus34_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likeMinus34_invoker.call(OverFlowController_7.get.likeMinus(id))
      }
  
    // @LINE:71
    case controllers_OverFlowController_linkWithPreviousAnswer35_route(params) =>
      call { 
        controllers_OverFlowController_linkWithPreviousAnswer35_invoker.call(OverFlowController_7.get.linkWithPreviousAnswer())
      }
  
    // @LINE:72
    case controllers_OverFlowController_unlinkWithPreviousAnswer36_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_unlinkWithPreviousAnswer36_invoker.call(OverFlowController_7.get.unlinkWithPreviousAnswer(id))
      }
  
    // @LINE:73
    case controllers_OverFlowController_removeHashTag37_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag37_invoker.call(OverFlowController_7.get.removeHashTag())
      }
  
    // @LINE:74
    case controllers_OverFlowController_addHashTag38_route(params) =>
      call { 
        controllers_OverFlowController_addHashTag38_invoker.call(OverFlowController_7.get.addHashTag())
      }
  
    // @LINE:75
    case controllers_OverFlowController_removeHashTag39_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag39_invoker.call(OverFlowController_7.get.removeHashTag())
      }
  
    // @LINE:76
    case controllers_OverFlowController_addConfirmType40_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_addConfirmType40_invoker.call(OverFlowController_7.get.addConfirmType(id))
      }
  
    // @LINE:77
    case controllers_OverFlowController_removeConfirmType41_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_removeConfirmType41_invoker.call(OverFlowController_7.get.removeConfirmType(id))
      }
  
    // @LINE:85
    case controllers_ProgramingPackageController_postNewProject42_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProject42_invoker.call(ProgramingPackageController_4.get.postNewProject())
      }
  
    // @LINE:86
    case controllers_ProgramingPackageController_updateProject43_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateProject43_invoker.call(ProgramingPackageController_4.get.updateProject(id))
      }
  
    // @LINE:87
    case controllers_ProgramingPackageController_getProject44_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProject44_invoker.call(ProgramingPackageController_4.get.getProject(id))
      }
  
    // @LINE:88
    case controllers_ProgramingPackageController_getProjectsByUserAccount45_route(params) =>
      call { 
        controllers_ProgramingPackageController_getProjectsByUserAccount45_invoker.call(ProgramingPackageController_4.get.getProjectsByUserAccount())
      }
  
    // @LINE:89
    case controllers_ProgramingPackageController_deleteProject46_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteProject46_invoker.call(ProgramingPackageController_4.get.deleteProject(id))
      }
  
    // @LINE:90
    case controllers_ProgramingPackageController_shareProjectWithUsers47_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_shareProjectWithUsers47_invoker.call(ProgramingPackageController_4.get.shareProjectWithUsers(id))
      }
  
    // @LINE:91
    case controllers_ProgramingPackageController_unshareProjectWithUsers48_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_unshareProjectWithUsers48_invoker.call(ProgramingPackageController_4.get.unshareProjectWithUsers(id))
      }
  
    // @LINE:92
    case controllers_ProgramingPackageController_getProgramPrograms49_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramPrograms49_invoker.call(ProgramingPackageController_4.get.getProgramPrograms(id))
      }
  
    // @LINE:93
    case controllers_ProgramingPackageController_getProgramhomerList50_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramhomerList50_invoker.call(ProgramingPackageController_4.get.getProgramhomerList(id))
      }
  
    // @LINE:94
    case controllers_ProgramingPackageController_getProjectOwners51_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectOwners51_invoker.call(ProgramingPackageController_4.get.getProjectOwners(id))
      }
  
    // @LINE:97
    case controllers_ProgramingPackageController_newHomer52_route(params) =>
      call { 
        controllers_ProgramingPackageController_newHomer52_invoker.call(ProgramingPackageController_4.get.newHomer())
      }
  
    // @LINE:98
    case controllers_ProgramingPackageController_removeHomer53_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeHomer53_invoker.call(ProgramingPackageController_4.get.removeHomer(id))
      }
  
    // @LINE:99
    case controllers_ProgramingPackageController_getHomer54_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getHomer54_invoker.call(ProgramingPackageController_4.get.getHomer(id))
      }
  
    // @LINE:100
    case controllers_ProgramingPackageController_getConnectedHomers55_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getConnectedHomers55_invoker.call(ProgramingPackageController_4.get.getConnectedHomers(id))
      }
  
    // @LINE:101
    case controllers_ProgramingPackageController_getAllHomers56_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllHomers56_invoker.call(ProgramingPackageController_4.get.getAllHomers())
      }
  
    // @LINE:105
    case controllers_ProgramingPackageController_connectHomerWithProject57_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectHomerWithProject57_invoker.call(ProgramingPackageController_4.get.connectHomerWithProject())
      }
  
    // @LINE:106
    case controllers_ProgramingPackageController_unConnectHomerWithProject58_route(params) =>
      call { 
        controllers_ProgramingPackageController_unConnectHomerWithProject58_invoker.call(ProgramingPackageController_4.get.unConnectHomerWithProject())
      }
  
    // @LINE:109
    case controllers_ProgramingPackageController_postNewProgram59_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProgram59_invoker.call(ProgramingPackageController_4.get.postNewProgram())
      }
  
    // @LINE:110
    case controllers_ProgramingPackageController_getProgram60_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgram60_invoker.call(ProgramingPackageController_4.get.getProgram(id))
      }
  
    // @LINE:111
    case controllers_ProgramingPackageController_editProgram61_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editProgram61_invoker.call(ProgramingPackageController_4.get.editProgram(id))
      }
  
    // @LINE:112
    case controllers_ProgramingPackageController_removeProgram62_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeProgram62_invoker.call(ProgramingPackageController_4.get.removeProgram(id))
      }
  
    // @LINE:113
    case controllers_ProgramingPackageController_getProgramInJson63_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson63_invoker.call(ProgramingPackageController_4.get.getProgramInJson(id))
      }
  
    // @LINE:116
    case controllers_ProgramingPackageController_getAllPrograms64_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAllPrograms64_invoker.call(ProgramingPackageController_4.get.getAllPrograms(id))
      }
  
    // @LINE:117
    case controllers_ProgramingPackageController_listOfUploadedHomers65_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfUploadedHomers65_invoker.call(ProgramingPackageController_4.get.listOfUploadedHomers(id))
      }
  
    // @LINE:118
    case controllers_ProgramingPackageController_listOfHomersWaitingForUpload66_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfHomersWaitingForUpload66_invoker.call(ProgramingPackageController_4.get.listOfHomersWaitingForUpload(id))
      }
  
    // @LINE:119
    case controllers_ProgramingPackageController_getProgramInJson67_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson67_invoker.call(ProgramingPackageController_4.get.getProgramInJson(id))
      }
  
    // @LINE:120
    case controllers_ProgramingPackageController_getProjectsBoard68_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProjectsBoard68_invoker.call(ProgramingPackageController_4.get.getProjectsBoard(id))
      }
  
    // @LINE:121
    case controllers_ProgramingPackageController_uploadProgramToHomer_Immediately69_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_Immediately69_invoker.call(ProgramingPackageController_4.get.uploadProgramToHomer_Immediately())
      }
  
    // @LINE:122
    case controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible70_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible70_invoker.call(ProgramingPackageController_4.get.uploadProgramToHomer_AsSoonAsPossible())
      }
  
    // @LINE:123
    case controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible71_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible71_invoker.call(ProgramingPackageController_4.get.uploadProgramToHomer_GivenTimeAsSoonAsPossible())
      }
  
    // @LINE:126
    case controllers_ProgramingPackageController_newBlock72_route(params) =>
      call { 
        controllers_ProgramingPackageController_newBlock72_invoker.call(ProgramingPackageController_4.get.newBlock())
      }
  
    // @LINE:127
    case controllers_ProgramingPackageController_newVersionOfBlock73_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_newVersionOfBlock73_invoker.call(ProgramingPackageController_4.get.newVersionOfBlock(id))
      }
  
    // @LINE:128
    case controllers_ProgramingPackageController_logicJson74_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_logicJson74_invoker.call(ProgramingPackageController_4.get.logicJson(url))
      }
  
    // @LINE:129
    case controllers_ProgramingPackageController_designJson75_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_designJson75_invoker.call(ProgramingPackageController_4.get.designJson(url))
      }
  
    // @LINE:130
    case controllers_ProgramingPackageController_generalDescription76_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_generalDescription76_invoker.call(ProgramingPackageController_4.get.generalDescription(id))
      }
  
    // @LINE:131
    case controllers_ProgramingPackageController_versionDescription77_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_versionDescription77_invoker.call(ProgramingPackageController_4.get.versionDescription(id))
      }
  
    // @LINE:132
    case controllers_ProgramingPackageController_getBlock78_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_getBlock78_invoker.call(ProgramingPackageController_4.get.getBlock(url))
      }
  
    // @LINE:134
    case controllers_ProgramingPackageController_allPrevVersions79_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_allPrevVersions79_invoker.call(ProgramingPackageController_4.get.allPrevVersions(id))
      }
  
    // @LINE:135
    case controllers_ProgramingPackageController_deleteBlock80_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_deleteBlock80_invoker.call(ProgramingPackageController_4.get.deleteBlock(url))
      }
  
    // @LINE:136
    case controllers_ProgramingPackageController_getByFilter81_route(params) =>
      call { 
        controllers_ProgramingPackageController_getByFilter81_invoker.call(ProgramingPackageController_4.get.getByFilter())
      }
  
    // @LINE:145
    case controllers_CompilationLibrariesController_newProcessor82_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProcessor82_invoker.call(CompilationLibrariesController_5.get.newProcessor())
      }
  
    // @LINE:146
    case controllers_CompilationLibrariesController_getProcessor83_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessor83_invoker.call(CompilationLibrariesController_5.get.getProcessor(id))
      }
  
    // @LINE:147
    case controllers_CompilationLibrariesController_getProcessorAll84_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProcessorAll84_invoker.call(CompilationLibrariesController_5.get.getProcessorAll())
      }
  
    // @LINE:148
    case controllers_CompilationLibrariesController_updateProcessor85_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProcessor85_invoker.call(CompilationLibrariesController_5.get.updateProcessor(id))
      }
  
    // @LINE:149
    case controllers_CompilationLibrariesController_deleteProcessor86_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteProcessor86_invoker.call(CompilationLibrariesController_5.get.deleteProcessor(id))
      }
  
    // @LINE:151
    case controllers_CompilationLibrariesController_connectProcessorWithLibrary87_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrId", None)) { (id, lbrId) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibrary87_invoker.call(CompilationLibrariesController_5.get.connectProcessorWithLibrary(id, lbrId))
      }
  
    // @LINE:152
    case controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup88_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrgId", None)) { (id, lbrgId) =>
        controllers_CompilationLibrariesController_connectProcessorWithLibraryGroup88_invoker.call(CompilationLibrariesController_5.get.connectProcessorWithLibraryGroup(id, lbrgId))
      }
  
    // @LINE:153
    case controllers_CompilationLibrariesController_unconnectProcessorWithLibrary89_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrId", None)) { (id, lbrId) =>
        controllers_CompilationLibrariesController_unconnectProcessorWithLibrary89_invoker.call(CompilationLibrariesController_5.get.unconnectProcessorWithLibrary(id, lbrId))
      }
  
    // @LINE:154
    case controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup90_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("lbrgId", None)) { (id, lbrgId) =>
        controllers_CompilationLibrariesController_unconnectProcessorWithLibraryGroup90_invoker.call(CompilationLibrariesController_5.get.unconnectProcessorWithLibraryGroup(id, lbrgId))
      }
  
    // @LINE:156
    case controllers_CompilationLibrariesController_getProcessorDescription91_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorDescription91_invoker.call(CompilationLibrariesController_5.get.getProcessorDescription(id))
      }
  
    // @LINE:157
    case controllers_CompilationLibrariesController_getProcessorLibraryGroups92_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorLibraryGroups92_invoker.call(CompilationLibrariesController_5.get.getProcessorLibraryGroups(id))
      }
  
    // @LINE:158
    case controllers_CompilationLibrariesController_getProcessorSingleLibraries93_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorSingleLibraries93_invoker.call(CompilationLibrariesController_5.get.getProcessorSingleLibraries(id))
      }
  
    // @LINE:162
    case controllers_CompilationLibrariesController_newBoard94_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newBoard94_invoker.call(CompilationLibrariesController_5.get.newBoard())
      }
  
    // @LINE:163
    case controllers_CompilationLibrariesController_addUserDescription95_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_addUserDescription95_invoker.call(CompilationLibrariesController_5.get.addUserDescription(id))
      }
  
    // @LINE:164
    case controllers_CompilationLibrariesController_getBoard96_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoard96_invoker.call(CompilationLibrariesController_5.get.getBoard(id))
      }
  
    // @LINE:165
    case controllers_CompilationLibrariesController_deactivateBoard97_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deactivateBoard97_invoker.call(CompilationLibrariesController_5.get.deactivateBoard(id))
      }
  
    // @LINE:166
    case controllers_CompilationLibrariesController_getUserDescription98_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getUserDescription98_invoker.call(CompilationLibrariesController_5.get.getUserDescription(id))
      }
  
    // @LINE:167
    case controllers_CompilationLibrariesController_connectBoardWthProject99_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("pr", None)) { (id, pr) =>
        controllers_CompilationLibrariesController_connectBoardWthProject99_invoker.call(CompilationLibrariesController_5.get.connectBoardWthProject(id, pr))
      }
  
    // @LINE:168
    case controllers_CompilationLibrariesController_unconnectBoardWthProject100_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[String]("pr", None)) { (id, pr) =>
        controllers_CompilationLibrariesController_unconnectBoardWthProject100_invoker.call(CompilationLibrariesController_5.get.unconnectBoardWthProject(id, pr))
      }
  
    // @LINE:169
    case controllers_CompilationLibrariesController_getBoardProjects101_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoardProjects101_invoker.call(CompilationLibrariesController_5.get.getBoardProjects(id))
      }
  
    // @LINE:173
    case controllers_CompilationLibrariesController_newProducers102_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProducers102_invoker.call(CompilationLibrariesController_5.get.newProducers())
      }
  
    // @LINE:174
    case controllers_CompilationLibrariesController_updateProducers103_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProducers103_invoker.call(CompilationLibrariesController_5.get.updateProducers(id))
      }
  
    // @LINE:175
    case controllers_CompilationLibrariesController_getProducers104_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProducers104_invoker.call(CompilationLibrariesController_5.get.getProducers())
      }
  
    // @LINE:176
    case controllers_CompilationLibrariesController_getProducer105_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducer105_invoker.call(CompilationLibrariesController_5.get.getProducer(id))
      }
  
    // @LINE:177
    case controllers_CompilationLibrariesController_getProducerDescription106_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerDescription106_invoker.call(CompilationLibrariesController_5.get.getProducerDescription(id))
      }
  
    // @LINE:178
    case controllers_CompilationLibrariesController_getProducerTypeOfBoards107_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerTypeOfBoards107_invoker.call(CompilationLibrariesController_5.get.getProducerTypeOfBoards(id))
      }
  
    // @LINE:181
    case controllers_CompilationLibrariesController_newTypeOfBoard108_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newTypeOfBoard108_invoker.call(CompilationLibrariesController_5.get.newTypeOfBoard())
      }
  
    // @LINE:182
    case controllers_CompilationLibrariesController_updateTypeOfBoard109_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateTypeOfBoard109_invoker.call(CompilationLibrariesController_5.get.updateTypeOfBoard(id))
      }
  
    // @LINE:183
    case controllers_CompilationLibrariesController_getTypeOfBoards110_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getTypeOfBoards110_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoards())
      }
  
    // @LINE:184
    case controllers_CompilationLibrariesController_getTypeOfBoard111_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoard111_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoard(id))
      }
  
    // @LINE:185
    case controllers_CompilationLibrariesController_getTypeOfBoardDescription112_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardDescription112_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoardDescription(id))
      }
  
    // @LINE:186
    case controllers_CompilationLibrariesController_getTypeOfBoardAllBoards113_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardAllBoards113_invoker.call(CompilationLibrariesController_5.get.getTypeOfBoardAllBoards(id))
      }
  
    // @LINE:190
    case controllers_CompilationLibrariesController_newLibraryGroup114_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newLibraryGroup114_invoker.call(CompilationLibrariesController_5.get.newLibraryGroup())
      }
  
    // @LINE:191
    case controllers_CompilationLibrariesController_getLibraryGroup115_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroup115_invoker.call(CompilationLibrariesController_5.get.getLibraryGroup(id))
      }
  
    // @LINE:192
    case controllers_CompilationLibrariesController_deleteLibraryGroup116_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteLibraryGroup116_invoker.call(CompilationLibrariesController_5.get.deleteLibraryGroup(id))
      }
  
    // @LINE:193
    case controllers_CompilationLibrariesController_getLibraryGroupAll117_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getLibraryGroupAll117_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupAll())
      }
  
    // @LINE:194
    case controllers_CompilationLibrariesController_updateLibraryGroup118_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateLibraryGroup118_invoker.call(CompilationLibrariesController_5.get.updateLibraryGroup(id))
      }
  
    // @LINE:195
    case controllers_CompilationLibrariesController_getLibraryGroupDescription119_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupDescription119_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupDescription(id))
      }
  
    // @LINE:196
    case controllers_CompilationLibrariesController_getLibraryGroupProcessors120_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupProcessors120_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupProcessors(id))
      }
  
    // @LINE:197
    case controllers_CompilationLibrariesController_getLibraryGroupLibraries121_route(params) =>
      call(params.fromPath[String]("libraryId", None), params.fromPath[String]("version", None)) { (libraryId, version) =>
        controllers_CompilationLibrariesController_getLibraryGroupLibraries121_invoker.call(CompilationLibrariesController_5.get.getLibraryGroupLibraries(libraryId, version))
      }
  
    // @LINE:198
    case controllers_CompilationLibrariesController_createNewVersionLibraryGroup122_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_createNewVersionLibraryGroup122_invoker.call(CompilationLibrariesController_5.get.createNewVersionLibraryGroup(id))
      }
  
    // @LINE:199
    case controllers_CompilationLibrariesController_getVersionLibraryGroup123_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getVersionLibraryGroup123_invoker.call(CompilationLibrariesController_5.get.getVersionLibraryGroup(id))
      }
  
    // @LINE:200
    case controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup124_route(params) =>
      call(params.fromPath[String]("libraryId", None), params.fromPath[Double]("version", None)) { (libraryId, version) =>
        controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup124_invoker.call(CompilationLibrariesController_5.get.uploudLibraryToLibraryGroup(libraryId, version))
      }
  
    // @LINE:202
    case controllers_CompilationLibrariesController_listOfFilesInVersion125_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_listOfFilesInVersion125_invoker.call(CompilationLibrariesController_5.get.listOfFilesInVersion(id))
      }
  
    // @LINE:203
    case controllers_CompilationLibrariesController_fileRecord126_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_fileRecord126_invoker.call(CompilationLibrariesController_5.get.fileRecord(id))
      }
  
    // @LINE:206
    case controllers_CompilationLibrariesController_newSingleLibrary127_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newSingleLibrary127_invoker.call(CompilationLibrariesController_5.get.newSingleLibrary())
      }
  
    // @LINE:207
    case controllers_CompilationLibrariesController_newVersionSingleLibrary128_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_newVersionSingleLibrary128_invoker.call(CompilationLibrariesController_5.get.newVersionSingleLibrary(id))
      }
  
    // @LINE:208
    case controllers_CompilationLibrariesController_getSingleLibraryFilter129_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getSingleLibraryFilter129_invoker.call(CompilationLibrariesController_5.get.getSingleLibraryFilter())
      }
  
    // @LINE:209
    case controllers_CompilationLibrariesController_getSingleLibrary130_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getSingleLibrary130_invoker.call(CompilationLibrariesController_5.get.getSingleLibrary(id))
      }
  
    // @LINE:210
    case controllers_CompilationLibrariesController_getSingleLibraryAll131_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getSingleLibraryAll131_invoker.call(CompilationLibrariesController_5.get.getSingleLibraryAll())
      }
  
    // @LINE:212
    case controllers_CompilationLibrariesController_updateSingleLibrary132_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateSingleLibrary132_invoker.call(CompilationLibrariesController_5.get.updateSingleLibrary(id))
      }
  
    // @LINE:213
    case controllers_CompilationLibrariesController_deleteSingleLibrary133_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteSingleLibrary133_invoker.call(CompilationLibrariesController_5.get.deleteSingleLibrary(id))
      }
  
    // @LINE:214
    case controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion134_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[Double]("version", None)) { (id, version) =>
        controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion134_invoker.call(CompilationLibrariesController_5.get.uploadSingleLibraryWithVersion(id, version))
      }
  
    // @LINE:215
    case controllers_CompilationLibrariesController_getSingleLibraryDescription135_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getSingleLibraryDescription135_invoker.call(CompilationLibrariesController_5.get.getSingleLibraryDescription(id))
      }
  
    // @LINE:217
    case controllers_CompilationLibrariesController_generateProjectForEclipse136_route(params) =>
      call { 
        controllers_CompilationLibrariesController_generateProjectForEclipse136_invoker.call(CompilationLibrariesController_5.get.generateProjectForEclipse())
      }
  
    // @LINE:223
    case utilities_swagger_ApiHelpController_getResources137_route(params) =>
      call { 
        utilities_swagger_ApiHelpController_getResources137_invoker.call(ApiHelpController_1.get.getResources)
      }
  
    // @LINE:226
    case controllers_SecurityController_optionLink138_route(params) =>
      call(params.fromPath[String]("all", None)) { (all) =>
        controllers_SecurityController_optionLink138_invoker.call(SecurityController_3.get.optionLink(all))
      }
  }
}

// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/tyrion/conf/routes
// @DATE:Tue Jan 12 19:48:46 CET 2016

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
  // @LINE:8
  SecurityController_2: javax.inject.Provider[controllers.SecurityController],
  // @LINE:12
  OutsideCommunicationPackageController_1: javax.inject.Provider[webSocket.controllers.OutsideCommunicationPackageController],
  // @LINE:27
  PersonCreateController_8: javax.inject.Provider[controllers.PersonCreateController],
  // @LINE:36
  PermissionController_0: javax.inject.Provider[controllers.PermissionController],
  // @LINE:47
  OverFlowController_7: javax.inject.Provider[controllers.OverFlowController],
  // @LINE:88
  ProgramingPackageController_3: javax.inject.Provider[controllers.ProgramingPackageController],
  // @LINE:143
  CompilationLibrariesController_4: javax.inject.Provider[controllers.CompilationLibrariesController],
  // @LINE:201
  Assets_5: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    Application_6: javax.inject.Provider[controllers.Application],
    // @LINE:8
    SecurityController_2: javax.inject.Provider[controllers.SecurityController],
    // @LINE:12
    OutsideCommunicationPackageController_1: javax.inject.Provider[webSocket.controllers.OutsideCommunicationPackageController],
    // @LINE:27
    PersonCreateController_8: javax.inject.Provider[controllers.PersonCreateController],
    // @LINE:36
    PermissionController_0: javax.inject.Provider[controllers.PermissionController],
    // @LINE:47
    OverFlowController_7: javax.inject.Provider[controllers.OverFlowController],
    // @LINE:88
    ProgramingPackageController_3: javax.inject.Provider[controllers.ProgramingPackageController],
    // @LINE:143
    CompilationLibrariesController_4: javax.inject.Provider[controllers.CompilationLibrariesController],
    // @LINE:201
    Assets_5: controllers.Assets
  ) = this(errorHandler, Application_6, SecurityController_2, OutsideCommunicationPackageController_1, PersonCreateController_8, PermissionController_0, OverFlowController_7, ProgramingPackageController_3, CompilationLibrariesController_4, Assets_5, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, Application_6, SecurityController_2, OutsideCommunicationPackageController_1, PersonCreateController_8, PermissionController_0, OverFlowController_7, ProgramingPackageController_3, CompilationLibrariesController_4, Assets_5, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """@controllers.Application@.index()"""),
    ("""OPTIONS""", this.prefix, """@controllers.SecurityController@.option()"""),
    ("""OPTIONS""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """$url<.+>""", """@controllers.SecurityController@.optionLink(url:String)"""),
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
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer""", """@controllers.ProgramingPackageController@.newHomer()"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/$id<[^/]+>""", """@controllers.ProgramingPackageController@.removeHomer(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/homer/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getHomer(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/getAllConnectedHomers/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getConnectedHomers(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/getallhoumers""", """@controllers.ProgramingPackageController@.getAllHomers()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/connectIoTWithProject""", """@controllers.ProgramingPackageController@.connectIoTWithProject()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/unconnectIoTWithProject""", """@controllers.ProgramingPackageController@.unConnectIoTWithProject()"""),
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
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getProcessorDescription(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/processor/libraryGroups""", """@controllers.CompilationLibrariesController@.getProcessorLibraryGroups(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board""", """@controllers.CompilationLibrariesController@.newBoard()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.editBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoard(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteBoard(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/generalDescription/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getBoardgeneralDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/board/userDescription/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getUserDescription(id:String)"""),
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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/TypeOfBoard/boards/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getTypeOfBoardAllBoards(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.newLibraryGroup()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroup(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup""", """@controllers.CompilationLibrariesController@.getLibraryGroupAll()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateLibraryGroup(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroupDescription(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/processors/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroupProcessors(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/libraries/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryGroupLibraries(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/libraryGroup/upload/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.libraryGroupUpload(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.newLibrary()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library""", """@controllers.CompilationLibrariesController@.getLibraryAll()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/filter""", """@controllers.CompilationLibrariesController@.getLibraryFilter()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.updateLibrary(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.deleteLibrary(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/description/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryDescription(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/content/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryContent(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/library/libraryGroups/$id<[^/]+>""", """@controllers.CompilationLibrariesController@.getLibraryLibraryGroups(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """compilation/project/eclipse""", """@controllers.CompilationLibrariesController@.generateProjectForEclipse()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """public/$file<.+>""", """controllers.Assets.at(path:String = "/public", file:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
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
    Application_6.get.index(),
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

  // @LINE:8
  private[this] lazy val controllers_SecurityController_option1_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_SecurityController_option1_invoker = createInvoker(
    SecurityController_2.get.option(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "option",
      Nil,
      "OPTIONS",
      """CORS""",
      this.prefix + """"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_SecurityController_optionLink2_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_optionLink2_invoker = createInvoker(
    SecurityController_2.get.optionLink(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.SecurityController",
      "optionLink",
      Seq(classOf[String]),
      "OPTIONS",
      """""",
      this.prefix + """$url<.+>"""
    )
  )

  // @LINE:12
  private[this] lazy val webSocket_controllers_OutsideCommunicationPackageController_connection3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val webSocket_controllers_OutsideCommunicationPackageController_connection3_invoker = createInvoker(
    OutsideCommunicationPackageController_1.get.connection(fakeValue[String]),
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

  // @LINE:18
  private[this] lazy val controllers_SecurityController_login4_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/login")))
  )
  private[this] lazy val controllers_SecurityController_login4_invoker = createInvoker(
    SecurityController_2.get.login(),
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

  // @LINE:19
  private[this] lazy val controllers_SecurityController_logout5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/logout")))
  )
  private[this] lazy val controllers_SecurityController_logout5_invoker = createInvoker(
    SecurityController_2.get.logout,
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

  // @LINE:27
  private[this] lazy val controllers_PersonCreateController_createNewPerson6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_createNewPerson6_invoker = createInvoker(
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

  // @LINE:28
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation7_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation7_invoker = createInvoker(
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

  // @LINE:29
  private[this] lazy val controllers_PersonCreateController_getPerson8_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_getPerson8_invoker = createInvoker(
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

  // @LINE:30
  private[this] lazy val controllers_PersonCreateController_deletePerson9_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_deletePerson9_invoker = createInvoker(
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

  // @LINE:36
  private[this] lazy val controllers_PermissionController_getAllPermissions10_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionKeys")))
  )
  private[this] lazy val controllers_PermissionController_getAllPermissions10_invoker = createInvoker(
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

  // @LINE:37
  private[this] lazy val controllers_PermissionController_getAllGroups11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionGroups")))
  )
  private[this] lazy val controllers_PermissionController_getAllGroups11_invoker = createInvoker(
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

  // @LINE:38
  private[this] lazy val controllers_PermissionController_createGroup12_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/group")))
  )
  private[this] lazy val controllers_PermissionController_createGroup12_invoker = createInvoker(
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

  // @LINE:40
  private[this] lazy val controllers_PermissionController_getAllPersonPermission13_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_getAllPersonPermission13_invoker = createInvoker(
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

  // @LINE:41
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission14_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission14_invoker = createInvoker(
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

  // @LINE:42
  private[this] lazy val controllers_PermissionController_addAllPersonPermission15_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_addAllPersonPermission15_invoker = createInvoker(
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

  // @LINE:47
  private[this] lazy val controllers_OverFlowController_newPost16_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_newPost16_invoker = createInvoker(
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

  // @LINE:48
  private[this] lazy val controllers_OverFlowController_getPost17_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPost17_invoker = createInvoker(
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

  // @LINE:49
  private[this] lazy val controllers_OverFlowController_deletePost18_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost18_invoker = createInvoker(
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

  // @LINE:50
  private[this] lazy val controllers_OverFlowController_editPost19_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_editPost19_invoker = createInvoker(
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

  // @LINE:51
  private[this] lazy val controllers_OverFlowController_getLatestPost20_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postAll")))
  )
  private[this] lazy val controllers_OverFlowController_getLatestPost20_invoker = createInvoker(
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

  // @LINE:52
  private[this] lazy val controllers_OverFlowController_getPostByFilter21_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postFilter")))
  )
  private[this] lazy val controllers_OverFlowController_getPostByFilter21_invoker = createInvoker(
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

  // @LINE:53
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers22_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/linkedAnswers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers22_invoker = createInvoker(
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

  // @LINE:55
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost23_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/hashTags/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost23_invoker = createInvoker(
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

  // @LINE:56
  private[this] lazy val controllers_OverFlowController_commentsListOnPost24_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/comments/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_commentsListOnPost24_invoker = createInvoker(
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

  // @LINE:57
  private[this] lazy val controllers_OverFlowController_answereListOnPost25_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/answers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_answereListOnPost25_invoker = createInvoker(
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

  // @LINE:58
  private[this] lazy val controllers_OverFlowController_textOfPost26_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/textOfPost/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_textOfPost26_invoker = createInvoker(
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

  // @LINE:60
  private[this] lazy val controllers_OverFlowController_newTypeOfPost27_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfPost27_invoker = createInvoker(
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

  // @LINE:61
  private[this] lazy val controllers_OverFlowController_getTypeOfPost28_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfPost28_invoker = createInvoker(
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

  // @LINE:64
  private[this] lazy val controllers_OverFlowController_addComment29_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment")))
  )
  private[this] lazy val controllers_OverFlowController_addComment29_invoker = createInvoker(
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

  // @LINE:65
  private[this] lazy val controllers_OverFlowController_updateComment30_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment30_invoker = createInvoker(
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

  // @LINE:66
  private[this] lazy val controllers_OverFlowController_deletePost31_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost31_invoker = createInvoker(
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

  // @LINE:68
  private[this] lazy val controllers_OverFlowController_addAnswer32_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer")))
  )
  private[this] lazy val controllers_OverFlowController_addAnswer32_invoker = createInvoker(
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

  // @LINE:69
  private[this] lazy val controllers_OverFlowController_updateComment33_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment33_invoker = createInvoker(
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

  // @LINE:70
  private[this] lazy val controllers_OverFlowController_deletePost34_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost34_invoker = createInvoker(
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

  // @LINE:72
  private[this] lazy val controllers_OverFlowController_likePlus35_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likePlus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likePlus35_invoker = createInvoker(
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

  // @LINE:73
  private[this] lazy val controllers_OverFlowController_likeMinus36_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likeMinus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likeMinus36_invoker = createInvoker(
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

  // @LINE:74
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer37_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link")))
  )
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer37_invoker = createInvoker(
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

  // @LINE:75
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer38_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer38_invoker = createInvoker(
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

  // @LINE:76
  private[this] lazy val controllers_OverFlowController_removeHashTag39_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeLink")))
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
      this.prefix + """overflow/removeLink"""
    )
  )

  // @LINE:77
  private[this] lazy val controllers_OverFlowController_addHashTag40_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/hashTag")))
  )
  private[this] lazy val controllers_OverFlowController_addHashTag40_invoker = createInvoker(
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

  // @LINE:78
  private[this] lazy val controllers_OverFlowController_removeHashTag41_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeHashTag")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag41_invoker = createInvoker(
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

  // @LINE:79
  private[this] lazy val controllers_OverFlowController_addConfirmType42_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/confirm/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_addConfirmType42_invoker = createInvoker(
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

  // @LINE:80
  private[this] lazy val controllers_OverFlowController_removeConfirmType43_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/confirm/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_removeConfirmType43_invoker = createInvoker(
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

  // @LINE:88
  private[this] lazy val controllers_ProgramingPackageController_postNewProject44_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProject44_invoker = createInvoker(
    ProgramingPackageController_3.get.postNewProject(),
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

  // @LINE:89
  private[this] lazy val controllers_ProgramingPackageController_updateProject45_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateProject45_invoker = createInvoker(
    ProgramingPackageController_3.get.updateProject(fakeValue[String]),
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

  // @LINE:90
  private[this] lazy val controllers_ProgramingPackageController_getProject46_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProject46_invoker = createInvoker(
    ProgramingPackageController_3.get.getProject(fakeValue[String]),
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

  // @LINE:91
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount47_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount47_invoker = createInvoker(
    ProgramingPackageController_3.get.getProjectsByUserAccount(),
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

  // @LINE:92
  private[this] lazy val controllers_ProgramingPackageController_deleteProject48_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteProject48_invoker = createInvoker(
    ProgramingPackageController_3.get.deleteProject(fakeValue[String]),
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

  // @LINE:95
  private[this] lazy val controllers_ProgramingPackageController_newHomer49_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newHomer49_invoker = createInvoker(
    ProgramingPackageController_3.get.newHomer(),
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

  // @LINE:96
  private[this] lazy val controllers_ProgramingPackageController_removeHomer50_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeHomer50_invoker = createInvoker(
    ProgramingPackageController_3.get.removeHomer(fakeValue[String]),
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

  // @LINE:97
  private[this] lazy val controllers_ProgramingPackageController_getHomer51_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getHomer51_invoker = createInvoker(
    ProgramingPackageController_3.get.getHomer(fakeValue[String]),
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

  // @LINE:98
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers52_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getAllConnectedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers52_invoker = createInvoker(
    ProgramingPackageController_3.get.getConnectedHomers(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getConnectedHomers",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/getAllConnectedHomers/$id<[^/]+>"""
    )
  )

  // @LINE:99
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers53_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getallhoumers")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers53_invoker = createInvoker(
    ProgramingPackageController_3.get.getAllHomers(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getAllHomers",
      Nil,
      "GET",
      """""",
      this.prefix + """project/getallhoumers"""
    )
  )

  // @LINE:103
  private[this] lazy val controllers_ProgramingPackageController_connectIoTWithProject54_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectIoTWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectIoTWithProject54_invoker = createInvoker(
    ProgramingPackageController_3.get.connectIoTWithProject(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "connectIoTWithProject",
      Nil,
      "PUT",
      """Project - connection""",
      this.prefix + """project/connectIoTWithProject"""
    )
  )

  // @LINE:104
  private[this] lazy val controllers_ProgramingPackageController_unConnectIoTWithProject55_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/unconnectIoTWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_unConnectIoTWithProject55_invoker = createInvoker(
    ProgramingPackageController_3.get.unConnectIoTWithProject(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "unConnectIoTWithProject",
      Nil,
      "PUT",
      """""",
      this.prefix + """project/unconnectIoTWithProject"""
    )
  )

  // @LINE:105
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject56_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject56_invoker = createInvoker(
    ProgramingPackageController_3.get.connectHomerWithProject(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "connectHomerWithProject",
      Nil,
      "PUT",
      """""",
      this.prefix + """project/connectHomerWithProject"""
    )
  )

  // @LINE:106
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject57_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/unconnectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject57_invoker = createInvoker(
    ProgramingPackageController_3.get.unConnectHomerWithProject(),
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
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram58_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram58_invoker = createInvoker(
    ProgramingPackageController_3.get.postNewProgram(),
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
  private[this] lazy val controllers_ProgramingPackageController_getProgram59_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgram59_invoker = createInvoker(
    ProgramingPackageController_3.get.getProgram(fakeValue[String]),
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
  private[this] lazy val controllers_ProgramingPackageController_editProgram60_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editProgram60_invoker = createInvoker(
    ProgramingPackageController_3.get.editProgram(fakeValue[String]),
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
  private[this] lazy val controllers_ProgramingPackageController_removeProgram61_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeProgram61_invoker = createInvoker(
    ProgramingPackageController_3.get.removeProgram(fakeValue[String]),
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
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson62_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/programInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson62_invoker = createInvoker(
    ProgramingPackageController_3.get.getProgramInJson(fakeValue[String]),
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

  // @LINE:115
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms63_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getallprograms/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms63_invoker = createInvoker(
    ProgramingPackageController_3.get.getAllPrograms(fakeValue[String]),
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

  // @LINE:116
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers64_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfUploadedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers64_invoker = createInvoker(
    ProgramingPackageController_3.get.listOfUploadedHomers(fakeValue[String]),
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

  // @LINE:117
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload65_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfHomersWaitingForUpload/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload65_invoker = createInvoker(
    ProgramingPackageController_3.get.listOfHomersWaitingForUpload(fakeValue[String]),
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

  // @LINE:118
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson66_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getProgramInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson66_invoker = createInvoker(
    ProgramingPackageController_3.get.getProgramInJson(fakeValue[String]),
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

  // @LINE:119
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately67_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerImmediately")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately67_invoker = createInvoker(
    ProgramingPackageController_3.get.uploadProgramToHomer_Immediately(),
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

  // @LINE:120
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible68_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerAsSoonAsPossible")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible68_invoker = createInvoker(
    ProgramingPackageController_3.get.uploadProgramToHomer_AsSoonAsPossible(),
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

  // @LINE:121
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible69_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerGivenTime")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible69_invoker = createInvoker(
    ProgramingPackageController_3.get.uploadProgramToHomer_GivenTimeAsSoonAsPossible(),
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

  // @LINE:124
  private[this] lazy val controllers_ProgramingPackageController_newBlock70_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newBlock70_invoker = createInvoker(
    ProgramingPackageController_3.get.newBlock(),
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

  // @LINE:125
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock71_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock71_invoker = createInvoker(
    ProgramingPackageController_3.get.newVersionOfBlock(fakeValue[String]),
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

  // @LINE:126
  private[this] lazy val controllers_ProgramingPackageController_logicJson72_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/logicJson/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_logicJson72_invoker = createInvoker(
    ProgramingPackageController_3.get.logicJson(fakeValue[String]),
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

  // @LINE:127
  private[this] lazy val controllers_ProgramingPackageController_designJson73_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/designJson/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_designJson73_invoker = createInvoker(
    ProgramingPackageController_3.get.designJson(fakeValue[String]),
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

  // @LINE:128
  private[this] lazy val controllers_ProgramingPackageController_generalDescription74_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_generalDescription74_invoker = createInvoker(
    ProgramingPackageController_3.get.generalDescription(fakeValue[String]),
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

  // @LINE:129
  private[this] lazy val controllers_ProgramingPackageController_versionDescription75_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/versionDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_versionDescription75_invoker = createInvoker(
    ProgramingPackageController_3.get.versionDescription(fakeValue[String]),
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

  // @LINE:130
  private[this] lazy val controllers_ProgramingPackageController_getBlock76_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlock76_invoker = createInvoker(
    ProgramingPackageController_3.get.getBlock(fakeValue[String]),
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

  // @LINE:132
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions77_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/allPrevVersions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions77_invoker = createInvoker(
    ProgramingPackageController_3.get.allPrevVersions(fakeValue[String]),
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

  // @LINE:133
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock78_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock78_invoker = createInvoker(
    ProgramingPackageController_3.get.deleteBlock(fakeValue[String]),
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

  // @LINE:134
  private[this] lazy val controllers_ProgramingPackageController_getByFilter79_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/filter")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getByFilter79_invoker = createInvoker(
    ProgramingPackageController_3.get.getByFilter(),
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

  // @LINE:143
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor80_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor80_invoker = createInvoker(
    CompilationLibrariesController_4.get.newProcessor(),
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

  // @LINE:144
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor81_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor81_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProcessor(fakeValue[String]),
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

  // @LINE:145
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll82_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll82_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProcessorAll(),
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

  // @LINE:146
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor83_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor83_invoker = createInvoker(
    CompilationLibrariesController_4.get.updateProcessor(fakeValue[String]),
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

  // @LINE:147
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor84_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor84_invoker = createInvoker(
    CompilationLibrariesController_4.get.deleteProcessor(fakeValue[String]),
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

  // @LINE:148
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription85_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription85_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProcessorDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorDescription",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/processor/description/$id<[^/]+>"""
    )
  )

  // @LINE:149
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups86_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroups")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups86_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProcessorLibraryGroups(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getProcessorLibraryGroups",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/processor/libraryGroups"""
    )
  )

  // @LINE:152
  private[this] lazy val controllers_CompilationLibrariesController_newBoard87_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newBoard87_invoker = createInvoker(
    CompilationLibrariesController_4.get.newBoard(),
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

  // @LINE:153
  private[this] lazy val controllers_CompilationLibrariesController_editBoard88_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_editBoard88_invoker = createInvoker(
    CompilationLibrariesController_4.get.editBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "editBoard",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/board/$id<[^/]+>"""
    )
  )

  // @LINE:154
  private[this] lazy val controllers_CompilationLibrariesController_getBoard89_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoard89_invoker = createInvoker(
    CompilationLibrariesController_4.get.getBoard(fakeValue[String]),
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

  // @LINE:155
  private[this] lazy val controllers_CompilationLibrariesController_deleteBoard90_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteBoard90_invoker = createInvoker(
    CompilationLibrariesController_4.get.deleteBoard(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deleteBoard",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/board/$id<[^/]+>"""
    )
  )

  // @LINE:156
  private[this] lazy val controllers_CompilationLibrariesController_getBoardgeneralDescription91_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardgeneralDescription91_invoker = createInvoker(
    CompilationLibrariesController_4.get.getBoardgeneralDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getBoardgeneralDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/board/generalDescription/$id<[^/]+>"""
    )
  )

  // @LINE:157
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription92_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription92_invoker = createInvoker(
    CompilationLibrariesController_4.get.getUserDescription(fakeValue[String]),
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

  // @LINE:160
  private[this] lazy val controllers_CompilationLibrariesController_newProducers93_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProducers93_invoker = createInvoker(
    CompilationLibrariesController_4.get.newProducers(),
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

  // @LINE:161
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers94_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers94_invoker = createInvoker(
    CompilationLibrariesController_4.get.updateProducers(fakeValue[String]),
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

  // @LINE:162
  private[this] lazy val controllers_CompilationLibrariesController_getProducers95_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducers95_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProducers(),
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

  // @LINE:163
  private[this] lazy val controllers_CompilationLibrariesController_getProducer96_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducer96_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProducer(fakeValue[String]),
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

  // @LINE:164
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription97_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription97_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProducerDescription(fakeValue[String]),
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

  // @LINE:165
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards98_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/typeOfBoards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards98_invoker = createInvoker(
    CompilationLibrariesController_4.get.getProducerTypeOfBoards(fakeValue[String]),
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

  // @LINE:168
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard99_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard99_invoker = createInvoker(
    CompilationLibrariesController_4.get.newTypeOfBoard(),
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

  // @LINE:169
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard100_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard100_invoker = createInvoker(
    CompilationLibrariesController_4.get.updateTypeOfBoard(fakeValue[String]),
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

  // @LINE:170
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards101_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards101_invoker = createInvoker(
    CompilationLibrariesController_4.get.getTypeOfBoards(),
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

  // @LINE:171
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard102_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard102_invoker = createInvoker(
    CompilationLibrariesController_4.get.getTypeOfBoard(fakeValue[String]),
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

  // @LINE:172
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription103_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription103_invoker = createInvoker(
    CompilationLibrariesController_4.get.getTypeOfBoardDescription(fakeValue[String]),
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

  // @LINE:173
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards104_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/TypeOfBoard/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards104_invoker = createInvoker(
    CompilationLibrariesController_4.get.getTypeOfBoardAllBoards(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getTypeOfBoardAllBoards",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/TypeOfBoard/boards/$id<[^/]+>"""
    )
  )

  // @LINE:176
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup105_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup105_invoker = createInvoker(
    CompilationLibrariesController_4.get.newLibraryGroup(),
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

  // @LINE:177
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup106_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup106_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroup(fakeValue[String]),
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

  // @LINE:178
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll107_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll107_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroupAll(),
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

  // @LINE:179
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup108_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup108_invoker = createInvoker(
    CompilationLibrariesController_4.get.updateLibraryGroup(fakeValue[String]),
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

  // @LINE:180
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription109_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription109_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroupDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroupDescription",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/description/$id<[^/]+>"""
    )
  )

  // @LINE:181
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors110_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/processors/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors110_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroupProcessors(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroupProcessors",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/processors/$id<[^/]+>"""
    )
  )

  // @LINE:182
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries111_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/libraries/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries111_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroupLibraries(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryGroupLibraries",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/libraries/$id<[^/]+>"""
    )
  )

  // @LINE:183
  private[this] lazy val controllers_CompilationLibrariesController_libraryGroupUpload112_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/upload/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_libraryGroupUpload112_invoker = createInvoker(
    CompilationLibrariesController_4.get.libraryGroupUpload(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "libraryGroupUpload",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """compilation/libraryGroup/upload/$id<[^/]+>"""
    )
  )

  // @LINE:186
  private[this] lazy val controllers_CompilationLibrariesController_newLibrary113_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newLibrary113_invoker = createInvoker(
    CompilationLibrariesController_4.get.newLibrary(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "newLibrary",
      Nil,
      "POST",
      """Library""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:187
  private[this] lazy val controllers_CompilationLibrariesController_getLibrary114_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibrary114_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibrary",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/$id<[^/]+>"""
    )
  )

  // @LINE:188
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryAll115_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryAll115_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryAll(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryAll",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/library"""
    )
  )

  // @LINE:189
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryFilter116_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryFilter116_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryFilter(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryFilter",
      Nil,
      "GET",
      """""",
      this.prefix + """compilation/library/filter"""
    )
  )

  // @LINE:190
  private[this] lazy val controllers_CompilationLibrariesController_updateLibrary117_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateLibrary117_invoker = createInvoker(
    CompilationLibrariesController_4.get.updateLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "updateLibrary",
      Seq(classOf[String]),
      "PUT",
      """""",
      this.prefix + """compilation/library/$id<[^/]+>"""
    )
  )

  // @LINE:191
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibrary118_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibrary118_invoker = createInvoker(
    CompilationLibrariesController_4.get.deleteLibrary(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "deleteLibrary",
      Seq(classOf[String]),
      "DELETE",
      """""",
      this.prefix + """compilation/library/$id<[^/]+>"""
    )
  )

  // @LINE:192
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryDescription119_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryDescription119_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryDescription(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryDescription",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/description/$id<[^/]+>"""
    )
  )

  // @LINE:193
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryContent120_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/content/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryContent120_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryContent(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryContent",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/content/$id<[^/]+>"""
    )
  )

  // @LINE:194
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryLibraryGroups121_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/libraryGroups/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryLibraryGroups121_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryLibraryGroups(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.CompilationLibrariesController",
      "getLibraryLibraryGroups",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """compilation/library/libraryGroups/$id<[^/]+>"""
    )
  )

  // @LINE:196
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse122_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/eclipse")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse122_invoker = createInvoker(
    CompilationLibrariesController_4.get.generateProjectForEclipse(),
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

  // @LINE:201
  private[this] lazy val controllers_Assets_at123_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("public/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at123_invoker = createInvoker(
    Assets_5.at(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      """## TOOLS ############ TOOLS ############### TOOLS ################ TOOLS ######################################################################################################
###############################################################################################################################################################################
CSS template""",
      this.prefix + """public/$file<.+>"""
    )
  )

  // @LINE:205
  private[this] lazy val controllers_Assets_versioned124_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned124_invoker = createInvoker(
    Assets_5.versioned(fakeValue[String], fakeValue[Asset]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      """ Map static resources from the /public folder to the /assets URL path
GET            /assets/*file                               controllers.Assets.at(path="/public", file)""",
      this.prefix + """assets/$file<.+>"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:6
    case controllers_Application_index0_route(params) =>
      call { 
        controllers_Application_index0_invoker.call(Application_6.get.index())
      }
  
    // @LINE:8
    case controllers_SecurityController_option1_route(params) =>
      call { 
        controllers_SecurityController_option1_invoker.call(SecurityController_2.get.option())
      }
  
    // @LINE:9
    case controllers_SecurityController_optionLink2_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_SecurityController_optionLink2_invoker.call(SecurityController_2.get.optionLink(url))
      }
  
    // @LINE:12
    case webSocket_controllers_OutsideCommunicationPackageController_connection3_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        webSocket_controllers_OutsideCommunicationPackageController_connection3_invoker.call(OutsideCommunicationPackageController_1.get.connection(id))
      }
  
    // @LINE:18
    case controllers_SecurityController_login4_route(params) =>
      call { 
        controllers_SecurityController_login4_invoker.call(SecurityController_2.get.login())
      }
  
    // @LINE:19
    case controllers_SecurityController_logout5_route(params) =>
      call { 
        controllers_SecurityController_logout5_invoker.call(SecurityController_2.get.logout)
      }
  
    // @LINE:27
    case controllers_PersonCreateController_createNewPerson6_route(params) =>
      call { 
        controllers_PersonCreateController_createNewPerson6_invoker.call(PersonCreateController_8.get.createNewPerson())
      }
  
    // @LINE:28
    case controllers_PersonCreateController_updatePersonInformation7_route(params) =>
      call { 
        controllers_PersonCreateController_updatePersonInformation7_invoker.call(PersonCreateController_8.get.updatePersonInformation())
      }
  
    // @LINE:29
    case controllers_PersonCreateController_getPerson8_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_getPerson8_invoker.call(PersonCreateController_8.get.getPerson(id))
      }
  
    // @LINE:30
    case controllers_PersonCreateController_deletePerson9_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_deletePerson9_invoker.call(PersonCreateController_8.get.deletePerson(id))
      }
  
    // @LINE:36
    case controllers_PermissionController_getAllPermissions10_route(params) =>
      call { 
        controllers_PermissionController_getAllPermissions10_invoker.call(PermissionController_0.get.getAllPermissions())
      }
  
    // @LINE:37
    case controllers_PermissionController_getAllGroups11_route(params) =>
      call { 
        controllers_PermissionController_getAllGroups11_invoker.call(PermissionController_0.get.getAllGroups())
      }
  
    // @LINE:38
    case controllers_PermissionController_createGroup12_route(params) =>
      call { 
        controllers_PermissionController_createGroup12_invoker.call(PermissionController_0.get.createGroup())
      }
  
    // @LINE:40
    case controllers_PermissionController_getAllPersonPermission13_route(params) =>
      call { 
        controllers_PermissionController_getAllPersonPermission13_invoker.call(PermissionController_0.get.getAllPersonPermission())
      }
  
    // @LINE:41
    case controllers_PermissionController_removeAllPersonPermission14_route(params) =>
      call { 
        controllers_PermissionController_removeAllPersonPermission14_invoker.call(PermissionController_0.get.removeAllPersonPermission())
      }
  
    // @LINE:42
    case controllers_PermissionController_addAllPersonPermission15_route(params) =>
      call { 
        controllers_PermissionController_addAllPersonPermission15_invoker.call(PermissionController_0.get.addAllPersonPermission())
      }
  
    // @LINE:47
    case controllers_OverFlowController_newPost16_route(params) =>
      call { 
        controllers_OverFlowController_newPost16_invoker.call(OverFlowController_7.get.newPost())
      }
  
    // @LINE:48
    case controllers_OverFlowController_getPost17_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPost17_invoker.call(OverFlowController_7.get.getPost(id))
      }
  
    // @LINE:49
    case controllers_OverFlowController_deletePost18_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost18_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:50
    case controllers_OverFlowController_editPost19_route(params) =>
      call { 
        controllers_OverFlowController_editPost19_invoker.call(OverFlowController_7.get.editPost())
      }
  
    // @LINE:51
    case controllers_OverFlowController_getLatestPost20_route(params) =>
      call { 
        controllers_OverFlowController_getLatestPost20_invoker.call(OverFlowController_7.get.getLatestPost())
      }
  
    // @LINE:52
    case controllers_OverFlowController_getPostByFilter21_route(params) =>
      call { 
        controllers_OverFlowController_getPostByFilter21_invoker.call(OverFlowController_7.get.getPostByFilter())
      }
  
    // @LINE:53
    case controllers_OverFlowController_getPostLinkedAnswers22_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPostLinkedAnswers22_invoker.call(OverFlowController_7.get.getPostLinkedAnswers(id))
      }
  
    // @LINE:55
    case controllers_OverFlowController_hashTagsListOnPost23_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_hashTagsListOnPost23_invoker.call(OverFlowController_7.get.hashTagsListOnPost(id))
      }
  
    // @LINE:56
    case controllers_OverFlowController_commentsListOnPost24_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_commentsListOnPost24_invoker.call(OverFlowController_7.get.commentsListOnPost(id))
      }
  
    // @LINE:57
    case controllers_OverFlowController_answereListOnPost25_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_answereListOnPost25_invoker.call(OverFlowController_7.get.answereListOnPost(id))
      }
  
    // @LINE:58
    case controllers_OverFlowController_textOfPost26_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_textOfPost26_invoker.call(OverFlowController_7.get.textOfPost(id))
      }
  
    // @LINE:60
    case controllers_OverFlowController_newTypeOfPost27_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfPost27_invoker.call(OverFlowController_7.get.newTypeOfPost())
      }
  
    // @LINE:61
    case controllers_OverFlowController_getTypeOfPost28_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfPost28_invoker.call(OverFlowController_7.get.getTypeOfPost())
      }
  
    // @LINE:64
    case controllers_OverFlowController_addComment29_route(params) =>
      call { 
        controllers_OverFlowController_addComment29_invoker.call(OverFlowController_7.get.addComment())
      }
  
    // @LINE:65
    case controllers_OverFlowController_updateComment30_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment30_invoker.call(OverFlowController_7.get.updateComment(id))
      }
  
    // @LINE:66
    case controllers_OverFlowController_deletePost31_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost31_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:68
    case controllers_OverFlowController_addAnswer32_route(params) =>
      call { 
        controllers_OverFlowController_addAnswer32_invoker.call(OverFlowController_7.get.addAnswer())
      }
  
    // @LINE:69
    case controllers_OverFlowController_updateComment33_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment33_invoker.call(OverFlowController_7.get.updateComment(id))
      }
  
    // @LINE:70
    case controllers_OverFlowController_deletePost34_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost34_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:72
    case controllers_OverFlowController_likePlus35_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likePlus35_invoker.call(OverFlowController_7.get.likePlus(id))
      }
  
    // @LINE:73
    case controllers_OverFlowController_likeMinus36_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likeMinus36_invoker.call(OverFlowController_7.get.likeMinus(id))
      }
  
    // @LINE:74
    case controllers_OverFlowController_linkWithPreviousAnswer37_route(params) =>
      call { 
        controllers_OverFlowController_linkWithPreviousAnswer37_invoker.call(OverFlowController_7.get.linkWithPreviousAnswer())
      }
  
    // @LINE:75
    case controllers_OverFlowController_unlinkWithPreviousAnswer38_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_unlinkWithPreviousAnswer38_invoker.call(OverFlowController_7.get.unlinkWithPreviousAnswer(id))
      }
  
    // @LINE:76
    case controllers_OverFlowController_removeHashTag39_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag39_invoker.call(OverFlowController_7.get.removeHashTag())
      }
  
    // @LINE:77
    case controllers_OverFlowController_addHashTag40_route(params) =>
      call { 
        controllers_OverFlowController_addHashTag40_invoker.call(OverFlowController_7.get.addHashTag())
      }
  
    // @LINE:78
    case controllers_OverFlowController_removeHashTag41_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag41_invoker.call(OverFlowController_7.get.removeHashTag())
      }
  
    // @LINE:79
    case controllers_OverFlowController_addConfirmType42_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_addConfirmType42_invoker.call(OverFlowController_7.get.addConfirmType(id))
      }
  
    // @LINE:80
    case controllers_OverFlowController_removeConfirmType43_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_removeConfirmType43_invoker.call(OverFlowController_7.get.removeConfirmType(id))
      }
  
    // @LINE:88
    case controllers_ProgramingPackageController_postNewProject44_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProject44_invoker.call(ProgramingPackageController_3.get.postNewProject())
      }
  
    // @LINE:89
    case controllers_ProgramingPackageController_updateProject45_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateProject45_invoker.call(ProgramingPackageController_3.get.updateProject(id))
      }
  
    // @LINE:90
    case controllers_ProgramingPackageController_getProject46_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProject46_invoker.call(ProgramingPackageController_3.get.getProject(id))
      }
  
    // @LINE:91
    case controllers_ProgramingPackageController_getProjectsByUserAccount47_route(params) =>
      call { 
        controllers_ProgramingPackageController_getProjectsByUserAccount47_invoker.call(ProgramingPackageController_3.get.getProjectsByUserAccount())
      }
  
    // @LINE:92
    case controllers_ProgramingPackageController_deleteProject48_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteProject48_invoker.call(ProgramingPackageController_3.get.deleteProject(id))
      }
  
    // @LINE:95
    case controllers_ProgramingPackageController_newHomer49_route(params) =>
      call { 
        controllers_ProgramingPackageController_newHomer49_invoker.call(ProgramingPackageController_3.get.newHomer())
      }
  
    // @LINE:96
    case controllers_ProgramingPackageController_removeHomer50_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeHomer50_invoker.call(ProgramingPackageController_3.get.removeHomer(id))
      }
  
    // @LINE:97
    case controllers_ProgramingPackageController_getHomer51_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getHomer51_invoker.call(ProgramingPackageController_3.get.getHomer(id))
      }
  
    // @LINE:98
    case controllers_ProgramingPackageController_getConnectedHomers52_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getConnectedHomers52_invoker.call(ProgramingPackageController_3.get.getConnectedHomers(id))
      }
  
    // @LINE:99
    case controllers_ProgramingPackageController_getAllHomers53_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllHomers53_invoker.call(ProgramingPackageController_3.get.getAllHomers())
      }
  
    // @LINE:103
    case controllers_ProgramingPackageController_connectIoTWithProject54_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectIoTWithProject54_invoker.call(ProgramingPackageController_3.get.connectIoTWithProject())
      }
  
    // @LINE:104
    case controllers_ProgramingPackageController_unConnectIoTWithProject55_route(params) =>
      call { 
        controllers_ProgramingPackageController_unConnectIoTWithProject55_invoker.call(ProgramingPackageController_3.get.unConnectIoTWithProject())
      }
  
    // @LINE:105
    case controllers_ProgramingPackageController_connectHomerWithProject56_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectHomerWithProject56_invoker.call(ProgramingPackageController_3.get.connectHomerWithProject())
      }
  
    // @LINE:106
    case controllers_ProgramingPackageController_unConnectHomerWithProject57_route(params) =>
      call { 
        controllers_ProgramingPackageController_unConnectHomerWithProject57_invoker.call(ProgramingPackageController_3.get.unConnectHomerWithProject())
      }
  
    // @LINE:109
    case controllers_ProgramingPackageController_postNewProgram58_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProgram58_invoker.call(ProgramingPackageController_3.get.postNewProgram())
      }
  
    // @LINE:110
    case controllers_ProgramingPackageController_getProgram59_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgram59_invoker.call(ProgramingPackageController_3.get.getProgram(id))
      }
  
    // @LINE:111
    case controllers_ProgramingPackageController_editProgram60_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editProgram60_invoker.call(ProgramingPackageController_3.get.editProgram(id))
      }
  
    // @LINE:112
    case controllers_ProgramingPackageController_removeProgram61_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeProgram61_invoker.call(ProgramingPackageController_3.get.removeProgram(id))
      }
  
    // @LINE:113
    case controllers_ProgramingPackageController_getProgramInJson62_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson62_invoker.call(ProgramingPackageController_3.get.getProgramInJson(id))
      }
  
    // @LINE:115
    case controllers_ProgramingPackageController_getAllPrograms63_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAllPrograms63_invoker.call(ProgramingPackageController_3.get.getAllPrograms(id))
      }
  
    // @LINE:116
    case controllers_ProgramingPackageController_listOfUploadedHomers64_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfUploadedHomers64_invoker.call(ProgramingPackageController_3.get.listOfUploadedHomers(id))
      }
  
    // @LINE:117
    case controllers_ProgramingPackageController_listOfHomersWaitingForUpload65_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfHomersWaitingForUpload65_invoker.call(ProgramingPackageController_3.get.listOfHomersWaitingForUpload(id))
      }
  
    // @LINE:118
    case controllers_ProgramingPackageController_getProgramInJson66_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson66_invoker.call(ProgramingPackageController_3.get.getProgramInJson(id))
      }
  
    // @LINE:119
    case controllers_ProgramingPackageController_uploadProgramToHomer_Immediately67_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_Immediately67_invoker.call(ProgramingPackageController_3.get.uploadProgramToHomer_Immediately())
      }
  
    // @LINE:120
    case controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible68_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible68_invoker.call(ProgramingPackageController_3.get.uploadProgramToHomer_AsSoonAsPossible())
      }
  
    // @LINE:121
    case controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible69_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible69_invoker.call(ProgramingPackageController_3.get.uploadProgramToHomer_GivenTimeAsSoonAsPossible())
      }
  
    // @LINE:124
    case controllers_ProgramingPackageController_newBlock70_route(params) =>
      call { 
        controllers_ProgramingPackageController_newBlock70_invoker.call(ProgramingPackageController_3.get.newBlock())
      }
  
    // @LINE:125
    case controllers_ProgramingPackageController_newVersionOfBlock71_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_newVersionOfBlock71_invoker.call(ProgramingPackageController_3.get.newVersionOfBlock(id))
      }
  
    // @LINE:126
    case controllers_ProgramingPackageController_logicJson72_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_logicJson72_invoker.call(ProgramingPackageController_3.get.logicJson(url))
      }
  
    // @LINE:127
    case controllers_ProgramingPackageController_designJson73_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_designJson73_invoker.call(ProgramingPackageController_3.get.designJson(url))
      }
  
    // @LINE:128
    case controllers_ProgramingPackageController_generalDescription74_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_generalDescription74_invoker.call(ProgramingPackageController_3.get.generalDescription(id))
      }
  
    // @LINE:129
    case controllers_ProgramingPackageController_versionDescription75_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_versionDescription75_invoker.call(ProgramingPackageController_3.get.versionDescription(id))
      }
  
    // @LINE:130
    case controllers_ProgramingPackageController_getBlock76_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_getBlock76_invoker.call(ProgramingPackageController_3.get.getBlock(url))
      }
  
    // @LINE:132
    case controllers_ProgramingPackageController_allPrevVersions77_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_allPrevVersions77_invoker.call(ProgramingPackageController_3.get.allPrevVersions(id))
      }
  
    // @LINE:133
    case controllers_ProgramingPackageController_deleteBlock78_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_deleteBlock78_invoker.call(ProgramingPackageController_3.get.deleteBlock(url))
      }
  
    // @LINE:134
    case controllers_ProgramingPackageController_getByFilter79_route(params) =>
      call { 
        controllers_ProgramingPackageController_getByFilter79_invoker.call(ProgramingPackageController_3.get.getByFilter())
      }
  
    // @LINE:143
    case controllers_CompilationLibrariesController_newProcessor80_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProcessor80_invoker.call(CompilationLibrariesController_4.get.newProcessor())
      }
  
    // @LINE:144
    case controllers_CompilationLibrariesController_getProcessor81_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessor81_invoker.call(CompilationLibrariesController_4.get.getProcessor(id))
      }
  
    // @LINE:145
    case controllers_CompilationLibrariesController_getProcessorAll82_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProcessorAll82_invoker.call(CompilationLibrariesController_4.get.getProcessorAll())
      }
  
    // @LINE:146
    case controllers_CompilationLibrariesController_updateProcessor83_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProcessor83_invoker.call(CompilationLibrariesController_4.get.updateProcessor(id))
      }
  
    // @LINE:147
    case controllers_CompilationLibrariesController_deleteProcessor84_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteProcessor84_invoker.call(CompilationLibrariesController_4.get.deleteProcessor(id))
      }
  
    // @LINE:148
    case controllers_CompilationLibrariesController_getProcessorDescription85_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorDescription85_invoker.call(CompilationLibrariesController_4.get.getProcessorDescription(id))
      }
  
    // @LINE:149
    case controllers_CompilationLibrariesController_getProcessorLibraryGroups86_route(params) =>
      call(params.fromQuery[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorLibraryGroups86_invoker.call(CompilationLibrariesController_4.get.getProcessorLibraryGroups(id))
      }
  
    // @LINE:152
    case controllers_CompilationLibrariesController_newBoard87_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newBoard87_invoker.call(CompilationLibrariesController_4.get.newBoard())
      }
  
    // @LINE:153
    case controllers_CompilationLibrariesController_editBoard88_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_editBoard88_invoker.call(CompilationLibrariesController_4.get.editBoard(id))
      }
  
    // @LINE:154
    case controllers_CompilationLibrariesController_getBoard89_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoard89_invoker.call(CompilationLibrariesController_4.get.getBoard(id))
      }
  
    // @LINE:155
    case controllers_CompilationLibrariesController_deleteBoard90_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteBoard90_invoker.call(CompilationLibrariesController_4.get.deleteBoard(id))
      }
  
    // @LINE:156
    case controllers_CompilationLibrariesController_getBoardgeneralDescription91_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoardgeneralDescription91_invoker.call(CompilationLibrariesController_4.get.getBoardgeneralDescription(id))
      }
  
    // @LINE:157
    case controllers_CompilationLibrariesController_getUserDescription92_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getUserDescription92_invoker.call(CompilationLibrariesController_4.get.getUserDescription(id))
      }
  
    // @LINE:160
    case controllers_CompilationLibrariesController_newProducers93_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProducers93_invoker.call(CompilationLibrariesController_4.get.newProducers())
      }
  
    // @LINE:161
    case controllers_CompilationLibrariesController_updateProducers94_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProducers94_invoker.call(CompilationLibrariesController_4.get.updateProducers(id))
      }
  
    // @LINE:162
    case controllers_CompilationLibrariesController_getProducers95_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProducers95_invoker.call(CompilationLibrariesController_4.get.getProducers())
      }
  
    // @LINE:163
    case controllers_CompilationLibrariesController_getProducer96_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducer96_invoker.call(CompilationLibrariesController_4.get.getProducer(id))
      }
  
    // @LINE:164
    case controllers_CompilationLibrariesController_getProducerDescription97_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerDescription97_invoker.call(CompilationLibrariesController_4.get.getProducerDescription(id))
      }
  
    // @LINE:165
    case controllers_CompilationLibrariesController_getProducerTypeOfBoards98_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerTypeOfBoards98_invoker.call(CompilationLibrariesController_4.get.getProducerTypeOfBoards(id))
      }
  
    // @LINE:168
    case controllers_CompilationLibrariesController_newTypeOfBoard99_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newTypeOfBoard99_invoker.call(CompilationLibrariesController_4.get.newTypeOfBoard())
      }
  
    // @LINE:169
    case controllers_CompilationLibrariesController_updateTypeOfBoard100_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateTypeOfBoard100_invoker.call(CompilationLibrariesController_4.get.updateTypeOfBoard(id))
      }
  
    // @LINE:170
    case controllers_CompilationLibrariesController_getTypeOfBoards101_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getTypeOfBoards101_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoards())
      }
  
    // @LINE:171
    case controllers_CompilationLibrariesController_getTypeOfBoard102_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoard102_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoard(id))
      }
  
    // @LINE:172
    case controllers_CompilationLibrariesController_getTypeOfBoardDescription103_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardDescription103_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoardDescription(id))
      }
  
    // @LINE:173
    case controllers_CompilationLibrariesController_getTypeOfBoardAllBoards104_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardAllBoards104_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoardAllBoards(id))
      }
  
    // @LINE:176
    case controllers_CompilationLibrariesController_newLibraryGroup105_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newLibraryGroup105_invoker.call(CompilationLibrariesController_4.get.newLibraryGroup())
      }
  
    // @LINE:177
    case controllers_CompilationLibrariesController_getLibraryGroup106_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroup106_invoker.call(CompilationLibrariesController_4.get.getLibraryGroup(id))
      }
  
    // @LINE:178
    case controllers_CompilationLibrariesController_getLibraryGroupAll107_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getLibraryGroupAll107_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupAll())
      }
  
    // @LINE:179
    case controllers_CompilationLibrariesController_updateLibraryGroup108_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateLibraryGroup108_invoker.call(CompilationLibrariesController_4.get.updateLibraryGroup(id))
      }
  
    // @LINE:180
    case controllers_CompilationLibrariesController_getLibraryGroupDescription109_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupDescription109_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupDescription(id))
      }
  
    // @LINE:181
    case controllers_CompilationLibrariesController_getLibraryGroupProcessors110_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupProcessors110_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupProcessors(id))
      }
  
    // @LINE:182
    case controllers_CompilationLibrariesController_getLibraryGroupLibraries111_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupLibraries111_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupLibraries(id))
      }
  
    // @LINE:183
    case controllers_CompilationLibrariesController_libraryGroupUpload112_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_libraryGroupUpload112_invoker.call(CompilationLibrariesController_4.get.libraryGroupUpload(id))
      }
  
    // @LINE:186
    case controllers_CompilationLibrariesController_newLibrary113_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newLibrary113_invoker.call(CompilationLibrariesController_4.get.newLibrary())
      }
  
    // @LINE:187
    case controllers_CompilationLibrariesController_getLibrary114_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibrary114_invoker.call(CompilationLibrariesController_4.get.getLibrary(id))
      }
  
    // @LINE:188
    case controllers_CompilationLibrariesController_getLibraryAll115_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getLibraryAll115_invoker.call(CompilationLibrariesController_4.get.getLibraryAll())
      }
  
    // @LINE:189
    case controllers_CompilationLibrariesController_getLibraryFilter116_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getLibraryFilter116_invoker.call(CompilationLibrariesController_4.get.getLibraryFilter())
      }
  
    // @LINE:190
    case controllers_CompilationLibrariesController_updateLibrary117_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateLibrary117_invoker.call(CompilationLibrariesController_4.get.updateLibrary(id))
      }
  
    // @LINE:191
    case controllers_CompilationLibrariesController_deleteLibrary118_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteLibrary118_invoker.call(CompilationLibrariesController_4.get.deleteLibrary(id))
      }
  
    // @LINE:192
    case controllers_CompilationLibrariesController_getLibraryDescription119_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryDescription119_invoker.call(CompilationLibrariesController_4.get.getLibraryDescription(id))
      }
  
    // @LINE:193
    case controllers_CompilationLibrariesController_getLibraryContent120_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryContent120_invoker.call(CompilationLibrariesController_4.get.getLibraryContent(id))
      }
  
    // @LINE:194
    case controllers_CompilationLibrariesController_getLibraryLibraryGroups121_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryLibraryGroups121_invoker.call(CompilationLibrariesController_4.get.getLibraryLibraryGroups(id))
      }
  
    // @LINE:196
    case controllers_CompilationLibrariesController_generateProjectForEclipse122_route(params) =>
      call { 
        controllers_CompilationLibrariesController_generateProjectForEclipse122_invoker.call(CompilationLibrariesController_4.get.generateProjectForEclipse())
      }
  
    // @LINE:201
    case controllers_Assets_at123_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at123_invoker.call(Assets_5.at(path, file))
      }
  
    // @LINE:205
    case controllers_Assets_versioned124_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned124_invoker.call(Assets_5.versioned(path, file))
      }
  }
}
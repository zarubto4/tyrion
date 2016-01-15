
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Fri Jan 15 18:09:49 CET 2016

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
  SecurityController_2: javax.inject.Provider[controllers.SecurityController],
  // @LINE:13
  OutsideCommunicationPackageController_1: javax.inject.Provider[webSocket.controllers.OutsideCommunicationPackageController],
  // @LINE:28
  PersonCreateController_8: javax.inject.Provider[controllers.PersonCreateController],
  // @LINE:37
  PermissionController_0: javax.inject.Provider[controllers.PermissionController],
  // @LINE:48
  OverFlowController_7: javax.inject.Provider[controllers.OverFlowController],
  // @LINE:89
  ProgramingPackageController_3: javax.inject.Provider[controllers.ProgramingPackageController],
  // @LINE:148
  CompilationLibrariesController_4: javax.inject.Provider[controllers.CompilationLibrariesController],
  // @LINE:213
  Assets_5: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    Application_6: javax.inject.Provider[controllers.Application],
    // @LINE:9
    SecurityController_2: javax.inject.Provider[controllers.SecurityController],
    // @LINE:13
    OutsideCommunicationPackageController_1: javax.inject.Provider[webSocket.controllers.OutsideCommunicationPackageController],
    // @LINE:28
    PersonCreateController_8: javax.inject.Provider[controllers.PersonCreateController],
    // @LINE:37
    PermissionController_0: javax.inject.Provider[controllers.PermissionController],
    // @LINE:48
    OverFlowController_7: javax.inject.Provider[controllers.OverFlowController],
    // @LINE:89
    ProgramingPackageController_3: javax.inject.Provider[controllers.ProgramingPackageController],
    // @LINE:148
    CompilationLibrariesController_4: javax.inject.Provider[controllers.CompilationLibrariesController],
    // @LINE:213
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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test1""", """@controllers.Application@.test1()"""),
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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/programs/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramPrograms(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/electronicDevicesList/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramelectronicDevicesList(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """project/project/homerList/$id<[^/]+>""", """@controllers.ProgramingPackageController@.getProgramhomerList(id:String)"""),
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

  // @LINE:7
  private[this] lazy val controllers_Application_test11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test1")))
  )
  private[this] lazy val controllers_Application_test11_invoker = createInvoker(
    Application_6.get.test1(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "test1",
      Nil,
      "GET",
      """""",
      this.prefix + """test1"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_SecurityController_option2_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_SecurityController_option2_invoker = createInvoker(
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

  // @LINE:10
  private[this] lazy val controllers_SecurityController_optionLink3_route = Route("OPTIONS",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_SecurityController_optionLink3_invoker = createInvoker(
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

  // @LINE:13
  private[this] lazy val webSocket_controllers_OutsideCommunicationPackageController_connection4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("websocket/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val webSocket_controllers_OutsideCommunicationPackageController_connection4_invoker = createInvoker(
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

  // @LINE:19
  private[this] lazy val controllers_SecurityController_login5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/login")))
  )
  private[this] lazy val controllers_SecurityController_login5_invoker = createInvoker(
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

  // @LINE:20
  private[this] lazy val controllers_SecurityController_logout6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/permission/logout")))
  )
  private[this] lazy val controllers_SecurityController_logout6_invoker = createInvoker(
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

  // @LINE:28
  private[this] lazy val controllers_PersonCreateController_createNewPerson7_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_createNewPerson7_invoker = createInvoker(
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

  // @LINE:29
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation8_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person")))
  )
  private[this] lazy val controllers_PersonCreateController_updatePersonInformation8_invoker = createInvoker(
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

  // @LINE:30
  private[this] lazy val controllers_PersonCreateController_getPerson9_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_getPerson9_invoker = createInvoker(
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

  // @LINE:31
  private[this] lazy val controllers_PersonCreateController_deletePerson10_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("coreClient/person/person/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PersonCreateController_deletePerson10_invoker = createInvoker(
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

  // @LINE:37
  private[this] lazy val controllers_PermissionController_getAllPermissions11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionKeys")))
  )
  private[this] lazy val controllers_PermissionController_getAllPermissions11_invoker = createInvoker(
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

  // @LINE:38
  private[this] lazy val controllers_PermissionController_getAllGroups12_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/permisionGroups")))
  )
  private[this] lazy val controllers_PermissionController_getAllGroups12_invoker = createInvoker(
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

  // @LINE:39
  private[this] lazy val controllers_PermissionController_createGroup13_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/group")))
  )
  private[this] lazy val controllers_PermissionController_createGroup13_invoker = createInvoker(
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

  // @LINE:41
  private[this] lazy val controllers_PermissionController_getAllPersonPermission14_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_getAllPersonPermission14_invoker = createInvoker(
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

  // @LINE:42
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission15_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_removeAllPersonPermission15_invoker = createInvoker(
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

  // @LINE:43
  private[this] lazy val controllers_PermissionController_addAllPersonPermission16_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("permission/personPermission")))
  )
  private[this] lazy val controllers_PermissionController_addAllPersonPermission16_invoker = createInvoker(
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

  // @LINE:48
  private[this] lazy val controllers_OverFlowController_newPost17_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_newPost17_invoker = createInvoker(
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

  // @LINE:49
  private[this] lazy val controllers_OverFlowController_getPost18_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPost18_invoker = createInvoker(
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

  // @LINE:50
  private[this] lazy val controllers_OverFlowController_deletePost19_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost19_invoker = createInvoker(
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

  // @LINE:51
  private[this] lazy val controllers_OverFlowController_editPost20_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post")))
  )
  private[this] lazy val controllers_OverFlowController_editPost20_invoker = createInvoker(
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

  // @LINE:52
  private[this] lazy val controllers_OverFlowController_getLatestPost21_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postAll")))
  )
  private[this] lazy val controllers_OverFlowController_getLatestPost21_invoker = createInvoker(
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

  // @LINE:53
  private[this] lazy val controllers_OverFlowController_getPostByFilter22_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/postFilter")))
  )
  private[this] lazy val controllers_OverFlowController_getPostByFilter22_invoker = createInvoker(
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

  // @LINE:54
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers23_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/linkedAnswers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_getPostLinkedAnswers23_invoker = createInvoker(
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

  // @LINE:56
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost24_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/hashTags/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_hashTagsListOnPost24_invoker = createInvoker(
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

  // @LINE:57
  private[this] lazy val controllers_OverFlowController_commentsListOnPost25_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/comments/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_commentsListOnPost25_invoker = createInvoker(
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

  // @LINE:58
  private[this] lazy val controllers_OverFlowController_answereListOnPost26_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/answers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_answereListOnPost26_invoker = createInvoker(
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

  // @LINE:59
  private[this] lazy val controllers_OverFlowController_textOfPost27_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/post/textOfPost/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_textOfPost27_invoker = createInvoker(
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

  // @LINE:61
  private[this] lazy val controllers_OverFlowController_newTypeOfPost28_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_newTypeOfPost28_invoker = createInvoker(
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

  // @LINE:62
  private[this] lazy val controllers_OverFlowController_getTypeOfPost29_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/typeOfPost")))
  )
  private[this] lazy val controllers_OverFlowController_getTypeOfPost29_invoker = createInvoker(
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

  // @LINE:65
  private[this] lazy val controllers_OverFlowController_addComment30_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment")))
  )
  private[this] lazy val controllers_OverFlowController_addComment30_invoker = createInvoker(
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

  // @LINE:66
  private[this] lazy val controllers_OverFlowController_updateComment31_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
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
      this.prefix + """overflow/comment/$id<[^/]+>"""
    )
  )

  // @LINE:67
  private[this] lazy val controllers_OverFlowController_deletePost32_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/comment/"), DynamicPart("id", """[^/]+""",true)))
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
      this.prefix + """overflow/comment/$id<[^/]+>"""
    )
  )

  // @LINE:69
  private[this] lazy val controllers_OverFlowController_addAnswer33_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer")))
  )
  private[this] lazy val controllers_OverFlowController_addAnswer33_invoker = createInvoker(
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

  // @LINE:70
  private[this] lazy val controllers_OverFlowController_updateComment34_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_updateComment34_invoker = createInvoker(
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

  // @LINE:71
  private[this] lazy val controllers_OverFlowController_deletePost35_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/answer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_deletePost35_invoker = createInvoker(
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

  // @LINE:73
  private[this] lazy val controllers_OverFlowController_likePlus36_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likePlus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likePlus36_invoker = createInvoker(
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

  // @LINE:74
  private[this] lazy val controllers_OverFlowController_likeMinus37_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/likeMinus/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_likeMinus37_invoker = createInvoker(
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

  // @LINE:75
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer38_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link")))
  )
  private[this] lazy val controllers_OverFlowController_linkWithPreviousAnswer38_invoker = createInvoker(
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

  // @LINE:76
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer39_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/link/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_unlinkWithPreviousAnswer39_invoker = createInvoker(
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

  // @LINE:77
  private[this] lazy val controllers_OverFlowController_removeHashTag40_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeLink")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag40_invoker = createInvoker(
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

  // @LINE:78
  private[this] lazy val controllers_OverFlowController_addHashTag41_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/hashTag")))
  )
  private[this] lazy val controllers_OverFlowController_addHashTag41_invoker = createInvoker(
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

  // @LINE:79
  private[this] lazy val controllers_OverFlowController_removeHashTag42_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/removeHashTag")))
  )
  private[this] lazy val controllers_OverFlowController_removeHashTag42_invoker = createInvoker(
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

  // @LINE:80
  private[this] lazy val controllers_OverFlowController_addConfirmType43_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/confirm/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_addConfirmType43_invoker = createInvoker(
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

  // @LINE:81
  private[this] lazy val controllers_OverFlowController_removeConfirmType44_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("overflow/confirm/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_OverFlowController_removeConfirmType44_invoker = createInvoker(
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

  // @LINE:89
  private[this] lazy val controllers_ProgramingPackageController_postNewProject45_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProject45_invoker = createInvoker(
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

  // @LINE:90
  private[this] lazy val controllers_ProgramingPackageController_updateProject46_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_updateProject46_invoker = createInvoker(
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

  // @LINE:91
  private[this] lazy val controllers_ProgramingPackageController_getProject47_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProject47_invoker = createInvoker(
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

  // @LINE:92
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount48_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProjectsByUserAccount48_invoker = createInvoker(
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

  // @LINE:93
  private[this] lazy val controllers_ProgramingPackageController_deleteProject49_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteProject49_invoker = createInvoker(
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

  // @LINE:96
  private[this] lazy val controllers_ProgramingPackageController_newHomer50_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newHomer50_invoker = createInvoker(
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

  // @LINE:97
  private[this] lazy val controllers_ProgramingPackageController_removeHomer51_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeHomer51_invoker = createInvoker(
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

  // @LINE:98
  private[this] lazy val controllers_ProgramingPackageController_getHomer52_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/homer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getHomer52_invoker = createInvoker(
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

  // @LINE:99
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers53_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getAllConnectedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getConnectedHomers53_invoker = createInvoker(
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

  // @LINE:100
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers54_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getallhoumers")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllHomers54_invoker = createInvoker(
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

  // @LINE:104
  private[this] lazy val controllers_ProgramingPackageController_connectIoTWithProject55_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectIoTWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectIoTWithProject55_invoker = createInvoker(
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

  // @LINE:105
  private[this] lazy val controllers_ProgramingPackageController_unConnectIoTWithProject56_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/unconnectIoTWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_unConnectIoTWithProject56_invoker = createInvoker(
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

  // @LINE:106
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject57_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/connectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_connectHomerWithProject57_invoker = createInvoker(
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

  // @LINE:107
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject58_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/unconnectHomerWithProject")))
  )
  private[this] lazy val controllers_ProgramingPackageController_unConnectHomerWithProject58_invoker = createInvoker(
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

  // @LINE:110
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram59_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program")))
  )
  private[this] lazy val controllers_ProgramingPackageController_postNewProgram59_invoker = createInvoker(
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

  // @LINE:111
  private[this] lazy val controllers_ProgramingPackageController_getProgram60_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgram60_invoker = createInvoker(
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

  // @LINE:112
  private[this] lazy val controllers_ProgramingPackageController_editProgram61_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_editProgram61_invoker = createInvoker(
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

  // @LINE:113
  private[this] lazy val controllers_ProgramingPackageController_removeProgram62_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/program/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_removeProgram62_invoker = createInvoker(
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

  // @LINE:114
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson63_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/programInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson63_invoker = createInvoker(
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
  private[this] lazy val controllers_ProgramingPackageController_getProgramPrograms64_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/programs/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramPrograms64_invoker = createInvoker(
    ProgramingPackageController_3.get.getProgramPrograms(fakeValue[String]),
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

  // @LINE:116
  private[this] lazy val controllers_ProgramingPackageController_getProgramelectronicDevicesList65_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/electronicDevicesList/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramelectronicDevicesList65_invoker = createInvoker(
    ProgramingPackageController_3.get.getProgramelectronicDevicesList(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ProgramingPackageController",
      "getProgramelectronicDevicesList",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """project/project/electronicDevicesList/$id<[^/]+>"""
    )
  )

  // @LINE:117
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList66_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/project/homerList/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramhomerList66_invoker = createInvoker(
    ProgramingPackageController_3.get.getProgramhomerList(fakeValue[String]),
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

  // @LINE:120
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms67_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getallprograms/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getAllPrograms67_invoker = createInvoker(
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

  // @LINE:121
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers68_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfUploadedHomers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfUploadedHomers68_invoker = createInvoker(
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

  // @LINE:122
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload69_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/listOfHomersWaitingForUpload/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_listOfHomersWaitingForUpload69_invoker = createInvoker(
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

  // @LINE:123
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson70_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/getProgramInJson/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getProgramInJson70_invoker = createInvoker(
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

  // @LINE:124
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately71_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerImmediately")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_Immediately71_invoker = createInvoker(
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

  // @LINE:125
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible72_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerAsSoonAsPossible")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible72_invoker = createInvoker(
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

  // @LINE:126
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible73_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/uploudtohomerGivenTime")))
  )
  private[this] lazy val controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible73_invoker = createInvoker(
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

  // @LINE:129
  private[this] lazy val controllers_ProgramingPackageController_newBlock74_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock")))
  )
  private[this] lazy val controllers_ProgramingPackageController_newBlock74_invoker = createInvoker(
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

  // @LINE:130
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock75_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_newVersionOfBlock75_invoker = createInvoker(
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

  // @LINE:131
  private[this] lazy val controllers_ProgramingPackageController_logicJson76_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/logicJson/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_logicJson76_invoker = createInvoker(
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

  // @LINE:132
  private[this] lazy val controllers_ProgramingPackageController_designJson77_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/designJson/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_designJson77_invoker = createInvoker(
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

  // @LINE:133
  private[this] lazy val controllers_ProgramingPackageController_generalDescription78_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_generalDescription78_invoker = createInvoker(
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

  // @LINE:134
  private[this] lazy val controllers_ProgramingPackageController_versionDescription79_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/versionDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_versionDescription79_invoker = createInvoker(
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

  // @LINE:135
  private[this] lazy val controllers_ProgramingPackageController_getBlock80_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_getBlock80_invoker = createInvoker(
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

  // @LINE:137
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions81_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/allPrevVersions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ProgramingPackageController_allPrevVersions81_invoker = createInvoker(
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

  // @LINE:138
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock82_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/"), DynamicPart("url", """.+""",false)))
  )
  private[this] lazy val controllers_ProgramingPackageController_deleteBlock82_invoker = createInvoker(
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

  // @LINE:139
  private[this] lazy val controllers_ProgramingPackageController_getByFilter83_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("project/blockoBlock/filter")))
  )
  private[this] lazy val controllers_ProgramingPackageController_getByFilter83_invoker = createInvoker(
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

  // @LINE:148
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor84_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProcessor84_invoker = createInvoker(
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

  // @LINE:149
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor85_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessor85_invoker = createInvoker(
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

  // @LINE:150
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll86_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorAll86_invoker = createInvoker(
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

  // @LINE:151
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor87_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProcessor87_invoker = createInvoker(
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

  // @LINE:152
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor88_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteProcessor88_invoker = createInvoker(
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

  // @LINE:153
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription89_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorDescription89_invoker = createInvoker(
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

  // @LINE:154
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups90_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/processor/libraryGroups")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProcessorLibraryGroups90_invoker = createInvoker(
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

  // @LINE:157
  private[this] lazy val controllers_CompilationLibrariesController_newBoard91_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newBoard91_invoker = createInvoker(
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

  // @LINE:158
  private[this] lazy val controllers_CompilationLibrariesController_editBoard92_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_editBoard92_invoker = createInvoker(
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

  // @LINE:159
  private[this] lazy val controllers_CompilationLibrariesController_getBoard93_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoard93_invoker = createInvoker(
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

  // @LINE:160
  private[this] lazy val controllers_CompilationLibrariesController_deleteBoard94_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteBoard94_invoker = createInvoker(
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

  // @LINE:161
  private[this] lazy val controllers_CompilationLibrariesController_getBoardgeneralDescription95_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getBoardgeneralDescription95_invoker = createInvoker(
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

  // @LINE:162
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription96_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/board/userDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getUserDescription96_invoker = createInvoker(
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

  // @LINE:165
  private[this] lazy val controllers_CompilationLibrariesController_newProducers97_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newProducers97_invoker = createInvoker(
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

  // @LINE:166
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers98_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateProducers98_invoker = createInvoker(
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

  // @LINE:167
  private[this] lazy val controllers_CompilationLibrariesController_getProducers99_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducers99_invoker = createInvoker(
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

  // @LINE:168
  private[this] lazy val controllers_CompilationLibrariesController_getProducer100_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducer100_invoker = createInvoker(
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

  // @LINE:169
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription101_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerDescription101_invoker = createInvoker(
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

  // @LINE:170
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards102_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/producer/typeOfBoards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getProducerTypeOfBoards102_invoker = createInvoker(
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

  // @LINE:173
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard103_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newTypeOfBoard103_invoker = createInvoker(
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

  // @LINE:174
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard104_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateTypeOfBoard104_invoker = createInvoker(
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

  // @LINE:175
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards105_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoards105_invoker = createInvoker(
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

  // @LINE:176
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard106_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoard106_invoker = createInvoker(
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

  // @LINE:177
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription107_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/typeOfBoard/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardDescription107_invoker = createInvoker(
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

  // @LINE:178
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards108_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/TypeOfBoard/boards/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getTypeOfBoardAllBoards108_invoker = createInvoker(
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

  // @LINE:181
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup109_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newLibraryGroup109_invoker = createInvoker(
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

  // @LINE:182
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup110_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroup110_invoker = createInvoker(
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

  // @LINE:183
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibraryGroup111_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteLibraryGroup111_invoker = createInvoker(
    CompilationLibrariesController_4.get.deleteLibraryGroup(fakeValue[String]),
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

  // @LINE:184
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll112_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupAll112_invoker = createInvoker(
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

  // @LINE:185
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup113_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateLibraryGroup113_invoker = createInvoker(
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

  // @LINE:186
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription114_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/generalDescription/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupDescription114_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroupDescription(fakeValue[String]),
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

  // @LINE:187
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors115_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/processors/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupProcessors115_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroupProcessors(fakeValue[String]),
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

  // @LINE:188
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries116_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/libraries/"), DynamicPart("libraryId", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getLibraryGroupLibraries116_invoker = createInvoker(
    CompilationLibrariesController_4.get.getLibraryGroupLibraries(fakeValue[String], fakeValue[String]),
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

  // @LINE:189
  private[this] lazy val controllers_CompilationLibrariesController_createNewVersionLibraryGroup117_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_createNewVersionLibraryGroup117_invoker = createInvoker(
    CompilationLibrariesController_4.get.createNewVersionLibraryGroup(fakeValue[String]),
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

  // @LINE:190
  private[this] lazy val controllers_CompilationLibrariesController_getVersionLibraryGroup118_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/versions/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getVersionLibraryGroup118_invoker = createInvoker(
    CompilationLibrariesController_4.get.getVersionLibraryGroup(fakeValue[String]),
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

  // @LINE:191
  private[this] lazy val controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup119_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/libraryGroup/upload/"), DynamicPart("libraryId", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup119_invoker = createInvoker(
    CompilationLibrariesController_4.get.uploudLibraryToLibraryGroup(fakeValue[String], fakeValue[Double]),
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

  // @LINE:193
  private[this] lazy val controllers_CompilationLibrariesController_listOfFilesInVersion120_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/listOfFiles/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_listOfFilesInVersion120_invoker = createInvoker(
    CompilationLibrariesController_4.get.listOfFilesInVersion(fakeValue[String]),
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

  // @LINE:194
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord121_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/fileRecord/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_fileRecord121_invoker = createInvoker(
    CompilationLibrariesController_4.get.fileRecord(fakeValue[String]),
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

  // @LINE:197
  private[this] lazy val controllers_CompilationLibrariesController_newSingleLibrary122_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newSingleLibrary122_invoker = createInvoker(
    CompilationLibrariesController_4.get.newSingleLibrary(),
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

  // @LINE:198
  private[this] lazy val controllers_CompilationLibrariesController_newVersionSingleLibrary123_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/version/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_newVersionSingleLibrary123_invoker = createInvoker(
    CompilationLibrariesController_4.get.newVersionSingleLibrary(fakeValue[String]),
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

  // @LINE:199
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryFilter124_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/filter")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryFilter124_invoker = createInvoker(
    CompilationLibrariesController_4.get.getSingleLibraryFilter(),
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

  // @LINE:200
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibrary125_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibrary125_invoker = createInvoker(
    CompilationLibrariesController_4.get.getSingleLibrary(fakeValue[String]),
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

  // @LINE:201
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryAll126_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryAll126_invoker = createInvoker(
    CompilationLibrariesController_4.get.getSingleLibraryAll(),
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

  // @LINE:203
  private[this] lazy val controllers_CompilationLibrariesController_updateSingleLibrary127_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_updateSingleLibrary127_invoker = createInvoker(
    CompilationLibrariesController_4.get.updateSingleLibrary(fakeValue[String]),
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

  // @LINE:204
  private[this] lazy val controllers_CompilationLibrariesController_deleteSingleLibrary128_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_deleteSingleLibrary128_invoker = createInvoker(
    CompilationLibrariesController_4.get.deleteSingleLibrary(fakeValue[String]),
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

  // @LINE:205
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion129_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/uploud/"), DynamicPart("id", """[^/]+""",true), StaticPart("/"), DynamicPart("version", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion129_invoker = createInvoker(
    CompilationLibrariesController_4.get.uploadSingleLibraryWithVersion(fakeValue[String], fakeValue[Double]),
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

  // @LINE:206
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryDescription130_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/library/description/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_CompilationLibrariesController_getSingleLibraryDescription130_invoker = createInvoker(
    CompilationLibrariesController_4.get.getSingleLibraryDescription(fakeValue[String]),
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

  // @LINE:208
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse131_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("compilation/project/eclipse")))
  )
  private[this] lazy val controllers_CompilationLibrariesController_generateProjectForEclipse131_invoker = createInvoker(
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

  // @LINE:213
  private[this] lazy val controllers_Assets_at132_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("public/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at132_invoker = createInvoker(
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

  // @LINE:217
  private[this] lazy val controllers_Assets_versioned133_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned133_invoker = createInvoker(
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
  
    // @LINE:7
    case controllers_Application_test11_route(params) =>
      call { 
        controllers_Application_test11_invoker.call(Application_6.get.test1())
      }
  
    // @LINE:9
    case controllers_SecurityController_option2_route(params) =>
      call { 
        controllers_SecurityController_option2_invoker.call(SecurityController_2.get.option())
      }
  
    // @LINE:10
    case controllers_SecurityController_optionLink3_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_SecurityController_optionLink3_invoker.call(SecurityController_2.get.optionLink(url))
      }
  
    // @LINE:13
    case webSocket_controllers_OutsideCommunicationPackageController_connection4_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        webSocket_controllers_OutsideCommunicationPackageController_connection4_invoker.call(OutsideCommunicationPackageController_1.get.connection(id))
      }
  
    // @LINE:19
    case controllers_SecurityController_login5_route(params) =>
      call { 
        controllers_SecurityController_login5_invoker.call(SecurityController_2.get.login())
      }
  
    // @LINE:20
    case controllers_SecurityController_logout6_route(params) =>
      call { 
        controllers_SecurityController_logout6_invoker.call(SecurityController_2.get.logout)
      }
  
    // @LINE:28
    case controllers_PersonCreateController_createNewPerson7_route(params) =>
      call { 
        controllers_PersonCreateController_createNewPerson7_invoker.call(PersonCreateController_8.get.createNewPerson())
      }
  
    // @LINE:29
    case controllers_PersonCreateController_updatePersonInformation8_route(params) =>
      call { 
        controllers_PersonCreateController_updatePersonInformation8_invoker.call(PersonCreateController_8.get.updatePersonInformation())
      }
  
    // @LINE:30
    case controllers_PersonCreateController_getPerson9_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_getPerson9_invoker.call(PersonCreateController_8.get.getPerson(id))
      }
  
    // @LINE:31
    case controllers_PersonCreateController_deletePerson10_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_PersonCreateController_deletePerson10_invoker.call(PersonCreateController_8.get.deletePerson(id))
      }
  
    // @LINE:37
    case controllers_PermissionController_getAllPermissions11_route(params) =>
      call { 
        controllers_PermissionController_getAllPermissions11_invoker.call(PermissionController_0.get.getAllPermissions())
      }
  
    // @LINE:38
    case controllers_PermissionController_getAllGroups12_route(params) =>
      call { 
        controllers_PermissionController_getAllGroups12_invoker.call(PermissionController_0.get.getAllGroups())
      }
  
    // @LINE:39
    case controllers_PermissionController_createGroup13_route(params) =>
      call { 
        controllers_PermissionController_createGroup13_invoker.call(PermissionController_0.get.createGroup())
      }
  
    // @LINE:41
    case controllers_PermissionController_getAllPersonPermission14_route(params) =>
      call { 
        controllers_PermissionController_getAllPersonPermission14_invoker.call(PermissionController_0.get.getAllPersonPermission())
      }
  
    // @LINE:42
    case controllers_PermissionController_removeAllPersonPermission15_route(params) =>
      call { 
        controllers_PermissionController_removeAllPersonPermission15_invoker.call(PermissionController_0.get.removeAllPersonPermission())
      }
  
    // @LINE:43
    case controllers_PermissionController_addAllPersonPermission16_route(params) =>
      call { 
        controllers_PermissionController_addAllPersonPermission16_invoker.call(PermissionController_0.get.addAllPersonPermission())
      }
  
    // @LINE:48
    case controllers_OverFlowController_newPost17_route(params) =>
      call { 
        controllers_OverFlowController_newPost17_invoker.call(OverFlowController_7.get.newPost())
      }
  
    // @LINE:49
    case controllers_OverFlowController_getPost18_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPost18_invoker.call(OverFlowController_7.get.getPost(id))
      }
  
    // @LINE:50
    case controllers_OverFlowController_deletePost19_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost19_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:51
    case controllers_OverFlowController_editPost20_route(params) =>
      call { 
        controllers_OverFlowController_editPost20_invoker.call(OverFlowController_7.get.editPost())
      }
  
    // @LINE:52
    case controllers_OverFlowController_getLatestPost21_route(params) =>
      call { 
        controllers_OverFlowController_getLatestPost21_invoker.call(OverFlowController_7.get.getLatestPost())
      }
  
    // @LINE:53
    case controllers_OverFlowController_getPostByFilter22_route(params) =>
      call { 
        controllers_OverFlowController_getPostByFilter22_invoker.call(OverFlowController_7.get.getPostByFilter())
      }
  
    // @LINE:54
    case controllers_OverFlowController_getPostLinkedAnswers23_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_getPostLinkedAnswers23_invoker.call(OverFlowController_7.get.getPostLinkedAnswers(id))
      }
  
    // @LINE:56
    case controllers_OverFlowController_hashTagsListOnPost24_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_hashTagsListOnPost24_invoker.call(OverFlowController_7.get.hashTagsListOnPost(id))
      }
  
    // @LINE:57
    case controllers_OverFlowController_commentsListOnPost25_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_commentsListOnPost25_invoker.call(OverFlowController_7.get.commentsListOnPost(id))
      }
  
    // @LINE:58
    case controllers_OverFlowController_answereListOnPost26_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_answereListOnPost26_invoker.call(OverFlowController_7.get.answereListOnPost(id))
      }
  
    // @LINE:59
    case controllers_OverFlowController_textOfPost27_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_textOfPost27_invoker.call(OverFlowController_7.get.textOfPost(id))
      }
  
    // @LINE:61
    case controllers_OverFlowController_newTypeOfPost28_route(params) =>
      call { 
        controllers_OverFlowController_newTypeOfPost28_invoker.call(OverFlowController_7.get.newTypeOfPost())
      }
  
    // @LINE:62
    case controllers_OverFlowController_getTypeOfPost29_route(params) =>
      call { 
        controllers_OverFlowController_getTypeOfPost29_invoker.call(OverFlowController_7.get.getTypeOfPost())
      }
  
    // @LINE:65
    case controllers_OverFlowController_addComment30_route(params) =>
      call { 
        controllers_OverFlowController_addComment30_invoker.call(OverFlowController_7.get.addComment())
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
    case controllers_OverFlowController_addAnswer33_route(params) =>
      call { 
        controllers_OverFlowController_addAnswer33_invoker.call(OverFlowController_7.get.addAnswer())
      }
  
    // @LINE:70
    case controllers_OverFlowController_updateComment34_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_updateComment34_invoker.call(OverFlowController_7.get.updateComment(id))
      }
  
    // @LINE:71
    case controllers_OverFlowController_deletePost35_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_deletePost35_invoker.call(OverFlowController_7.get.deletePost(id))
      }
  
    // @LINE:73
    case controllers_OverFlowController_likePlus36_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likePlus36_invoker.call(OverFlowController_7.get.likePlus(id))
      }
  
    // @LINE:74
    case controllers_OverFlowController_likeMinus37_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_likeMinus37_invoker.call(OverFlowController_7.get.likeMinus(id))
      }
  
    // @LINE:75
    case controllers_OverFlowController_linkWithPreviousAnswer38_route(params) =>
      call { 
        controllers_OverFlowController_linkWithPreviousAnswer38_invoker.call(OverFlowController_7.get.linkWithPreviousAnswer())
      }
  
    // @LINE:76
    case controllers_OverFlowController_unlinkWithPreviousAnswer39_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_unlinkWithPreviousAnswer39_invoker.call(OverFlowController_7.get.unlinkWithPreviousAnswer(id))
      }
  
    // @LINE:77
    case controllers_OverFlowController_removeHashTag40_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag40_invoker.call(OverFlowController_7.get.removeHashTag())
      }
  
    // @LINE:78
    case controllers_OverFlowController_addHashTag41_route(params) =>
      call { 
        controllers_OverFlowController_addHashTag41_invoker.call(OverFlowController_7.get.addHashTag())
      }
  
    // @LINE:79
    case controllers_OverFlowController_removeHashTag42_route(params) =>
      call { 
        controllers_OverFlowController_removeHashTag42_invoker.call(OverFlowController_7.get.removeHashTag())
      }
  
    // @LINE:80
    case controllers_OverFlowController_addConfirmType43_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_addConfirmType43_invoker.call(OverFlowController_7.get.addConfirmType(id))
      }
  
    // @LINE:81
    case controllers_OverFlowController_removeConfirmType44_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_OverFlowController_removeConfirmType44_invoker.call(OverFlowController_7.get.removeConfirmType(id))
      }
  
    // @LINE:89
    case controllers_ProgramingPackageController_postNewProject45_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProject45_invoker.call(ProgramingPackageController_3.get.postNewProject())
      }
  
    // @LINE:90
    case controllers_ProgramingPackageController_updateProject46_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_updateProject46_invoker.call(ProgramingPackageController_3.get.updateProject(id))
      }
  
    // @LINE:91
    case controllers_ProgramingPackageController_getProject47_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProject47_invoker.call(ProgramingPackageController_3.get.getProject(id))
      }
  
    // @LINE:92
    case controllers_ProgramingPackageController_getProjectsByUserAccount48_route(params) =>
      call { 
        controllers_ProgramingPackageController_getProjectsByUserAccount48_invoker.call(ProgramingPackageController_3.get.getProjectsByUserAccount())
      }
  
    // @LINE:93
    case controllers_ProgramingPackageController_deleteProject49_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_deleteProject49_invoker.call(ProgramingPackageController_3.get.deleteProject(id))
      }
  
    // @LINE:96
    case controllers_ProgramingPackageController_newHomer50_route(params) =>
      call { 
        controllers_ProgramingPackageController_newHomer50_invoker.call(ProgramingPackageController_3.get.newHomer())
      }
  
    // @LINE:97
    case controllers_ProgramingPackageController_removeHomer51_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeHomer51_invoker.call(ProgramingPackageController_3.get.removeHomer(id))
      }
  
    // @LINE:98
    case controllers_ProgramingPackageController_getHomer52_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getHomer52_invoker.call(ProgramingPackageController_3.get.getHomer(id))
      }
  
    // @LINE:99
    case controllers_ProgramingPackageController_getConnectedHomers53_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getConnectedHomers53_invoker.call(ProgramingPackageController_3.get.getConnectedHomers(id))
      }
  
    // @LINE:100
    case controllers_ProgramingPackageController_getAllHomers54_route(params) =>
      call { 
        controllers_ProgramingPackageController_getAllHomers54_invoker.call(ProgramingPackageController_3.get.getAllHomers())
      }
  
    // @LINE:104
    case controllers_ProgramingPackageController_connectIoTWithProject55_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectIoTWithProject55_invoker.call(ProgramingPackageController_3.get.connectIoTWithProject())
      }
  
    // @LINE:105
    case controllers_ProgramingPackageController_unConnectIoTWithProject56_route(params) =>
      call { 
        controllers_ProgramingPackageController_unConnectIoTWithProject56_invoker.call(ProgramingPackageController_3.get.unConnectIoTWithProject())
      }
  
    // @LINE:106
    case controllers_ProgramingPackageController_connectHomerWithProject57_route(params) =>
      call { 
        controllers_ProgramingPackageController_connectHomerWithProject57_invoker.call(ProgramingPackageController_3.get.connectHomerWithProject())
      }
  
    // @LINE:107
    case controllers_ProgramingPackageController_unConnectHomerWithProject58_route(params) =>
      call { 
        controllers_ProgramingPackageController_unConnectHomerWithProject58_invoker.call(ProgramingPackageController_3.get.unConnectHomerWithProject())
      }
  
    // @LINE:110
    case controllers_ProgramingPackageController_postNewProgram59_route(params) =>
      call { 
        controllers_ProgramingPackageController_postNewProgram59_invoker.call(ProgramingPackageController_3.get.postNewProgram())
      }
  
    // @LINE:111
    case controllers_ProgramingPackageController_getProgram60_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgram60_invoker.call(ProgramingPackageController_3.get.getProgram(id))
      }
  
    // @LINE:112
    case controllers_ProgramingPackageController_editProgram61_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_editProgram61_invoker.call(ProgramingPackageController_3.get.editProgram(id))
      }
  
    // @LINE:113
    case controllers_ProgramingPackageController_removeProgram62_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_removeProgram62_invoker.call(ProgramingPackageController_3.get.removeProgram(id))
      }
  
    // @LINE:114
    case controllers_ProgramingPackageController_getProgramInJson63_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson63_invoker.call(ProgramingPackageController_3.get.getProgramInJson(id))
      }
  
    // @LINE:115
    case controllers_ProgramingPackageController_getProgramPrograms64_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramPrograms64_invoker.call(ProgramingPackageController_3.get.getProgramPrograms(id))
      }
  
    // @LINE:116
    case controllers_ProgramingPackageController_getProgramelectronicDevicesList65_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramelectronicDevicesList65_invoker.call(ProgramingPackageController_3.get.getProgramelectronicDevicesList(id))
      }
  
    // @LINE:117
    case controllers_ProgramingPackageController_getProgramhomerList66_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramhomerList66_invoker.call(ProgramingPackageController_3.get.getProgramhomerList(id))
      }
  
    // @LINE:120
    case controllers_ProgramingPackageController_getAllPrograms67_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getAllPrograms67_invoker.call(ProgramingPackageController_3.get.getAllPrograms(id))
      }
  
    // @LINE:121
    case controllers_ProgramingPackageController_listOfUploadedHomers68_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfUploadedHomers68_invoker.call(ProgramingPackageController_3.get.listOfUploadedHomers(id))
      }
  
    // @LINE:122
    case controllers_ProgramingPackageController_listOfHomersWaitingForUpload69_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_listOfHomersWaitingForUpload69_invoker.call(ProgramingPackageController_3.get.listOfHomersWaitingForUpload(id))
      }
  
    // @LINE:123
    case controllers_ProgramingPackageController_getProgramInJson70_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_getProgramInJson70_invoker.call(ProgramingPackageController_3.get.getProgramInJson(id))
      }
  
    // @LINE:124
    case controllers_ProgramingPackageController_uploadProgramToHomer_Immediately71_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_Immediately71_invoker.call(ProgramingPackageController_3.get.uploadProgramToHomer_Immediately())
      }
  
    // @LINE:125
    case controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible72_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_AsSoonAsPossible72_invoker.call(ProgramingPackageController_3.get.uploadProgramToHomer_AsSoonAsPossible())
      }
  
    // @LINE:126
    case controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible73_route(params) =>
      call { 
        controllers_ProgramingPackageController_uploadProgramToHomer_GivenTimeAsSoonAsPossible73_invoker.call(ProgramingPackageController_3.get.uploadProgramToHomer_GivenTimeAsSoonAsPossible())
      }
  
    // @LINE:129
    case controllers_ProgramingPackageController_newBlock74_route(params) =>
      call { 
        controllers_ProgramingPackageController_newBlock74_invoker.call(ProgramingPackageController_3.get.newBlock())
      }
  
    // @LINE:130
    case controllers_ProgramingPackageController_newVersionOfBlock75_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_newVersionOfBlock75_invoker.call(ProgramingPackageController_3.get.newVersionOfBlock(id))
      }
  
    // @LINE:131
    case controllers_ProgramingPackageController_logicJson76_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_logicJson76_invoker.call(ProgramingPackageController_3.get.logicJson(url))
      }
  
    // @LINE:132
    case controllers_ProgramingPackageController_designJson77_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_designJson77_invoker.call(ProgramingPackageController_3.get.designJson(url))
      }
  
    // @LINE:133
    case controllers_ProgramingPackageController_generalDescription78_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_generalDescription78_invoker.call(ProgramingPackageController_3.get.generalDescription(id))
      }
  
    // @LINE:134
    case controllers_ProgramingPackageController_versionDescription79_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_versionDescription79_invoker.call(ProgramingPackageController_3.get.versionDescription(id))
      }
  
    // @LINE:135
    case controllers_ProgramingPackageController_getBlock80_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_getBlock80_invoker.call(ProgramingPackageController_3.get.getBlock(url))
      }
  
    // @LINE:137
    case controllers_ProgramingPackageController_allPrevVersions81_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ProgramingPackageController_allPrevVersions81_invoker.call(ProgramingPackageController_3.get.allPrevVersions(id))
      }
  
    // @LINE:138
    case controllers_ProgramingPackageController_deleteBlock82_route(params) =>
      call(params.fromPath[String]("url", None)) { (url) =>
        controllers_ProgramingPackageController_deleteBlock82_invoker.call(ProgramingPackageController_3.get.deleteBlock(url))
      }
  
    // @LINE:139
    case controllers_ProgramingPackageController_getByFilter83_route(params) =>
      call { 
        controllers_ProgramingPackageController_getByFilter83_invoker.call(ProgramingPackageController_3.get.getByFilter())
      }
  
    // @LINE:148
    case controllers_CompilationLibrariesController_newProcessor84_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProcessor84_invoker.call(CompilationLibrariesController_4.get.newProcessor())
      }
  
    // @LINE:149
    case controllers_CompilationLibrariesController_getProcessor85_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessor85_invoker.call(CompilationLibrariesController_4.get.getProcessor(id))
      }
  
    // @LINE:150
    case controllers_CompilationLibrariesController_getProcessorAll86_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProcessorAll86_invoker.call(CompilationLibrariesController_4.get.getProcessorAll())
      }
  
    // @LINE:151
    case controllers_CompilationLibrariesController_updateProcessor87_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProcessor87_invoker.call(CompilationLibrariesController_4.get.updateProcessor(id))
      }
  
    // @LINE:152
    case controllers_CompilationLibrariesController_deleteProcessor88_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteProcessor88_invoker.call(CompilationLibrariesController_4.get.deleteProcessor(id))
      }
  
    // @LINE:153
    case controllers_CompilationLibrariesController_getProcessorDescription89_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorDescription89_invoker.call(CompilationLibrariesController_4.get.getProcessorDescription(id))
      }
  
    // @LINE:154
    case controllers_CompilationLibrariesController_getProcessorLibraryGroups90_route(params) =>
      call(params.fromQuery[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProcessorLibraryGroups90_invoker.call(CompilationLibrariesController_4.get.getProcessorLibraryGroups(id))
      }
  
    // @LINE:157
    case controllers_CompilationLibrariesController_newBoard91_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newBoard91_invoker.call(CompilationLibrariesController_4.get.newBoard())
      }
  
    // @LINE:158
    case controllers_CompilationLibrariesController_editBoard92_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_editBoard92_invoker.call(CompilationLibrariesController_4.get.editBoard(id))
      }
  
    // @LINE:159
    case controllers_CompilationLibrariesController_getBoard93_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoard93_invoker.call(CompilationLibrariesController_4.get.getBoard(id))
      }
  
    // @LINE:160
    case controllers_CompilationLibrariesController_deleteBoard94_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteBoard94_invoker.call(CompilationLibrariesController_4.get.deleteBoard(id))
      }
  
    // @LINE:161
    case controllers_CompilationLibrariesController_getBoardgeneralDescription95_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getBoardgeneralDescription95_invoker.call(CompilationLibrariesController_4.get.getBoardgeneralDescription(id))
      }
  
    // @LINE:162
    case controllers_CompilationLibrariesController_getUserDescription96_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getUserDescription96_invoker.call(CompilationLibrariesController_4.get.getUserDescription(id))
      }
  
    // @LINE:165
    case controllers_CompilationLibrariesController_newProducers97_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newProducers97_invoker.call(CompilationLibrariesController_4.get.newProducers())
      }
  
    // @LINE:166
    case controllers_CompilationLibrariesController_updateProducers98_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateProducers98_invoker.call(CompilationLibrariesController_4.get.updateProducers(id))
      }
  
    // @LINE:167
    case controllers_CompilationLibrariesController_getProducers99_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getProducers99_invoker.call(CompilationLibrariesController_4.get.getProducers())
      }
  
    // @LINE:168
    case controllers_CompilationLibrariesController_getProducer100_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducer100_invoker.call(CompilationLibrariesController_4.get.getProducer(id))
      }
  
    // @LINE:169
    case controllers_CompilationLibrariesController_getProducerDescription101_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerDescription101_invoker.call(CompilationLibrariesController_4.get.getProducerDescription(id))
      }
  
    // @LINE:170
    case controllers_CompilationLibrariesController_getProducerTypeOfBoards102_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getProducerTypeOfBoards102_invoker.call(CompilationLibrariesController_4.get.getProducerTypeOfBoards(id))
      }
  
    // @LINE:173
    case controllers_CompilationLibrariesController_newTypeOfBoard103_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newTypeOfBoard103_invoker.call(CompilationLibrariesController_4.get.newTypeOfBoard())
      }
  
    // @LINE:174
    case controllers_CompilationLibrariesController_updateTypeOfBoard104_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateTypeOfBoard104_invoker.call(CompilationLibrariesController_4.get.updateTypeOfBoard(id))
      }
  
    // @LINE:175
    case controllers_CompilationLibrariesController_getTypeOfBoards105_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getTypeOfBoards105_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoards())
      }
  
    // @LINE:176
    case controllers_CompilationLibrariesController_getTypeOfBoard106_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoard106_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoard(id))
      }
  
    // @LINE:177
    case controllers_CompilationLibrariesController_getTypeOfBoardDescription107_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardDescription107_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoardDescription(id))
      }
  
    // @LINE:178
    case controllers_CompilationLibrariesController_getTypeOfBoardAllBoards108_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getTypeOfBoardAllBoards108_invoker.call(CompilationLibrariesController_4.get.getTypeOfBoardAllBoards(id))
      }
  
    // @LINE:181
    case controllers_CompilationLibrariesController_newLibraryGroup109_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newLibraryGroup109_invoker.call(CompilationLibrariesController_4.get.newLibraryGroup())
      }
  
    // @LINE:182
    case controllers_CompilationLibrariesController_getLibraryGroup110_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroup110_invoker.call(CompilationLibrariesController_4.get.getLibraryGroup(id))
      }
  
    // @LINE:183
    case controllers_CompilationLibrariesController_deleteLibraryGroup111_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteLibraryGroup111_invoker.call(CompilationLibrariesController_4.get.deleteLibraryGroup(id))
      }
  
    // @LINE:184
    case controllers_CompilationLibrariesController_getLibraryGroupAll112_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getLibraryGroupAll112_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupAll())
      }
  
    // @LINE:185
    case controllers_CompilationLibrariesController_updateLibraryGroup113_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateLibraryGroup113_invoker.call(CompilationLibrariesController_4.get.updateLibraryGroup(id))
      }
  
    // @LINE:186
    case controllers_CompilationLibrariesController_getLibraryGroupDescription114_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupDescription114_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupDescription(id))
      }
  
    // @LINE:187
    case controllers_CompilationLibrariesController_getLibraryGroupProcessors115_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getLibraryGroupProcessors115_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupProcessors(id))
      }
  
    // @LINE:188
    case controllers_CompilationLibrariesController_getLibraryGroupLibraries116_route(params) =>
      call(params.fromPath[String]("libraryId", None), params.fromPath[String]("version", None)) { (libraryId, version) =>
        controllers_CompilationLibrariesController_getLibraryGroupLibraries116_invoker.call(CompilationLibrariesController_4.get.getLibraryGroupLibraries(libraryId, version))
      }
  
    // @LINE:189
    case controllers_CompilationLibrariesController_createNewVersionLibraryGroup117_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_createNewVersionLibraryGroup117_invoker.call(CompilationLibrariesController_4.get.createNewVersionLibraryGroup(id))
      }
  
    // @LINE:190
    case controllers_CompilationLibrariesController_getVersionLibraryGroup118_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getVersionLibraryGroup118_invoker.call(CompilationLibrariesController_4.get.getVersionLibraryGroup(id))
      }
  
    // @LINE:191
    case controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup119_route(params) =>
      call(params.fromPath[String]("libraryId", None), params.fromPath[Double]("version", None)) { (libraryId, version) =>
        controllers_CompilationLibrariesController_uploudLibraryToLibraryGroup119_invoker.call(CompilationLibrariesController_4.get.uploudLibraryToLibraryGroup(libraryId, version))
      }
  
    // @LINE:193
    case controllers_CompilationLibrariesController_listOfFilesInVersion120_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_listOfFilesInVersion120_invoker.call(CompilationLibrariesController_4.get.listOfFilesInVersion(id))
      }
  
    // @LINE:194
    case controllers_CompilationLibrariesController_fileRecord121_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_fileRecord121_invoker.call(CompilationLibrariesController_4.get.fileRecord(id))
      }
  
    // @LINE:197
    case controllers_CompilationLibrariesController_newSingleLibrary122_route(params) =>
      call { 
        controllers_CompilationLibrariesController_newSingleLibrary122_invoker.call(CompilationLibrariesController_4.get.newSingleLibrary())
      }
  
    // @LINE:198
    case controllers_CompilationLibrariesController_newVersionSingleLibrary123_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_newVersionSingleLibrary123_invoker.call(CompilationLibrariesController_4.get.newVersionSingleLibrary(id))
      }
  
    // @LINE:199
    case controllers_CompilationLibrariesController_getSingleLibraryFilter124_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getSingleLibraryFilter124_invoker.call(CompilationLibrariesController_4.get.getSingleLibraryFilter())
      }
  
    // @LINE:200
    case controllers_CompilationLibrariesController_getSingleLibrary125_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getSingleLibrary125_invoker.call(CompilationLibrariesController_4.get.getSingleLibrary(id))
      }
  
    // @LINE:201
    case controllers_CompilationLibrariesController_getSingleLibraryAll126_route(params) =>
      call { 
        controllers_CompilationLibrariesController_getSingleLibraryAll126_invoker.call(CompilationLibrariesController_4.get.getSingleLibraryAll())
      }
  
    // @LINE:203
    case controllers_CompilationLibrariesController_updateSingleLibrary127_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_updateSingleLibrary127_invoker.call(CompilationLibrariesController_4.get.updateSingleLibrary(id))
      }
  
    // @LINE:204
    case controllers_CompilationLibrariesController_deleteSingleLibrary128_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_deleteSingleLibrary128_invoker.call(CompilationLibrariesController_4.get.deleteSingleLibrary(id))
      }
  
    // @LINE:205
    case controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion129_route(params) =>
      call(params.fromPath[String]("id", None), params.fromPath[Double]("version", None)) { (id, version) =>
        controllers_CompilationLibrariesController_uploadSingleLibraryWithVersion129_invoker.call(CompilationLibrariesController_4.get.uploadSingleLibraryWithVersion(id, version))
      }
  
    // @LINE:206
    case controllers_CompilationLibrariesController_getSingleLibraryDescription130_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_CompilationLibrariesController_getSingleLibraryDescription130_invoker.call(CompilationLibrariesController_4.get.getSingleLibraryDescription(id))
      }
  
    // @LINE:208
    case controllers_CompilationLibrariesController_generateProjectForEclipse131_route(params) =>
      call { 
        controllers_CompilationLibrariesController_generateProjectForEclipse131_invoker.call(CompilationLibrariesController_4.get.generateProjectForEclipse())
      }
  
    // @LINE:213
    case controllers_Assets_at132_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at132_invoker.call(Assets_5.at(path, file))
      }
  
    // @LINE:217
    case controllers_Assets_versioned133_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned133_invoker.call(Assets_5.versioned(path, file))
      }
  }
}
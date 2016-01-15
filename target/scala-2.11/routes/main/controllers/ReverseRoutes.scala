
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Fri Jan 15 18:09:49 CET 2016

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:6
package controllers {

  // @LINE:213
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:213
    def at(file:String): Call = {
      implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
      Call("GET", _prefix + { _defaultPrefix } + "public/" + implicitly[PathBindable[String]].unbind("file", file))
    }
  
    // @LINE:217
    def versioned(file:Asset): Call = {
      implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[Asset]].unbind("file", file))
    }
  
  }

  // @LINE:48
  class ReverseOverFlowController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:73
    def likePlus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likePlus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:53
    def getPostByFilter(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/postFilter")
    }
  
    // @LINE:66
    def updateComment(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:66
        case (id)  =>
          import ReverseRouteContext.empty
          Call("PUT", _prefix + { _defaultPrefix } + "overflow/comment/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:62
    def getTypeOfPost(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfPost")
    }
  
    // @LINE:57
    def commentsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/comments/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:74
    def likeMinus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likeMinus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:61
    def newTypeOfPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/typeOfPost")
    }
  
    // @LINE:77
    def removeHashTag(): Call = {
    
      () match {
      
        // @LINE:77
        case ()  =>
          import ReverseRouteContext.empty
          Call("PUT", _prefix + { _defaultPrefix } + "overflow/removeLink")
      
      }
    
    }
  
    // @LINE:75
    def linkWithPreviousAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/link")
    }
  
    // @LINE:54
    def getPostLinkedAnswers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/linkedAnswers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:52
    def getLatestPost(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/postAll")
    }
  
    // @LINE:51
    def editPost(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/post")
    }
  
    // @LINE:65
    def addComment(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/comment")
    }
  
    // @LINE:58
    def answereListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/answers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:81
    def removeConfirmType(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/confirm/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:49
    def getPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:69
    def addAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/answer")
    }
  
    // @LINE:50
    def deletePost(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:50
        case (id)  =>
          import ReverseRouteContext.empty
          Call("DELETE", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:76
    def unlinkWithPreviousAnswer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/link/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:48
    def newPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/post")
    }
  
    // @LINE:80
    def addConfirmType(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/confirm/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:56
    def hashTagsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/hashTags/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:78
    def addHashTag(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/hashTag")
    }
  
    // @LINE:59
    def textOfPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/textOfPost/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }

  // @LINE:148
  class ReverseCompilationLibrariesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:161
    def getBoardgeneralDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/generalDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:188
    def getLibraryGroupLibraries(libraryId:String, version:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/libraries/" + implicitly[PathBindable[String]].unbind("libraryId", dynamicString(libraryId)) + "/" + implicitly[PathBindable[String]].unbind("version", dynamicString(version)))
    }
  
    // @LINE:151
    def updateProcessor(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:152
    def deleteProcessor(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:157
    def newBoard(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/board")
    }
  
    // @LINE:191
    def uploudLibraryToLibraryGroup(libraryId:String, version:Double): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/upload/" + implicitly[PathBindable[String]].unbind("libraryId", dynamicString(libraryId)) + "/" + implicitly[PathBindable[Double]].unbind("version", version))
    }
  
    // @LINE:182
    def getLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:160
    def deleteBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/board/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:200
    def getSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:165
    def newProducers(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/producer")
    }
  
    // @LINE:190
    def getVersionLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/versions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:149
    def getProcessor(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:186
    def getLibraryGroupDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/generalDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:177
    def getTypeOfBoardDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:178
    def getTypeOfBoardAllBoards(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/TypeOfBoard/boards/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:197
    def newSingleLibrary(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library")
    }
  
    // @LINE:150
    def getProcessorAll(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:198
    def newVersionSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/version/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:153
    def getProcessorDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/processor/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:205
    def uploadSingleLibraryWithVersion(id:String, version:Double): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/uploud/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[Double]].unbind("version", version))
    }
  
    // @LINE:181
    def newLibraryGroup(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup")
    }
  
    // @LINE:194
    def fileRecord(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/fileRecord/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:176
    def getTypeOfBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:199
    def getSingleLibraryFilter(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/filter")
    }
  
    // @LINE:159
    def getBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:204
    def deleteSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/library/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:162
    def getUserDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/userDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:148
    def newProcessor(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:173
    def newTypeOfBoard(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/typeOfBoard")
    }
  
    // @LINE:184
    def getLibraryGroupAll(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup")
    }
  
    // @LINE:166
    def updateProducers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/producer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:189
    def createNewVersionLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/version/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:208
    def generateProjectForEclipse(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/project/eclipse")
    }
  
    // @LINE:170
    def getProducerTypeOfBoards(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/typeOfBoards/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:185
    def updateLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:158
    def editBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:183
    def deleteLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:206
    def getSingleLibraryDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:168
    def getProducer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:187
    def getLibraryGroupProcessors(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/processors/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:193
    def listOfFilesInVersion(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/listOfFiles/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:167
    def getProducers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer")
    }
  
    // @LINE:201
    def getSingleLibraryAll(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library")
    }
  
    // @LINE:174
    def updateTypeOfBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:203
    def updateSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/library/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:154
    def getProcessorLibraryGroups(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroups" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("id", id)))))
    }
  
    // @LINE:175
    def getTypeOfBoards(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard")
    }
  
    // @LINE:169
    def getProducerDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }

  // @LINE:9
  class ReverseSecurityController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:9
    def option(): Call = {
      import ReverseRouteContext.empty
      Call("OPTIONS", _prefix)
    }
  
    // @LINE:20
    def logout(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/permission/logout")
    }
  
    // @LINE:10
    def optionLink(url:String): Call = {
      import ReverseRouteContext.empty
      Call("OPTIONS", _prefix + { _defaultPrefix } + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:19
    def login(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/permission/login")
    }
  
  }

  // @LINE:37
  class ReversePermissionController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:39
    def createGroup(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "permission/group")
    }
  
    // @LINE:41
    def getAllPersonPermission(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "permission/personPermission")
    }
  
    // @LINE:42
    def removeAllPersonPermission(): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "permission/personPermission")
    }
  
    // @LINE:43
    def addAllPersonPermission(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "permission/personPermission")
    }
  
    // @LINE:37
    def getAllPermissions(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "permission/permisionKeys")
    }
  
    // @LINE:38
    def getAllGroups(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "permission/permisionGroups")
    }
  
  }

  // @LINE:6
  class ReverseApplication(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def test1(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test1")
    }
  
    // @LINE:6
    def index(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix)
    }
  
  }

  // @LINE:28
  class ReversePersonCreateController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:31
    def deletePerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:30
    def getPerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:29
    def updatePersonInformation(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
    // @LINE:28
    def createNewPerson(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
  }

  // @LINE:89
  class ReverseProgramingPackageController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:104
    def connectIoTWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/connectIoTWithProject")
    }
  
    // @LINE:137
    def allPrevVersions(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/allPrevVersions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:114
    def getProgramInJson(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:114
        case (id)  =>
          import ReverseRouteContext.empty
          Call("GET", _prefix + { _defaultPrefix } + "project/programInJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:139
    def getByFilter(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock/filter")
    }
  
    // @LINE:111
    def getProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:89
    def postNewProject(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:130
    def newVersionOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:92
    def getProjectsByUserAccount(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:105
    def unConnectIoTWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/unconnectIoTWithProject")
    }
  
    // @LINE:133
    def generalDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/generalDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:117
    def getProgramhomerList(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/homerList/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:93
    def deleteProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:135
    def getBlock(url:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:91
    def getProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:121
    def listOfUploadedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/listOfUploadedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:100
    def getAllHomers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/getallhoumers")
    }
  
    // @LINE:120
    def getAllPrograms(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/getallprograms/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:125
    def uploadProgramToHomer_AsSoonAsPossible(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/uploudtohomerAsSoonAsPossible")
    }
  
    // @LINE:112
    def editProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:129
    def newBlock(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock")
    }
  
    // @LINE:96
    def newHomer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/homer")
    }
  
    // @LINE:113
    def removeProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:115
    def getProgramPrograms(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/programs/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:99
    def getConnectedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/getAllConnectedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:90
    def updateProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:97
    def removeHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:98
    def getHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:106
    def connectHomerWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/connectHomerWithProject")
    }
  
    // @LINE:138
    def deleteBlock(url:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:116
    def getProgramelectronicDevicesList(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/electronicDevicesList/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:134
    def versionDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/versionDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:110
    def postNewProgram(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/program")
    }
  
    // @LINE:124
    def uploadProgramToHomer_Immediately(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/uploudtohomerImmediately")
    }
  
    // @LINE:132
    def designJson(url:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/designJson/" + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:126
    def uploadProgramToHomer_GivenTimeAsSoonAsPossible(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/uploudtohomerGivenTime")
    }
  
    // @LINE:107
    def unConnectHomerWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/unconnectHomerWithProject")
    }
  
    // @LINE:131
    def logicJson(url:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/logicJson/" + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:122
    def listOfHomersWaitingForUpload(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/listOfHomersWaitingForUpload/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }


}
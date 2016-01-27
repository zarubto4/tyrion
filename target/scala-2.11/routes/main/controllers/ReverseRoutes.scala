
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Wed Jan 27 15:12:26 CET 2016

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:8
package controllers {

  // @LINE:243
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:243
    def at(file:String): Call = {
      implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
    }
  
  }

  // @LINE:54
  class ReverseOverFlowController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:79
    def likePlus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likePlus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:59
    def getPostByFilter(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/postFilter")
    }
  
    // @LINE:72
    def updateComment(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:72
        case (id)  =>
          import ReverseRouteContext.empty
          Call("PUT", _prefix + { _defaultPrefix } + "overflow/comment/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:68
    def getTypeOfPost(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfPost")
    }
  
    // @LINE:63
    def commentsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/comments/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:80
    def likeMinus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likeMinus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:67
    def newTypeOfPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/typeOfPost")
    }
  
    // @LINE:83
    def removeHashTag(): Call = {
    
      () match {
      
        // @LINE:83
        case ()  =>
          import ReverseRouteContext.empty
          Call("PUT", _prefix + { _defaultPrefix } + "overflow/removeLink")
      
      }
    
    }
  
    // @LINE:81
    def linkWithPreviousAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/link")
    }
  
    // @LINE:60
    def getPostLinkedAnswers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/linkedAnswers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:58
    def getLatestPost(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/postAll")
    }
  
    // @LINE:57
    def editPost(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/post")
    }
  
    // @LINE:71
    def addComment(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/comment")
    }
  
    // @LINE:64
    def answereListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/answers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:87
    def removeConfirmType(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/confirm/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:55
    def getPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:75
    def addAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/answer")
    }
  
    // @LINE:56
    def deletePost(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:56
        case (id)  =>
          import ReverseRouteContext.empty
          Call("DELETE", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:82
    def unlinkWithPreviousAnswer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/link/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:54
    def newPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/post")
    }
  
    // @LINE:86
    def addConfirmType(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/confirm/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:62
    def hashTagsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/hashTags/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:84
    def addHashTag(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/hashTag")
    }
  
    // @LINE:65
    def textOfPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/textOfPost/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }

  // @LINE:159
  class ReverseCompilationLibrariesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:211
    def getLibraryGroupLibraries(libraryId:String, version:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/libraries/" + implicitly[PathBindable[String]].unbind("libraryId", dynamicString(libraryId)) + "/" + implicitly[PathBindable[String]].unbind("version", dynamicString(version)))
    }
  
    // @LINE:162
    def updateProcessor(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:163
    def deleteProcessor(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:176
    def newBoard(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/board")
    }
  
    // @LINE:214
    def uploudLibraryToLibraryGroup(libraryId:String, version:Double): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/upload/" + implicitly[PathBindable[String]].unbind("libraryId", dynamicString(libraryId)) + "/" + implicitly[PathBindable[Double]].unbind("version", version))
    }
  
    // @LINE:205
    def getLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:223
    def getSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:187
    def newProducers(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/producer")
    }
  
    // @LINE:179
    def deactivateBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/board/deactivateBoard" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:213
    def getVersionLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/versions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:160
    def getProcessor(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:209
    def getLibraryGroupDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/generalDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:199
    def getTypeOfBoardDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:200
    def getTypeOfBoardAllBoards(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/boards/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:220
    def newSingleLibrary(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library")
    }
  
    // @LINE:161
    def getProcessorAll(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:221
    def newVersionSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/version/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:170
    def getProcessorDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:228
    def uploadSingleLibraryWithVersion(id:String, version:Double): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/uploud/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[Double]].unbind("version", version))
    }
  
    // @LINE:177
    def addUserDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/userDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:204
    def newLibraryGroup(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup")
    }
  
    // @LINE:217
    def fileRecord(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/fileRecord/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:198
    def getTypeOfBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:222
    def getSingleLibraryFilter(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/filter")
    }
  
    // @LINE:178
    def getBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:227
    def deleteSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/library/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:180
    def getUserDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/userDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:159
    def newProcessor(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:172
    def getProcessorSingleLibraries(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/singleLibrary/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:195
    def newTypeOfBoard(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/typeOfBoard")
    }
  
    // @LINE:207
    def getLibraryGroupAll(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup")
    }
  
    // @LINE:188
    def updateProducers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/producer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:167
    def unconnectProcessorWithLibrary(id:String, lbrId:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/lbr/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("lbrId", dynamicString(lbrId)))
    }
  
    // @LINE:165
    def connectProcessorWithLibrary(id:String, lbrId:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/lbr/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("lbrId", dynamicString(lbrId)))
    }
  
    // @LINE:212
    def createNewVersionLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/version/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:231
    def generateProjectForEclipse(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/project/eclipse")
    }
  
    // @LINE:192
    def getProducerTypeOfBoards(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/typeOfBoards/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:208
    def updateLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:182
    def unconnectBoardWthProject(id:String, pr:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/unconnect/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("pr", dynamicString(pr)))
    }
  
    // @LINE:206
    def deleteLibraryGroup(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:166
    def connectProcessorWithLibraryGroup(id:String, lbrgId:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/lbrgrp/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("lbrgId", dynamicString(lbrgId)))
    }
  
    // @LINE:229
    def getSingleLibraryDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:183
    def getBoardProjects(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/projects/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:190
    def getProducer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:210
    def getLibraryGroupProcessors(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/processors/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:216
    def listOfFilesInVersion(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/listOfFiles/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:189
    def getProducers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer")
    }
  
    // @LINE:224
    def getSingleLibraryAll(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library")
    }
  
    // @LINE:181
    def connectBoardWthProject(id:String, pr:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/connect/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("pr", dynamicString(pr)))
    }
  
    // @LINE:168
    def unconnectProcessorWithLibraryGroup(id:String, lbrgId:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/lbrgrp/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("lbrgId", dynamicString(lbrgId)))
    }
  
    // @LINE:196
    def updateTypeOfBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:226
    def updateSingleLibrary(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/library/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:171
    def getProcessorLibraryGroups(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroups/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:197
    def getTypeOfBoards(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard")
    }
  
    // @LINE:191
    def getProducerDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/description/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }

  // @LINE:8
  class ReverseSecurityController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:20
    def Twitter(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/twitter")
    }
  
    // @LINE:19
    def Facebook(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/facebook")
    }
  
    // @LINE:22
    def Vkontakte(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/vkontakte")
    }
  
    // @LINE:21
    def GitHub(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/github")
    }
  
    // @LINE:240
    def optionLink(all:String): Call = {
      import ReverseRouteContext.empty
      Call("OPTIONS", _prefix + { _defaultPrefix } + implicitly[PathBindable[String]].unbind("all", all))
    }
  
    // @LINE:18
    def logout(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/permission/logout")
    }
  
    // @LINE:23
    def GEToauth_callback(code:String, state:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "oauth_callback/" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("code", code)), Some(implicitly[QueryStringBindable[String]].unbind("state", state)))))
    }
  
    // @LINE:8
    def index(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix)
    }
  
    // @LINE:17
    def login(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/permission/login")
    }
  
  }

  // @LINE:43
  class ReversePermissionController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:45
    def createGroup(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "permission/group")
    }
  
    // @LINE:47
    def getAllPersonPermission(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "permission/personPermission")
    }
  
    // @LINE:48
    def removeAllPersonPermission(): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "permission/personPermission")
    }
  
    // @LINE:49
    def addAllPersonPermission(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "permission/personPermission")
    }
  
    // @LINE:43
    def getAllPermissions(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "permission/permisionKeys")
    }
  
    // @LINE:44
    def getAllGroups(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "permission/permisionGroups")
    }
  
  }

  // @LINE:30
  class ReversePersonCreateController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:32
    def updatePersonInformation(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
    // @LINE:34
    def deletePerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:31
    def standartRegistration(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
    // @LINE:36
    def emailPersonAuthentitaction(mail:String, authToken:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "emailPersonAuthentication/" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("mail", mail)), Some(implicitly[QueryStringBindable[String]].unbind("authToken", authToken)))))
    }
  
    // @LINE:33
    def getPerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:30
    def developerRegistration(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/developer")
    }
  
  }

  // @LINE:95
  class ReverseProgramingPackageController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:148
    def allPrevVersions(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/allPrevVersions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:124
    def getProgramInJson(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:124
        case (id)  =>
          import ReverseRouteContext.empty
          Call("GET", _prefix + { _defaultPrefix } + "project/programInJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:150
    def getByFilter(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock/filter")
    }
  
    // @LINE:121
    def getProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:95
    def postNewProject(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:146
    def getBlockLast(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:138
    def newVersionOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:98
    def getProjectsByUserAccount(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:143
    def generalDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/generalDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:103
    def getProgramhomerList(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/homerList/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:140
    def designJsonVersion(id:String, version:Double): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/designJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[Double]].unbind("version", version))
    }
  
    // @LINE:131
    def getProjectsBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/boards/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:99
    def deleteProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:97
    def getProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:128
    def listOfUploadedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/listOfUploadedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:110
    def getAllHomers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer")
    }
  
    // @LINE:127
    def getAllPrograms(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/getallprograms/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:133
    def uploadProgramToHomer_AsSoonAsPossible(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/uploudtohomerAsSoonAsPossible")
    }
  
    // @LINE:122
    def editProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:104
    def getProjectOwners(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/owners/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:137
    def newBlock(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock")
    }
  
    // @LINE:107
    def newHomer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/homer")
    }
  
    // @LINE:123
    def removeProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:102
    def getProgramPrograms(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/programs/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:141
    def logicJsonLast(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/logicJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:111
    def getConnectedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer/getAllConnectedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:96
    def updateProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:108
    def removeHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:139
    def logicJsonVersion(id:String, version:Double): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/logicJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[Double]].unbind("version", version))
    }
  
    // @LINE:145
    def getBlockVersion(id:String, version:Double): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[Double]].unbind("version", version))
    }
  
    // @LINE:109
    def getHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:142
    def designJsonLast(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/designJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:116
    def connectHomerWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/connectHomerWithProject")
    }
  
    // @LINE:149
    def deleteBlock(url:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:101
    def unshareProjectWithUsers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/unshareProject/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:144
    def versionDescription(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/versionDescription/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:120
    def postNewProgram(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/program")
    }
  
    // @LINE:132
    def uploadProgramToHomer_Immediately(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/uploudtohomerImmediately")
    }
  
    // @LINE:100
    def shareProjectWithUsers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/shareProject/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:134
    def uploadProgramToHomer_GivenTimeAsSoonAsPossible(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/uploudtohomerGivenTime")
    }
  
    // @LINE:117
    def unConnectHomerWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/unconnectHomerWithProject")
    }
  
    // @LINE:129
    def listOfHomersWaitingForUpload(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/listOfHomersWaitingForUpload/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }


}
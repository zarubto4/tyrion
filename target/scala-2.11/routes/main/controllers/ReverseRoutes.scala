
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Tue Feb 23 18:14:01 CET 2016

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:8
package controllers {

  // @LINE:68
  class ReversePersonController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:73
    def deletePerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:75
    def email_Person_authentitaction(mail:String, authToken:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "emailPersonAuthentication/" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("mail", mail)), Some(implicitly[QueryStringBindable[String]].unbind("authToken", authToken)))))
    }
  
    // @LINE:70
    def edit_Person_Information(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
    // @LINE:71
    def getPerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:68
    def developerRegistration(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/developer")
    }
  
    // @LINE:69
    def registred_Person(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
  }

  // @LINE:329
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:329
    def at(file:String): Call = {
      implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
    }
  
  }

  // @LINE:87
  class ReverseOverFlowController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:103
    def getTypeOfConfirms(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm")
    }
  
    // @LINE:114
    def likePlus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likePlus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:91
    def getPostByFilter(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/postFilter")
    }
  
    // @LINE:107
    def updateComment(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:107
        case (id)  =>
          import ReverseRouteContext.empty
          Call("PUT", _prefix + { _defaultPrefix } + "overflow/comment/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:100
    def getTypeOfPost(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfPost")
    }
  
    // @LINE:95
    def commentsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/comments/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:115
    def likeMinus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likeMinus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:99
    def newTypeOfPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/typeOfPost")
    }
  
    // @LINE:102
    def newTypeOfConfirms(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm")
    }
  
    // @LINE:118
    def removeHashTag(): Call = {
    
      () match {
      
        // @LINE:118
        case ()  =>
          import ReverseRouteContext.empty
          Call("PUT", _prefix + { _defaultPrefix } + "overflow/removeLink")
      
      }
    
    }
  
    // @LINE:116
    def linkWithPreviousAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/link")
    }
  
    // @LINE:92
    def getPostLinkedAnswers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/linkedAnswers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:106
    def addComment(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/comment")
    }
  
    // @LINE:96
    def answereListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/answers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:88
    def getPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:110
    def addAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/answer")
    }
  
    // @LINE:89
    def deletePost(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:89
        case (id)  =>
          import ReverseRouteContext.empty
          Call("DELETE", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:117
    def unlinkWithPreviousAnswer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/link/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:87
    def newPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/post")
    }
  
    // @LINE:104
    def putTypeOfConfirmToPost(conf:String, pst:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm/" + implicitly[PathBindable[String]].unbind("conf", dynamicString(conf)) + "/" + implicitly[PathBindable[String]].unbind("pst", dynamicString(pst)))
    }
  
    // @LINE:94
    def hashTagsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/hashTags/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:119
    def addHashTag(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/hashTag")
    }
  
    // @LINE:90
    def editPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:97
    def textOfPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/textOfPost/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }

  // @LINE:198
  class ReverseCompilationLibrariesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:218
    def update_Processor(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)))))
    }
  
    // @LINE:279
    def upload_SingleLibrary_Version(library_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/upload" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)), Some(implicitly[QueryStringBindable[String]].unbind("version_id", version_id)))))
    }
  
    // @LINE:261
    def get_LibraryGroup_Filter(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/filter")
    }
  
    // @LINE:249
    def new_TypeOfBoard(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/typeOfBoard")
    }
  
    // @LINE:216
    def get_Processor(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)))))
    }
  
    // @LINE:263
    def get_LibraryGroup_Description(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/generalDescription" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("libraryGroup_id", libraryGroup_id)))))
    }
  
    // @LINE:217
    def get_Processor_All(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:223
    def disconnectProcessorWithLibrary(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/library" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)), Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:266
    def new_LibraryGroup_Version(version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/version" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("version_id", version_id)))))
    }
  
    // @LINE:212
    def get_Boards_from_Project(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/project/board/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:215
    def new_Processor(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:252
    def get_TypeOfBoard_all(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/all")
    }
  
    // @LINE:255
    def getTypeOfBoardAllBoards(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/boards" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("type_of_board_id", type_of_board_id)))))
    }
  
    // @LINE:205
    def delete_C_Program(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/c_program/c_program" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("c_program_id", c_program_id)))))
    }
  
    // @LINE:245
    def get_Producer_Description(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/description" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("producer_id", producer_id)))))
    }
  
    // @LINE:206
    def delete_C_Program_Version(c_program_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/c_program/version" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("c_program_id", c_program_id)), Some(implicitly[QueryStringBindable[String]].unbind("version_id", version_id)))))
    }
  
    // @LINE:210
    def uploadBinaryFileToBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/c_program/binary/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:233
    def get_Board(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:264
    def get_LibraryGroup_Processors(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/processors" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("libraryGroup_id", libraryGroup_id)))))
    }
  
    // @LINE:283
    def fileRecord(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "file/fileRecord/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:275
    def get_SingleLibrary(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:259
    def get_LibraryGroup(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("libraryGroup_id", libraryGroup_id)))))
    }
  
    // @LINE:258
    def new_LibraryGroup(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup")
    }
  
    // @LINE:274
    def get_SingleLibrary_Filter(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/filter")
    }
  
    // @LINE:198
    def create_C_Program(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/c_program" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("project_id", project_id)))))
    }
  
    // @LINE:278
    def delete_SingleLibrary(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/library" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:254
    def get_TypeOfBoard_Description(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/description" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("type_of_board_id", type_of_board_id)))))
    }
  
    // @LINE:277
    def edit_SingleLibrary(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/library" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:227
    def getProcessorSingleLibraries(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/singleLibrary" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)))))
    }
  
    // @LINE:262
    def editLibraryGroup(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/libraryGroup" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("libraryGroup_id", libraryGroup_id)))))
    }
  
    // @LINE:268
    def upload_Library_To_LibraryGroup(libraryGroup_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/upload" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("libraryGroup_id", libraryGroup_id)), Some(implicitly[QueryStringBindable[String]].unbind("version_id", version_id)))))
    }
  
    // @LINE:273
    def get_SingleLibrary_Versions(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/versions" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:271
    def new_SingleLibrary(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library")
    }
  
    // @LINE:260
    def delete_LibraryGroup(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/libraryGroup" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("libraryGroup_id", libraryGroup_id)))))
    }
  
    // @LINE:224
    def disconnectProcessorWithLibraryGroup(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroup" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)), Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:230
    def new_Board(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/board")
    }
  
    // @LINE:209
    def uploadCompilationToBoard(id:String, board:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/c_program/upload/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("board", dynamicString(board)))
    }
  
    // @LINE:221
    def connectProcessorWithLibrary(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/library" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)), Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:202
    def edit_C_Program_Description(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/c_program/edit" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("c_program_id", c_program_id)))))
    }
  
    // @LINE:208
    def generateProjectForEclipse(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/c_program/eclipse")
    }
  
    // @LINE:251
    def delete_TypeOfBoard(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/typeOfBoard" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("type_of_board_id", type_of_board_id)))))
    }
  
    // @LINE:246
    def get_Producer_TypeOfBoards(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/typeOfBoards" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("producer_id", producer_id)))))
    }
  
    // @LINE:236
    def disconnect_Board_from_Project(id:String, pr:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/disconnect/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("pr", dynamicString(pr)))
    }
  
    // @LINE:241
    def new_Producer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/producer")
    }
  
    // @LINE:231
    def edit_Board_User_Description(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/userDescription" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("type_of_board_id", type_of_board_id)))))
    }
  
    // @LINE:253
    def get_TypeOfBoard(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("type_of_board_id", type_of_board_id)))))
    }
  
    // @LINE:219
    def delete_Processor(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)))))
    }
  
    // @LINE:267
    def get_LibraryGroup_Version(version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/versions" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("version_id", version_id)))))
    }
  
    // @LINE:265
    def get_LibraryGroup_Libraries(libraryGroup_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/libraries" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("libraryGroup_id", libraryGroup_id)), Some(implicitly[QueryStringBindable[String]].unbind("version_id", version_id)))))
    }
  
    // @LINE:199
    def get_C_Program(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/c_program" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("c_program_id", c_program_id)))))
    }
  
    // @LINE:222
    def connectProcessorWithLibraryGroup(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroup" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)), Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:232
    def get_Board_Filter(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/filter")
    }
  
    // @LINE:237
    def getBoardProjects(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/projects/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:242
    def edit_Producer(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/producer" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("producer_id", producer_id)))))
    }
  
    // @LINE:235
    def connect_Board_with_Project(id:String, pr:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/connect/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("pr", dynamicString(pr)))
    }
  
    // @LINE:200
    def get_C_Program_All_from_Project(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/c_program/project" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("project_id", project_id)))))
    }
  
    // @LINE:244
    def get_Producer(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("producer_id", producer_id)))))
    }
  
    // @LINE:234
    def deactivate_Board(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/board/deactivateBoard" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:226
    def getProcessorLibraryGroups(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroups" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("processor_id", processor_id)))))
    }
  
    // @LINE:282
    def get_LibraryGroup_Version_Libraries(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "file/listOfFiles/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:203
    def update_C_Program(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/c_program/update" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("c_program_id", c_program_id)))))
    }
  
    // @LINE:272
    def new_SingleLibrary_Version(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/version" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("library_id", library_id)))))
    }
  
    // @LINE:243
    def get_Producers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/all")
    }
  
    // @LINE:250
    def edit_TypeOfBoard(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/typeOfBoard" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("type_of_board_id", type_of_board_id)))))
    }
  
  }

  // @LINE:8
  class ReverseSecurityController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:36
    def Vkontakte(returnLink:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/vkontakte" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("returnLink", returnLink)))))
    }
  
    // @LINE:33
    def Facebook(returnLink:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/facebook" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("returnLink", returnLink)))))
    }
  
    // @LINE:40
    def GET_facebook_oauth(url:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/facebook/" + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:41
    def GET_github_oauth(url:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/github/" + implicitly[PathBindable[String]].unbind("url", url))
    }
  
    // @LINE:326
    def optionLink(all:String): Call = {
      import ReverseRouteContext.empty
      Call("OPTIONS", _prefix + { _defaultPrefix } + implicitly[PathBindable[String]].unbind("all", all))
    }
  
    // @LINE:34
    def Twitter(returnLink:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/twitter" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("returnLink", returnLink)))))
    }
  
    // @LINE:31
    def logout(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/permission/logout")
    }
  
    // @LINE:35
    def GitHub(returnLink:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/github" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("returnLink", returnLink)))))
    }
  
    // @LINE:38
    def getPersonByToken(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "login/person")
    }
  
    // @LINE:8
    def index(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix)
    }
  
    // @LINE:30
    def login(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/permission/login")
    }
  
  }

  // @LINE:45
  class ReversePermissionController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:58
    def get_Role_All(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/all")
    }
  
    // @LINE:46
    def remove_Permission_Person(person_id:String, permission_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/permission/person/remove" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)))))
    }
  
    // @LINE:54
    def delete_Role(role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "secure/role" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:45
    def add_Permission_Person(person_id:String, permission_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/permission/person/add" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)))))
    }
  
    // @LINE:53
    def new_Role(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "secure/role")
    }
  
    // @LINE:56
    def add_Role_Person(person_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/role/person/add" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:51
    def remove_Permission_from_Role(permission_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/permission/remove" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:49
    def add_Permission_to_Role(permission_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/permission/add" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:47
    def get_Permission_All(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/permission")
    }
  
    // @LINE:50
    def get_Permission_in_Group(role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/permission" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:57
    def remove_Role_Person(person_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/role/person/remove" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:60
    def get_System_Acces(person_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/person/system_acces" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)))))
    }
  
  }

  // @LINE:290
  class ReverseGridController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:303
    def get_M_Program_byQR_Token_forMobile(qr_token:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_program/token" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("qr_token", qr_token)))))
    }
  
    // @LINE:309
    def get_Screen_Size_Type_PublicList(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/screen_type/all")
    }
  
    // @LINE:299
    def new_M_Program(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "grid/m_program")
    }
  
    // @LINE:296
    def get_M_Projects_ByLoggedPerson(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_project/person")
    }
  
    // @LINE:314
    def remove_Screen_Size_Type(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "grid/screen_type/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:292
    def edit_M_Project(m_project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "grid/m_project/" + implicitly[PathBindable[String]].unbind("m_project_id", dynamicString(m_project_id)))
    }
  
    // @LINE:301
    def edit_M_Program(m_progrm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "grid/m_program" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("m_progrm_id", m_progrm_id)))))
    }
  
    // @LINE:293
    def remove_M_Project(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "grid/m_project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:313
    def edit_Screen_Size_Type(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "grid/screen_type/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:300
    def get_M_Program(m_progrm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_program" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("m_progrm_id", m_progrm_id)))))
    }
  
    // @LINE:290
    def new_M_Project(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "grid/m_project" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("project_id", project_id)))))
    }
  
    // @LINE:311
    def get_Screen_Size_Type(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/screen_type/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:310
    def get_Screen_Size_Type_Combination(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/screen_type/all/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:291
    def get_M_Project(m_project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_project/" + implicitly[PathBindable[String]].unbind("m_project_id", dynamicString(m_project_id)))
    }
  
    // @LINE:302
    def remove_M_Program(m_progrm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "grid/m_program" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("m_progrm_id", m_progrm_id)))))
    }
  
    // @LINE:295
    def get_M_Projects_from_GlobalProject(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_project/project" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("project_id", project_id)))))
    }
  
    // @LINE:307
    def new_Screen_Size_Type(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "grid/screen_type")
    }
  
  }

  // @LINE:11
  class ReverseWikyController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:11
    def test1(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test1")
    }
  
    // @LINE:13
    def test3(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test3")
    }
  
    // @LINE:12
    def test2(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test2")
    }
  
    // @LINE:15
    def test5(projectId:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test5" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("projectId", projectId)))))
    }
  
    // @LINE:14
    def test4(projectId:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test4" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("projectId", projectId)))))
    }
  
    // @LINE:16
    def test6(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test6")
    }
  
  }

  // @LINE:129
  class ReverseProgramingPackageController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:174
    def getBlockBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:178
    def allPrevVersions(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/allPrevVersions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:158
    def getProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/b_program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:129
    def postNewProject(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:179
    def deleteBlockVersion(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/blockoBlock/version/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:136
    def getAll_b_Programs(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/b_programs/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:161
    def remove_b_Program(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/b_program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:132
    def getProjectsByUserAccount(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:185
    def getAllTypeOfBlocks(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/typeOfBlock")
    }
  
    // @LINE:140
    def getProgramhomerList(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/homerList/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:168
    def getProjectsBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/boards/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:160
    def update_b_program(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/update/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:133
    def deleteProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:131
    def getProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:186
    def deleteTypeOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/typeOfBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:166
    def listOfUploadedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/listOfUploadedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:147
    def getAllHomers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer")
    }
  
    // @LINE:159
    def editProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:164
    def uploadProgramToCloud(id:String, ver:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/uploadToCloud/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("ver", dynamicString(ver)))
    }
  
    // @LINE:184
    def editTypeOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/typeOfBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:175
    def getBlockVersions(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/versions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:141
    def getProjectOwners(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/owners/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:171
    def newBlock(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock")
    }
  
    // @LINE:182
    def newTypeOfBlock(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/typeOfBlock")
    }
  
    // @LINE:144
    def newHomer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/homer")
    }
  
    // @LINE:153
    def disconnectHomerWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/disconnectHomerWithProject")
    }
  
    // @LINE:148
    def getConnectedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer/getAllConnectedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:173
    def editBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:130
    def updateProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:145
    def removeHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:162
    def getProgramInString(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/b_programInJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:146
    def getHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:137
    def getAll_c_Programs(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/c_programs/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:172
    def updateOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:152
    def connectHomerWithProject(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/connectHomerWithProject")
    }
  
    // @LINE:157
    def postNewBProgram(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/b_program")
    }
  
    // @LINE:163
    def uploadProgramToHomer_Immediately(id:String, ver:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/upload/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("ver", dynamicString(ver)))
    }
  
    // @LINE:180
    def deleteBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/blockoBlock/block/id" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("id", id)))))
    }
  
    // @LINE:183
    def getByCategory(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/typeOfBlock/filter")
    }
  
    // @LINE:135
    def unshareProjectWithUsers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/unshareProject/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:134
    def shareProjectWithUsers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/shareProject/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:138
    def getAll_m_Projects(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/m_projects/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:167
    def listOfHomersWaitingForUpload(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/listOfHomersWaitingForUpload/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }

  // @LINE:21
  class ReverseWebSocketController_Incoming(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:24
    def getWebSocketStats(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "websocket/webSocketStats")
    }
  
    // @LINE:25
    def sendTo(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "websocket/sendTo/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:22
    def mobile_connection(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "websocket/mobile/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:21
    def homer_connection(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "websocket/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }


}
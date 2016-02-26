
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Fri Feb 26 14:33:18 CET 2016

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:8
package controllers {

  // @LINE:52
  class ReversePersonController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:62
    def deletePerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:59
    def valid_Person_mail(mail:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "coreClient/person/valid/mail/" + implicitly[PathBindable[String]].unbind("mail", dynamicString(mail)))
    }
  
    // @LINE:64
    def email_Person_authentitaction(mail:String, authToken:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "emailPersonAuthentication/" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("mail", mail)), Some(implicitly[QueryStringBindable[String]].unbind("authToken", authToken)))))
    }
  
    // @LINE:54
    def edit_Person_Information(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
    // @LINE:58
    def valid_Person_NickName(nickname:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "coreClient/person/valid/nickname/" + implicitly[PathBindable[String]].unbind("nickname", dynamicString(nickname)))
    }
  
    // @LINE:55
    def getPerson(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "coreClient/person/person/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:52
    def developerRegistration(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/developer")
    }
  
    // @LINE:53
    def registred_Person(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "coreClient/person/person")
    }
  
  }

  // @LINE:343
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:343
    def at(file:String): Call = {
      implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
    }
  
  }

  // @LINE:90
  class ReverseOverFlowController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:126
    def likePlus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likePlus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:94
    def getPostByFilter(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/postFilter")
    }
  
    // @LINE:132
    def add_HashTag_to_Post(post_id:String, hashTag:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/hashTag/" + implicitly[PathBindable[String]].unbind("post_id", dynamicString(post_id)) + "/" + implicitly[PathBindable[String]].unbind("hashTag", dynamicString(hashTag)))
    }
  
    // @LINE:119
    def updateComment(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:119
        case (id)  =>
          import ReverseRouteContext.empty
          Call("PUT", _prefix + { _defaultPrefix } + "overflow/comment/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:111
    def get_TypeOfConfirms(type_of_confirm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm/" + implicitly[PathBindable[String]].unbind("type_of_confirm_id", dynamicString(type_of_confirm_id)))
    }
  
    // @LINE:98
    def commentsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/comments/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:133
    def remove_HashTag_from_Post(post_id:String, hashTag:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/hashTag/" + implicitly[PathBindable[String]].unbind("post_id", dynamicString(post_id)) + "/" + implicitly[PathBindable[String]].unbind("hashTag", dynamicString(hashTag)))
    }
  
    // @LINE:108
    def new_TypeOfConfirms(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm")
    }
  
    // @LINE:127
    def likeMinus(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/likeMinus/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:103
    def get_TypeOfPost_all(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfPost/all")
    }
  
    // @LINE:102
    def new_TypeOfPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/typeOfPost")
    }
  
    // @LINE:128
    def linkWithPreviousAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/link")
    }
  
    // @LINE:95
    def getPostLinkedAnswers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/linkedAnswers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:110
    def get_TypeOfConfirms_all(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm/all")
    }
  
    // @LINE:118
    def addComment(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/comment")
    }
  
    // @LINE:99
    def answereListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/answers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:91
    def getPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:122
    def addAnswer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/answer")
    }
  
    // @LINE:92
    def deletePost(id:String): Call = {
    
      (id: @unchecked) match {
      
        // @LINE:92
        case (id)  =>
          import ReverseRouteContext.empty
          Call("DELETE", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
      
      }
    
    }
  
    // @LINE:129
    def unlinkWithPreviousAnswer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/link/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:90
    def newPost(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "overflow/post")
    }
  
    // @LINE:105
    def edit_TypeOfPost(type_of_post_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/typeOfPost/" + implicitly[PathBindable[String]].unbind("type_of_post_id", dynamicString(type_of_post_id)))
    }
  
    // @LINE:106
    def delete_TypeOfPost(type_of_post_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/typeOfPost/" + implicitly[PathBindable[String]].unbind("type_of_post_id", dynamicString(type_of_post_id)))
    }
  
    // @LINE:104
    def get_TypeOfPost(type_of_post_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/typeOfPost/" + implicitly[PathBindable[String]].unbind("type_of_post_id", dynamicString(type_of_post_id)))
    }
  
    // @LINE:109
    def edit_TypeOfConfirms(type_of_confirm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm/" + implicitly[PathBindable[String]].unbind("type_of_confirm_id", dynamicString(type_of_confirm_id)))
    }
  
    // @LINE:97
    def hashTagsListOnPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/hashTags/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:114
    def set_TypeOfConfirm_to_Post(post_id:String, type_of_confirm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm/" + implicitly[PathBindable[String]].unbind("post_id", dynamicString(post_id)) + "/" + implicitly[PathBindable[String]].unbind("type_of_confirm_id", dynamicString(type_of_confirm_id)))
    }
  
    // @LINE:115
    def remove_TypeOfConfirm_to_Post(post_id:String, type_of_confirm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm/" + implicitly[PathBindable[String]].unbind("post_id", dynamicString(post_id)) + "/" + implicitly[PathBindable[String]].unbind("type_of_confirm_id", dynamicString(type_of_confirm_id)))
    }
  
    // @LINE:93
    def editPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "overflow/post/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:100
    def textOfPost(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "overflow/post/textOfPost/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:112
    def delete_TypeOfConfirms(type_of_confirm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "overflow/typeOfConfirm/" + implicitly[PathBindable[String]].unbind("type_of_confirm_id", dynamicString(type_of_confirm_id)))
    }
  
  }

  // @LINE:211
  class ReverseCompilationLibrariesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:231
    def update_Processor(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)))
    }
  
    // @LINE:293
    def upload_SingleLibrary_Version(library_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/upload" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)) + "/" + implicitly[PathBindable[String]].unbind("version_id", dynamicString(version_id)))
    }
  
    // @LINE:275
    def get_LibraryGroup_Filter(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/filter")
    }
  
    // @LINE:263
    def new_TypeOfBoard(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/typeOfBoard")
    }
  
    // @LINE:229
    def get_Processor(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)))
    }
  
    // @LINE:277
    def get_LibraryGroup_Description(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/generalDescription/" + implicitly[PathBindable[String]].unbind("libraryGroup_id", dynamicString(libraryGroup_id)))
    }
  
    // @LINE:230
    def get_Processor_All(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:236
    def disconnectProcessorWithLibrary(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/library/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)) + "/" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:280
    def new_LibraryGroup_Version(version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/version/" + implicitly[PathBindable[String]].unbind("version_id", dynamicString(version_id)))
    }
  
    // @LINE:225
    def get_Boards_from_Project(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/project/board/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)))
    }
  
    // @LINE:228
    def new_Processor(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/processor")
    }
  
    // @LINE:266
    def get_TypeOfBoard_all(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/all")
    }
  
    // @LINE:269
    def getTypeOfBoardAllBoards(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/boards/" + implicitly[PathBindable[String]].unbind("type_of_board_id", dynamicString(type_of_board_id)))
    }
  
    // @LINE:218
    def delete_C_Program(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/c_program/c_program/" + implicitly[PathBindable[String]].unbind("c_program_id", dynamicString(c_program_id)))
    }
  
    // @LINE:259
    def get_Producer_Description(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/description/" + implicitly[PathBindable[String]].unbind("producer_id", dynamicString(producer_id)))
    }
  
    // @LINE:219
    def delete_C_Program_Version(c_program_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/c_program/version/" + implicitly[PathBindable[String]].unbind("c_program_id", dynamicString(c_program_id)) + "/" + implicitly[PathBindable[String]].unbind("version_id", dynamicString(version_id)))
    }
  
    // @LINE:223
    def uploadBinaryFileToBoard(board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/c_program/binary/" + implicitly[PathBindable[String]].unbind("board_id", dynamicString(board_id)))
    }
  
    // @LINE:246
    def get_Board(board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/" + implicitly[PathBindable[String]].unbind("board_id", dynamicString(board_id)))
    }
  
    // @LINE:278
    def get_LibraryGroup_Processors(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/processors/" + implicitly[PathBindable[String]].unbind("libraryGroup_id", dynamicString(libraryGroup_id)))
    }
  
    // @LINE:297
    def fileRecord(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "file/fileRecord/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:289
    def get_SingleLibrary(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:258
    def delete_Producer(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/producer/" + implicitly[PathBindable[String]].unbind("producer_id", dynamicString(producer_id)))
    }
  
    // @LINE:273
    def get_LibraryGroup(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("libraryGroup_id", dynamicString(libraryGroup_id)))
    }
  
    // @LINE:272
    def new_LibraryGroup(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup")
    }
  
    // @LINE:288
    def get_SingleLibrary_Filter(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/filter")
    }
  
    // @LINE:211
    def create_C_Program(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/c_program/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)))
    }
  
    // @LINE:292
    def delete_SingleLibrary(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/library" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:268
    def get_TypeOfBoard_Description(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/description/" + implicitly[PathBindable[String]].unbind("type_of_board_id", dynamicString(type_of_board_id)))
    }
  
    // @LINE:291
    def edit_SingleLibrary(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/library" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:240
    def getProcessorSingleLibraries(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/singleLibrary/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)))
    }
  
    // @LINE:276
    def editLibraryGroup(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("libraryGroup_id", dynamicString(libraryGroup_id)))
    }
  
    // @LINE:282
    def upload_Library_To_LibraryGroup(libraryGroup_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/libraryGroup/upload/" + implicitly[PathBindable[String]].unbind("libraryGroup_id", dynamicString(libraryGroup_id)) + "/" + implicitly[PathBindable[String]].unbind("version_id", dynamicString(version_id)))
    }
  
    // @LINE:287
    def get_SingleLibrary_Versions(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/library/versions/" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:285
    def new_SingleLibrary(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library")
    }
  
    // @LINE:274
    def delete_LibraryGroup(libraryGroup_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/libraryGroup/" + implicitly[PathBindable[String]].unbind("libraryGroup_id", dynamicString(libraryGroup_id)))
    }
  
    // @LINE:237
    def disconnectProcessorWithLibraryGroup(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroup/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)) + "/" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:243
    def new_Board(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/board")
    }
  
    // @LINE:222
    def uploadCompilationToBoard(c_program_id:String, board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/c_program/upload/" + implicitly[PathBindable[String]].unbind("c_program_id", dynamicString(c_program_id)) + "/" + implicitly[PathBindable[String]].unbind("board_id", dynamicString(board_id)))
    }
  
    // @LINE:234
    def connectProcessorWithLibrary(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/library/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)) + "/" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:215
    def edit_C_Program_Description(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/c_program/edit/" + implicitly[PathBindable[String]].unbind("c_program_id", dynamicString(c_program_id)))
    }
  
    // @LINE:221
    def generateProjectForEclipse(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/c_program/eclipse")
    }
  
    // @LINE:265
    def delete_TypeOfBoard(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/" + implicitly[PathBindable[String]].unbind("type_of_board_id", dynamicString(type_of_board_id)))
    }
  
    // @LINE:260
    def get_Producer_TypeOfBoards(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/typeOfBoards/" + implicitly[PathBindable[String]].unbind("producer_id", dynamicString(producer_id)))
    }
  
    // @LINE:249
    def disconnect_Board_from_Project(board_id:String, project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/disconnect/" + implicitly[PathBindable[String]].unbind("board_id", dynamicString(board_id)) + "/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)))
    }
  
    // @LINE:254
    def new_Producer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/producer")
    }
  
    // @LINE:244
    def edit_Board_User_Description(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/userDescription/" + implicitly[PathBindable[String]].unbind("type_of_board_id", dynamicString(type_of_board_id)))
    }
  
    // @LINE:267
    def get_TypeOfBoard(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/" + implicitly[PathBindable[String]].unbind("type_of_board_id", dynamicString(type_of_board_id)))
    }
  
    // @LINE:232
    def delete_Processor(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/processor/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)))
    }
  
    // @LINE:281
    def get_LibraryGroup_Version(version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/versions/" + implicitly[PathBindable[String]].unbind("version_id", dynamicString(version_id)))
    }
  
    // @LINE:279
    def get_LibraryGroup_Libraries(libraryGroup_id:String, version_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/libraryGroup/libraries/" + implicitly[PathBindable[String]].unbind("libraryGroup_id", dynamicString(libraryGroup_id)) + "/" + implicitly[PathBindable[String]].unbind("version_id", dynamicString(version_id)))
    }
  
    // @LINE:212
    def get_C_Program(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/c_program/" + implicitly[PathBindable[String]].unbind("c_program_id", dynamicString(c_program_id)))
    }
  
    // @LINE:235
    def connectProcessorWithLibraryGroup(processor_id:String, library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroup/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)) + "/" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:245
    def get_Board_Filter(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/filter")
    }
  
    // @LINE:250
    def getBoardProjects(board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/board/projects/" + implicitly[PathBindable[String]].unbind("board_id", dynamicString(board_id)))
    }
  
    // @LINE:255
    def edit_Producer(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/producer/" + implicitly[PathBindable[String]].unbind("producer_id", dynamicString(producer_id)))
    }
  
    // @LINE:248
    def connect_Board_with_Project(board_id:String, project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/board/connect/" + implicitly[PathBindable[String]].unbind("board_id", dynamicString(board_id)) + "/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)))
    }
  
    // @LINE:213
    def get_C_Program_All_from_Project(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/c_program/project/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)))
    }
  
    // @LINE:257
    def get_Producer(producer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/" + implicitly[PathBindable[String]].unbind("producer_id", dynamicString(producer_id)))
    }
  
    // @LINE:247
    def deactivate_Board(board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "compilation/board/deactivateBoard/" + implicitly[PathBindable[String]].unbind("board_id", dynamicString(board_id)))
    }
  
    // @LINE:239
    def getProcessorLibraryGroups(processor_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/processor/libraryGroups/" + implicitly[PathBindable[String]].unbind("processor_id", dynamicString(processor_id)))
    }
  
    // @LINE:296
    def get_LibraryGroup_Version_Libraries(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "file/listOfFiles/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:216
    def update_C_Program(c_program_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/c_program/update/" + implicitly[PathBindable[String]].unbind("c_program_id", dynamicString(c_program_id)))
    }
  
    // @LINE:286
    def new_SingleLibrary_Version(library_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "compilation/library/version/" + implicitly[PathBindable[String]].unbind("library_id", dynamicString(library_id)))
    }
  
    // @LINE:256
    def get_Producers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "compilation/producer/all")
    }
  
    // @LINE:264
    def edit_TypeOfBoard(type_of_board_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "compilation/typeOfBoard/" + implicitly[PathBindable[String]].unbind("type_of_board_id", dynamicString(type_of_board_id)))
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
  
    // @LINE:340
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

  // @LINE:70
  class ReversePermissionController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:83
    def get_Role_All(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/all")
    }
  
    // @LINE:71
    def remove_Permission_Person(person_id:String, permission_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/permission/person/remove" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)))))
    }
  
    // @LINE:79
    def delete_Role(role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "secure/role" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:70
    def add_Permission_Person(person_id:String, permission_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/permission/person/add" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)))))
    }
  
    // @LINE:78
    def new_Role(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "secure/role")
    }
  
    // @LINE:81
    def add_Role_Person(person_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/role/person/add" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:76
    def remove_Permission_from_Role(permission_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/permission/remove" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:74
    def add_Permission_to_Role(permission_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/permission/add" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("permission_id", permission_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:72
    def get_Permission_All(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/permission")
    }
  
    // @LINE:75
    def get_Permission_in_Group(role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/role/permission" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:82
    def remove_Role_Person(person_id:String, role_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "secure/role/person/remove" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)), Some(implicitly[QueryStringBindable[String]].unbind("role_id", role_id)))))
    }
  
    // @LINE:85
    def get_System_Acces(person_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "secure/person/system_acces" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("person_id", person_id)))))
    }
  
  }

  // @LINE:304
  class ReverseGridController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:315
    def get_M_Program_byQR_Token_forMobile(qr_token:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_program/app/token/" + implicitly[PathBindable[String]].unbind("qr_token", dynamicString(qr_token)))
    }
  
    // @LINE:314
    def new_M_Program(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "grid/m_program")
    }
  
    // @LINE:305
    def get_M_Projects_ByLoggedPerson(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_project/person")
    }
  
    // @LINE:316
    def get_M_Program_all_forMobile(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_program/app/m_programs")
    }
  
    // @LINE:328
    def remove_Screen_Size_Type(screen_size_type_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "grid/screen_type/" + implicitly[PathBindable[String]].unbind("screen_size_type_id", dynamicString(screen_size_type_id)))
    }
  
    // @LINE:307
    def edit_M_Project(m_project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "grid/m_project/" + implicitly[PathBindable[String]].unbind("m_project_id", dynamicString(m_project_id)))
    }
  
    // @LINE:319
    def edit_M_Program(m_progrm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "grid/m_program/" + implicitly[PathBindable[String]].unbind("m_progrm_id", dynamicString(m_progrm_id)))
    }
  
    // @LINE:308
    def remove_M_Project(m_project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "grid/m_project/" + implicitly[PathBindable[String]].unbind("m_project_id", dynamicString(m_project_id)))
    }
  
    // @LINE:327
    def edit_Screen_Size_Type(screen_size_type_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "grid/screen_type/" + implicitly[PathBindable[String]].unbind("screen_size_type_id", dynamicString(screen_size_type_id)))
    }
  
    // @LINE:318
    def get_M_Program(m_progrm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_program/" + implicitly[PathBindable[String]].unbind("m_progrm_id", dynamicString(m_progrm_id)))
    }
  
    // @LINE:325
    def get_Screen_Size_Type_Combination(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/screen_type/all")
    }
  
    // @LINE:304
    def new_M_Project(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "grid/m_project" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("project_id", project_id)))))
    }
  
    // @LINE:326
    def get_Screen_Size_Type(screen_size_type_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/screen_type/" + implicitly[PathBindable[String]].unbind("screen_size_type_id", dynamicString(screen_size_type_id)))
    }
  
    // @LINE:306
    def get_M_Project(m_project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_project/" + implicitly[PathBindable[String]].unbind("m_project_id", dynamicString(m_project_id)))
    }
  
    // @LINE:320
    def remove_M_Program(m_progrm_id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "grid/m_program/" + implicitly[PathBindable[String]].unbind("m_progrm_id", dynamicString(m_progrm_id)))
    }
  
    // @LINE:310
    def get_M_Projects_from_GlobalProject(project_id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "grid/m_project/project/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)))
    }
  
    // @LINE:324
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

  // @LINE:142
  class ReverseProgramingPackageController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:187
    def getBlockBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:191
    def allPrevVersions(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/allPrevVersions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:171
    def getProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/b_program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:142
    def postNewProject(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:192
    def deleteBlockVersion(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/blockoBlock/version/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:149
    def getAll_b_Programs(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/b_programs/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:174
    def remove_b_Program(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/b_program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:145
    def getProjectsByUserAccount(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project")
    }
  
    // @LINE:198
    def getAllTypeOfBlocks(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/typeOfBlock")
    }
  
    // @LINE:153
    def getProgramhomerList(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/homerList/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:181
    def getProjectsBoard(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/boards/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:173
    def update_b_program(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/update/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:146
    def deleteProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:144
    def getProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:199
    def deleteTypeOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/typeOfBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:179
    def listOfUploadedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/listOfUploadedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:160
    def getAllHomers(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer")
    }
  
    // @LINE:172
    def editProgram(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:177
    def uploadProgramToCloud(id:String, ver:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/uploadToCloud/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("ver", dynamicString(ver)))
    }
  
    // @LINE:197
    def editTypeOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/typeOfBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:188
    def getBlockVersions(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/blockoBlock/versions/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:154
    def getProjectOwners(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/owners/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:184
    def newBlock(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock")
    }
  
    // @LINE:195
    def newTypeOfBlock(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/typeOfBlock")
    }
  
    // @LINE:157
    def newHomer(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/homer")
    }
  
    // @LINE:161
    def getConnectedHomers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer/getAllConnectedHomers/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:186
    def editBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:143
    def updateProject(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:158
    def removeHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:175
    def getProgramInString(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/b_programInJson/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:159
    def getHomer(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/homer/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:150
    def getAll_c_Programs(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/c_programs/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:185
    def updateOfBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/blockoBlock/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:170
    def postNewBProgram(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "project/b_program")
    }
  
    // @LINE:176
    def uploadProgramToHomer_Immediately(id:String, ver:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/b_program/upload/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)) + "/" + implicitly[PathBindable[String]].unbind("ver", dynamicString(ver)))
    }
  
    // @LINE:193
    def deleteBlock(id:String): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "project/blockoBlock/block/id" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("id", id)))))
    }
  
    // @LINE:196
    def getByCategory(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/typeOfBlock/filter")
    }
  
    // @LINE:148
    def unshareProjectWithUsers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/unshareProject/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:147
    def shareProjectWithUsers(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/project/shareProject/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:165
    def connectHomerWithProject(project_id:String, homer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/connect/homer/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)) + "/" + implicitly[PathBindable[String]].unbind("homer_id", dynamicString(homer_id)))
    }
  
    // @LINE:166
    def disconnectHomerWithProject(project_id:String, homer_id:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "project/disconnect/homer/" + implicitly[PathBindable[String]].unbind("project_id", dynamicString(project_id)) + "/" + implicitly[PathBindable[String]].unbind("homer_id", dynamicString(homer_id)))
    }
  
    // @LINE:151
    def getAll_m_Projects(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "project/project/m_projects/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:180
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

// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Tue Feb 23 18:14:01 CET 2016

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:8
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:68
  class ReversePersonController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:73
    def deletePerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.deletePerson",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:75
    def email_Person_authentitaction: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.email_Person_authentitaction",
      """
        function(mail,authToken) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "emailPersonAuthentication/" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("mail", mail), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("authToken", authToken)])})
        }
      """
    )
  
    // @LINE:70
    def edit_Person_Information: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.edit_Person_Information",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
    // @LINE:71
    def getPerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.getPerson",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:68
    def developerRegistration: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.developerRegistration",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/developer"})
        }
      """
    )
  
    // @LINE:69
    def registred_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.registred_Person",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
  }

  // @LINE:329
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:329
    def at: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.at",
      """
        function(file) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
        }
      """
    )
  
  }

  // @LINE:87
  class ReverseOverFlowController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:103
    def getTypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getTypeOfConfirms",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm"})
        }
      """
    )
  
    // @LINE:114
    def likePlus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likePlus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likePlus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:91
    def getPostByFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostByFilter",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/postFilter"})
        }
      """
    )
  
    // @LINE:107
    def updateComment: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.updateComment",
      """
        function(id) {
        
          if (true) {
            return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/comment/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
          }
        
        }
      """
    )
  
    // @LINE:100
    def getTypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getTypeOfPost",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost"})
        }
      """
    )
  
    // @LINE:95
    def commentsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.commentsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/comments/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:115
    def likeMinus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likeMinus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likeMinus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:99
    def newTypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newTypeOfPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost"})
        }
      """
    )
  
    // @LINE:102
    def newTypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newTypeOfConfirms",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm"})
        }
      """
    )
  
    // @LINE:118
    def removeHashTag: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.removeHashTag",
      """
        function() {
        
          if (true) {
            return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/removeLink"})
          }
        
        }
      """
    )
  
    // @LINE:116
    def linkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.linkWithPreviousAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link"})
        }
      """
    )
  
    // @LINE:92
    def getPostLinkedAnswers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostLinkedAnswers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/linkedAnswers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:106
    def addComment: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addComment",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/comment"})
        }
      """
    )
  
    // @LINE:96
    def answereListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.answereListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/answers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:88
    def getPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:110
    def addAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/answer"})
        }
      """
    )
  
    // @LINE:89
    def deletePost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.deletePost",
      """
        function(id) {
        
          if (true) {
            return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
          }
        
        }
      """
    )
  
    // @LINE:117
    def unlinkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.unlinkWithPreviousAnswer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:87
    def newPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post"})
        }
      """
    )
  
    // @LINE:104
    def putTypeOfConfirmToPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.putTypeOfConfirmToPost",
      """
        function(conf,pst) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("conf", encodeURIComponent(conf)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pst", encodeURIComponent(pst))})
        }
      """
    )
  
    // @LINE:94
    def hashTagsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.hashTagsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/hashTags/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:119
    def addHashTag: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addHashTag",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/hashTag"})
        }
      """
    )
  
    // @LINE:90
    def editPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.editPost",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:97
    def textOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.textOfPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/textOfPost/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }

  // @LINE:198
  class ReverseCompilationLibrariesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:218
    def update_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.update_Processor",
      """
        function(processor_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id)])})
        }
      """
    )
  
    // @LINE:279
    def upload_SingleLibrary_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.upload_SingleLibrary_Version",
      """
        function(library_id,version_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/upload" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("version_id", version_id)])})
        }
      """
    )
  
    // @LINE:261
    def get_LibraryGroup_Filter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Filter",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/filter"})
        }
      """
    )
  
    // @LINE:249
    def new_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_TypeOfBoard",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard"})
        }
      """
    )
  
    // @LINE:216
    def get_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Processor",
      """
        function(processor_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id)])})
        }
      """
    )
  
    // @LINE:263
    def get_LibraryGroup_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Description",
      """
        function(libraryGroup_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/generalDescription" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("libraryGroup_id", libraryGroup_id)])})
        }
      """
    )
  
    // @LINE:217
    def get_Processor_All: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Processor_All",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:223
    def disconnectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.disconnectProcessorWithLibrary",
      """
        function(processor_id,library_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/library" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:266
    def new_LibraryGroup_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_LibraryGroup_Version",
      """
        function(version_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/version" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("version_id", version_id)])})
        }
      """
    )
  
    // @LINE:212
    def get_Boards_from_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Boards_from_Project",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/project/board/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:215
    def new_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_Processor",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:252
    def get_TypeOfBoard_all: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_TypeOfBoard_all",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/all"})
        }
      """
    )
  
    // @LINE:255
    def getTypeOfBoardAllBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoardAllBoards",
      """
        function(type_of_board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/boards" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("type_of_board_id", type_of_board_id)])})
        }
      """
    )
  
    // @LINE:205
    def delete_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_C_Program",
      """
        function(c_program_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/c_program" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("c_program_id", c_program_id)])})
        }
      """
    )
  
    // @LINE:245
    def get_Producer_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producer_Description",
      """
        function(producer_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/description" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("producer_id", producer_id)])})
        }
      """
    )
  
    // @LINE:206
    def delete_C_Program_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_C_Program_Version",
      """
        function(c_program_id,version_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/version" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("c_program_id", c_program_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("version_id", version_id)])})
        }
      """
    )
  
    // @LINE:210
    def uploadBinaryFileToBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploadBinaryFileToBoard",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/binary/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:233
    def get_Board: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Board",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:264
    def get_LibraryGroup_Processors: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Processors",
      """
        function(libraryGroup_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/processors" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("libraryGroup_id", libraryGroup_id)])})
        }
      """
    )
  
    // @LINE:283
    def fileRecord: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.fileRecord",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "file/fileRecord/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:275
    def get_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_SingleLibrary",
      """
        function(library_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:259
    def get_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup",
      """
        function(libraryGroup_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("libraryGroup_id", libraryGroup_id)])})
        }
      """
    )
  
    // @LINE:258
    def new_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_LibraryGroup",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup"})
        }
      """
    )
  
    // @LINE:274
    def get_SingleLibrary_Filter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_SingleLibrary_Filter",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/filter"})
        }
      """
    )
  
    // @LINE:198
    def create_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.create_C_Program",
      """
        function(project_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("project_id", project_id)])})
        }
      """
    )
  
    // @LINE:278
    def delete_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_SingleLibrary",
      """
        function(library_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:254
    def get_TypeOfBoard_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_TypeOfBoard_Description",
      """
        function(type_of_board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/description" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("type_of_board_id", type_of_board_id)])})
        }
      """
    )
  
    // @LINE:277
    def edit_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_SingleLibrary",
      """
        function(library_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:227
    def getProcessorSingleLibraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorSingleLibraries",
      """
        function(processor_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/singleLibrary" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id)])})
        }
      """
    )
  
    // @LINE:262
    def editLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.editLibraryGroup",
      """
        function(libraryGroup_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("libraryGroup_id", libraryGroup_id)])})
        }
      """
    )
  
    // @LINE:268
    def upload_Library_To_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.upload_Library_To_LibraryGroup",
      """
        function(libraryGroup_id,version_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/upload" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("libraryGroup_id", libraryGroup_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("version_id", version_id)])})
        }
      """
    )
  
    // @LINE:273
    def get_SingleLibrary_Versions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_SingleLibrary_Versions",
      """
        function(library_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/versions" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:271
    def new_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_SingleLibrary",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library"})
        }
      """
    )
  
    // @LINE:260
    def delete_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_LibraryGroup",
      """
        function(libraryGroup_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("libraryGroup_id", libraryGroup_id)])})
        }
      """
    )
  
    // @LINE:224
    def disconnectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.disconnectProcessorWithLibraryGroup",
      """
        function(processor_id,library_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroup" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:230
    def new_Board: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_Board",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board"})
        }
      """
    )
  
    // @LINE:209
    def uploadCompilationToBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploadCompilationToBoard",
      """
        function(id,board) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/upload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board", encodeURIComponent(board))})
        }
      """
    )
  
    // @LINE:221
    def connectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibrary",
      """
        function(processor_id,library_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/library" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:202
    def edit_C_Program_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_C_Program_Description",
      """
        function(c_program_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/edit" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("c_program_id", c_program_id)])})
        }
      """
    )
  
    // @LINE:208
    def generateProjectForEclipse: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.generateProjectForEclipse",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/eclipse"})
        }
      """
    )
  
    // @LINE:251
    def delete_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_TypeOfBoard",
      """
        function(type_of_board_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("type_of_board_id", type_of_board_id)])})
        }
      """
    )
  
    // @LINE:246
    def get_Producer_TypeOfBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producer_TypeOfBoards",
      """
        function(producer_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/typeOfBoards" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("producer_id", producer_id)])})
        }
      """
    )
  
    // @LINE:236
    def disconnect_Board_from_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.disconnect_Board_from_Project",
      """
        function(id,pr) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/disconnect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pr", encodeURIComponent(pr))})
        }
      """
    )
  
    // @LINE:241
    def new_Producer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_Producer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer"})
        }
      """
    )
  
    // @LINE:231
    def edit_Board_User_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_Board_User_Description",
      """
        function(type_of_board_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/userDescription" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("type_of_board_id", type_of_board_id)])})
        }
      """
    )
  
    // @LINE:253
    def get_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_TypeOfBoard",
      """
        function(type_of_board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("type_of_board_id", type_of_board_id)])})
        }
      """
    )
  
    // @LINE:219
    def delete_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_Processor",
      """
        function(processor_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id)])})
        }
      """
    )
  
    // @LINE:267
    def get_LibraryGroup_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Version",
      """
        function(version_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/versions" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("version_id", version_id)])})
        }
      """
    )
  
    // @LINE:265
    def get_LibraryGroup_Libraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Libraries",
      """
        function(libraryGroup_id,version_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/libraries" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("libraryGroup_id", libraryGroup_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("version_id", version_id)])})
        }
      """
    )
  
    // @LINE:199
    def get_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_C_Program",
      """
        function(c_program_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("c_program_id", c_program_id)])})
        }
      """
    )
  
    // @LINE:222
    def connectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibraryGroup",
      """
        function(processor_id,library_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroup" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:232
    def get_Board_Filter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Board_Filter",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/filter"})
        }
      """
    )
  
    // @LINE:237
    def getBoardProjects: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getBoardProjects",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/projects/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:242
    def edit_Producer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_Producer",
      """
        function(producer_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("producer_id", producer_id)])})
        }
      """
    )
  
    // @LINE:235
    def connect_Board_with_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connect_Board_with_Project",
      """
        function(id,pr) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/connect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pr", encodeURIComponent(pr))})
        }
      """
    )
  
    // @LINE:200
    def get_C_Program_All_from_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_C_Program_All_from_Project",
      """
        function(project_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/project" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("project_id", project_id)])})
        }
      """
    )
  
    // @LINE:244
    def get_Producer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producer",
      """
        function(producer_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("producer_id", producer_id)])})
        }
      """
    )
  
    // @LINE:234
    def deactivate_Board: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deactivate_Board",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/deactivateBoard" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:226
    def getProcessorLibraryGroups: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorLibraryGroups",
      """
        function(processor_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroups" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("processor_id", processor_id)])})
        }
      """
    )
  
    // @LINE:282
    def get_LibraryGroup_Version_Libraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Version_Libraries",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "file/listOfFiles/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:203
    def update_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.update_C_Program",
      """
        function(c_program_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/update" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("c_program_id", c_program_id)])})
        }
      """
    )
  
    // @LINE:272
    def new_SingleLibrary_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_SingleLibrary_Version",
      """
        function(library_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/version" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("library_id", library_id)])})
        }
      """
    )
  
    // @LINE:243
    def get_Producers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/all"})
        }
      """
    )
  
    // @LINE:250
    def edit_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_TypeOfBoard",
      """
        function(type_of_board_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("type_of_board_id", type_of_board_id)])})
        }
      """
    )
  
  }

  // @LINE:8
  class ReverseSecurityController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:36
    def Vkontakte: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.Vkontakte",
      """
        function(returnLink) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/vkontakte" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("returnLink", returnLink)])})
        }
      """
    )
  
    // @LINE:33
    def Facebook: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.Facebook",
      """
        function(returnLink) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/facebook" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("returnLink", returnLink)])})
        }
      """
    )
  
    // @LINE:40
    def GET_facebook_oauth: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.GET_facebook_oauth",
      """
        function(url) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/facebook/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:41
    def GET_github_oauth: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.GET_github_oauth",
      """
        function(url) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/github/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:326
    def optionLink: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.optionLink",
      """
        function(all) {
          return _wA({method:"OPTIONS", url:"""" + _prefix + { _defaultPrefix } + """" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("all", all)})
        }
      """
    )
  
    // @LINE:34
    def Twitter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.Twitter",
      """
        function(returnLink) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/twitter" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("returnLink", returnLink)])})
        }
      """
    )
  
    // @LINE:31
    def logout: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.logout",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/permission/logout"})
        }
      """
    )
  
    // @LINE:35
    def GitHub: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.GitHub",
      """
        function(returnLink) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/github" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("returnLink", returnLink)])})
        }
      """
    )
  
    // @LINE:38
    def getPersonByToken: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.getPersonByToken",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/person"})
        }
      """
    )
  
    // @LINE:8
    def index: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.index",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + """"})
        }
      """
    )
  
    // @LINE:30
    def login: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.login",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/permission/login"})
        }
      """
    )
  
  }

  // @LINE:45
  class ReversePermissionController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:58
    def get_Role_All: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_Role_All",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/all"})
        }
      """
    )
  
    // @LINE:46
    def remove_Permission_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.remove_Permission_Person",
      """
        function(person_id,permission_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/permission/person/remove" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id)])})
        }
      """
    )
  
    // @LINE:54
    def delete_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.delete_Role",
      """
        function(role_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:45
    def add_Permission_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.add_Permission_Person",
      """
        function(person_id,permission_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/permission/person/add" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id)])})
        }
      """
    )
  
    // @LINE:53
    def new_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.new_Role",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role"})
        }
      """
    )
  
    // @LINE:56
    def add_Role_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.add_Role_Person",
      """
        function(person_id,role_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/person/add" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:51
    def remove_Permission_from_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.remove_Permission_from_Role",
      """
        function(permission_id,role_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/permission/remove" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:49
    def add_Permission_to_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.add_Permission_to_Role",
      """
        function(permission_id,role_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/permission/add" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:47
    def get_Permission_All: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_Permission_All",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/permission"})
        }
      """
    )
  
    // @LINE:50
    def get_Permission_in_Group: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_Permission_in_Group",
      """
        function(role_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/permission" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:57
    def remove_Role_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.remove_Role_Person",
      """
        function(person_id,role_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/person/remove" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:60
    def get_System_Acces: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_System_Acces",
      """
        function(person_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/person/system_acces" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id)])})
        }
      """
    )
  
  }

  // @LINE:290
  class ReverseGridController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:303
    def get_M_Program_byQR_Token_forMobile: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Program_byQR_Token_forMobile",
      """
        function(qr_token) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program/token" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("qr_token", qr_token)])})
        }
      """
    )
  
    // @LINE:309
    def get_Screen_Size_Type_PublicList: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_Screen_Size_Type_PublicList",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/all"})
        }
      """
    )
  
    // @LINE:299
    def new_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.new_M_Program",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program"})
        }
      """
    )
  
    // @LINE:296
    def get_M_Projects_ByLoggedPerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Projects_ByLoggedPerson",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/person"})
        }
      """
    )
  
    // @LINE:314
    def remove_Screen_Size_Type: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.remove_Screen_Size_Type",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:292
    def edit_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.edit_M_Project",
      """
        function(m_project_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_project_id", encodeURIComponent(m_project_id))})
        }
      """
    )
  
    // @LINE:301
    def edit_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.edit_M_Program",
      """
        function(m_progrm_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("m_progrm_id", m_progrm_id)])})
        }
      """
    )
  
    // @LINE:293
    def remove_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.remove_M_Project",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:313
    def edit_Screen_Size_Type: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.edit_Screen_Size_Type",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:300
    def get_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Program",
      """
        function(m_progrm_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("m_progrm_id", m_progrm_id)])})
        }
      """
    )
  
    // @LINE:290
    def new_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.new_M_Project",
      """
        function(project_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("project_id", project_id)])})
        }
      """
    )
  
    // @LINE:311
    def get_Screen_Size_Type: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_Screen_Size_Type",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:310
    def get_Screen_Size_Type_Combination: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_Screen_Size_Type_Combination",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/all/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:291
    def get_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Project",
      """
        function(m_project_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_project_id", encodeURIComponent(m_project_id))})
        }
      """
    )
  
    // @LINE:302
    def remove_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.remove_M_Program",
      """
        function(m_progrm_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("m_progrm_id", m_progrm_id)])})
        }
      """
    )
  
    // @LINE:295
    def get_M_Projects_from_GlobalProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Projects_from_GlobalProject",
      """
        function(project_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/project" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("project_id", project_id)])})
        }
      """
    )
  
    // @LINE:307
    def new_Screen_Size_Type: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.new_Screen_Size_Type",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type"})
        }
      """
    )
  
  }

  // @LINE:11
  class ReverseWikyController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:11
    def test1: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test1",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test1"})
        }
      """
    )
  
    // @LINE:13
    def test3: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test3",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test3"})
        }
      """
    )
  
    // @LINE:12
    def test2: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test2",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test2"})
        }
      """
    )
  
    // @LINE:15
    def test5: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test5",
      """
        function(projectId) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test5" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("projectId", projectId)])})
        }
      """
    )
  
    // @LINE:14
    def test4: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test4",
      """
        function(projectId) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test4" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("projectId", projectId)])})
        }
      """
    )
  
    // @LINE:16
    def test6: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test6",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test6"})
        }
      """
    )
  
  }

  // @LINE:129
  class ReverseProgramingPackageController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:174
    def getBlockBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getBlockBlock",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:178
    def allPrevVersions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.allPrevVersions",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/allPrevVersions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:158
    def getProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgram",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:129
    def postNewProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewProject",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:179
    def deleteBlockVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteBlockVersion",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:136
    def getAll_b_Programs: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAll_b_Programs",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/b_programs/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:161
    def remove_b_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.remove_b_Program",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:132
    def getProjectsByUserAccount: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsByUserAccount",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:185
    def getAllTypeOfBlocks: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllTypeOfBlocks",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock"})
        }
      """
    )
  
    // @LINE:140
    def getProgramhomerList: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramhomerList",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/homerList/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:168
    def getProjectsBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/boards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:160
    def update_b_program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.update_b_program",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/update/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:133
    def deleteProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteProject",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:131
    def getProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProject",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:186
    def deleteTypeOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteTypeOfBlock",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:166
    def listOfUploadedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.listOfUploadedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/listOfUploadedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:147
    def getAllHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllHomers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:159
    def editProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editProgram",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:164
    def uploadProgramToCloud: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToCloud",
      """
        function(id,ver) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/uploadToCloud/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("ver", encodeURIComponent(ver))})
        }
      """
    )
  
    // @LINE:184
    def editTypeOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editTypeOfBlock",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:175
    def getBlockVersions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getBlockVersions",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/versions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:141
    def getProjectOwners: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectOwners",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/owners/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:171
    def newBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newBlock",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock"})
        }
      """
    )
  
    // @LINE:182
    def newTypeOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newTypeOfBlock",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock"})
        }
      """
    )
  
    // @LINE:144
    def newHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newHomer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:153
    def disconnectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.disconnectHomerWithProject",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/disconnectHomerWithProject"})
        }
      """
    )
  
    // @LINE:148
    def getConnectedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getConnectedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/getAllConnectedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:173
    def editBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editBlock",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:130
    def updateProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.updateProject",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:145
    def removeHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.removeHomer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:162
    def getProgramInString: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramInString",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_programInJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:146
    def getHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getHomer",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:137
    def getAll_c_Programs: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAll_c_Programs",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/c_programs/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:172
    def updateOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.updateOfBlock",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:152
    def connectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.connectHomerWithProject",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/connectHomerWithProject"})
        }
      """
    )
  
    // @LINE:157
    def postNewBProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewBProgram",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program"})
        }
      """
    )
  
    // @LINE:163
    def uploadProgramToHomer_Immediately: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToHomer_Immediately",
      """
        function(id,ver) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/upload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("ver", encodeURIComponent(ver))})
        }
      """
    )
  
    // @LINE:180
    def deleteBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteBlock",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/block/id" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("id", id)])})
        }
      """
    )
  
    // @LINE:183
    def getByCategory: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getByCategory",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock/filter"})
        }
      """
    )
  
    // @LINE:135
    def unshareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.unshareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/unshareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:134
    def shareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.shareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/shareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:138
    def getAll_m_Projects: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAll_m_Projects",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/m_projects/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:167
    def listOfHomersWaitingForUpload: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.listOfHomersWaitingForUpload",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/listOfHomersWaitingForUpload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }

  // @LINE:21
  class ReverseWebSocketController_Incoming(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:24
    def getWebSocketStats: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WebSocketController_Incoming.getWebSocketStats",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "websocket/webSocketStats"})
        }
      """
    )
  
    // @LINE:25
    def sendTo: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WebSocketController_Incoming.sendTo",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "websocket/sendTo/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:22
    def mobile_connection: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WebSocketController_Incoming.mobile_connection",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "websocket/mobile/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:21
    def homer_connection: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WebSocketController_Incoming.homer_connection",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "websocket/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }


}

// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Fri Feb 26 14:33:18 CET 2016

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:8
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:52
  class ReversePersonController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:62
    def deletePerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.deletePerson",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:59
    def valid_Person_mail: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.valid_Person_mail",
      """
        function(mail) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/valid/mail/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("mail", encodeURIComponent(mail))})
        }
      """
    )
  
    // @LINE:64
    def email_Person_authentitaction: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.email_Person_authentitaction",
      """
        function(mail,authToken) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "emailPersonAuthentication/" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("mail", mail), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("authToken", authToken)])})
        }
      """
    )
  
    // @LINE:54
    def edit_Person_Information: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.edit_Person_Information",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
    // @LINE:58
    def valid_Person_NickName: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.valid_Person_NickName",
      """
        function(nickname) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/valid/nickname/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("nickname", encodeURIComponent(nickname))})
        }
      """
    )
  
    // @LINE:55
    def getPerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.getPerson",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:52
    def developerRegistration: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.developerRegistration",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/developer"})
        }
      """
    )
  
    // @LINE:53
    def registred_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonController.registred_Person",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
  }

  // @LINE:343
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:343
    def at: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.at",
      """
        function(file) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
        }
      """
    )
  
  }

  // @LINE:90
  class ReverseOverFlowController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:126
    def likePlus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likePlus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likePlus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:94
    def getPostByFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostByFilter",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/postFilter"})
        }
      """
    )
  
    // @LINE:132
    def add_HashTag_to_Post: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.add_HashTag_to_Post",
      """
        function(post_id,hashTag) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/hashTag/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("post_id", encodeURIComponent(post_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("hashTag", encodeURIComponent(hashTag))})
        }
      """
    )
  
    // @LINE:119
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
  
    // @LINE:111
    def get_TypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.get_TypeOfConfirms",
      """
        function(type_of_confirm_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_confirm_id", encodeURIComponent(type_of_confirm_id))})
        }
      """
    )
  
    // @LINE:98
    def commentsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.commentsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/comments/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:133
    def remove_HashTag_from_Post: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.remove_HashTag_from_Post",
      """
        function(post_id,hashTag) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/hashTag/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("post_id", encodeURIComponent(post_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("hashTag", encodeURIComponent(hashTag))})
        }
      """
    )
  
    // @LINE:108
    def new_TypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.new_TypeOfConfirms",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm"})
        }
      """
    )
  
    // @LINE:127
    def likeMinus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likeMinus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likeMinus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:103
    def get_TypeOfPost_all: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.get_TypeOfPost_all",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost/all"})
        }
      """
    )
  
    // @LINE:102
    def new_TypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.new_TypeOfPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost"})
        }
      """
    )
  
    // @LINE:128
    def linkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.linkWithPreviousAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link"})
        }
      """
    )
  
    // @LINE:95
    def getPostLinkedAnswers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostLinkedAnswers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/linkedAnswers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:110
    def get_TypeOfConfirms_all: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.get_TypeOfConfirms_all",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/all"})
        }
      """
    )
  
    // @LINE:118
    def addComment: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addComment",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/comment"})
        }
      """
    )
  
    // @LINE:99
    def answereListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.answereListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/answers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:91
    def getPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:122
    def addAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/answer"})
        }
      """
    )
  
    // @LINE:92
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
  
    // @LINE:129
    def unlinkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.unlinkWithPreviousAnswer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:90
    def newPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post"})
        }
      """
    )
  
    // @LINE:105
    def edit_TypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.edit_TypeOfPost",
      """
        function(type_of_post_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_post_id", encodeURIComponent(type_of_post_id))})
        }
      """
    )
  
    // @LINE:106
    def delete_TypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.delete_TypeOfPost",
      """
        function(type_of_post_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_post_id", encodeURIComponent(type_of_post_id))})
        }
      """
    )
  
    // @LINE:104
    def get_TypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.get_TypeOfPost",
      """
        function(type_of_post_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_post_id", encodeURIComponent(type_of_post_id))})
        }
      """
    )
  
    // @LINE:109
    def edit_TypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.edit_TypeOfConfirms",
      """
        function(type_of_confirm_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_confirm_id", encodeURIComponent(type_of_confirm_id))})
        }
      """
    )
  
    // @LINE:97
    def hashTagsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.hashTagsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/hashTags/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:114
    def set_TypeOfConfirm_to_Post: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.set_TypeOfConfirm_to_Post",
      """
        function(post_id,type_of_confirm_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("post_id", encodeURIComponent(post_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_confirm_id", encodeURIComponent(type_of_confirm_id))})
        }
      """
    )
  
    // @LINE:115
    def remove_TypeOfConfirm_to_Post: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.remove_TypeOfConfirm_to_Post",
      """
        function(post_id,type_of_confirm_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("post_id", encodeURIComponent(post_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_confirm_id", encodeURIComponent(type_of_confirm_id))})
        }
      """
    )
  
    // @LINE:93
    def editPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.editPost",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:100
    def textOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.textOfPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/textOfPost/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:112
    def delete_TypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.delete_TypeOfConfirms",
      """
        function(type_of_confirm_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_confirm_id", encodeURIComponent(type_of_confirm_id))})
        }
      """
    )
  
  }

  // @LINE:211
  class ReverseCompilationLibrariesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:231
    def update_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.update_Processor",
      """
        function(processor_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id))})
        }
      """
    )
  
    // @LINE:293
    def upload_SingleLibrary_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.upload_SingleLibrary_Version",
      """
        function(library_id,version_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/upload" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version_id", encodeURIComponent(version_id))})
        }
      """
    )
  
    // @LINE:275
    def get_LibraryGroup_Filter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Filter",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/filter"})
        }
      """
    )
  
    // @LINE:263
    def new_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_TypeOfBoard",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard"})
        }
      """
    )
  
    // @LINE:229
    def get_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Processor",
      """
        function(processor_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id))})
        }
      """
    )
  
    // @LINE:277
    def get_LibraryGroup_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Description",
      """
        function(libraryGroup_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/generalDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryGroup_id", encodeURIComponent(libraryGroup_id))})
        }
      """
    )
  
    // @LINE:230
    def get_Processor_All: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Processor_All",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:236
    def disconnectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.disconnectProcessorWithLibrary",
      """
        function(processor_id,library_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:280
    def new_LibraryGroup_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_LibraryGroup_Version",
      """
        function(version_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version_id", encodeURIComponent(version_id))})
        }
      """
    )
  
    // @LINE:225
    def get_Boards_from_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Boards_from_Project",
      """
        function(project_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/project/board/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id))})
        }
      """
    )
  
    // @LINE:228
    def new_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_Processor",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:266
    def get_TypeOfBoard_all: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_TypeOfBoard_all",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/all"})
        }
      """
    )
  
    // @LINE:269
    def getTypeOfBoardAllBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoardAllBoards",
      """
        function(type_of_board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/boards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_board_id", encodeURIComponent(type_of_board_id))})
        }
      """
    )
  
    // @LINE:218
    def delete_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_C_Program",
      """
        function(c_program_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/c_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("c_program_id", encodeURIComponent(c_program_id))})
        }
      """
    )
  
    // @LINE:259
    def get_Producer_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producer_Description",
      """
        function(producer_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("producer_id", encodeURIComponent(producer_id))})
        }
      """
    )
  
    // @LINE:219
    def delete_C_Program_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_C_Program_Version",
      """
        function(c_program_id,version_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("c_program_id", encodeURIComponent(c_program_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version_id", encodeURIComponent(version_id))})
        }
      """
    )
  
    // @LINE:223
    def uploadBinaryFileToBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploadBinaryFileToBoard",
      """
        function(board_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/binary/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board_id", encodeURIComponent(board_id))})
        }
      """
    )
  
    // @LINE:246
    def get_Board: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Board",
      """
        function(board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board_id", encodeURIComponent(board_id))})
        }
      """
    )
  
    // @LINE:278
    def get_LibraryGroup_Processors: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Processors",
      """
        function(libraryGroup_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/processors/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryGroup_id", encodeURIComponent(libraryGroup_id))})
        }
      """
    )
  
    // @LINE:297
    def fileRecord: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.fileRecord",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "file/fileRecord/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:289
    def get_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_SingleLibrary",
      """
        function(library_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:258
    def delete_Producer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_Producer",
      """
        function(producer_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("producer_id", encodeURIComponent(producer_id))})
        }
      """
    )
  
    // @LINE:273
    def get_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup",
      """
        function(libraryGroup_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryGroup_id", encodeURIComponent(libraryGroup_id))})
        }
      """
    )
  
    // @LINE:272
    def new_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_LibraryGroup",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup"})
        }
      """
    )
  
    // @LINE:288
    def get_SingleLibrary_Filter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_SingleLibrary_Filter",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/filter"})
        }
      """
    )
  
    // @LINE:211
    def create_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.create_C_Program",
      """
        function(project_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id))})
        }
      """
    )
  
    // @LINE:292
    def delete_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_SingleLibrary",
      """
        function(library_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:268
    def get_TypeOfBoard_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_TypeOfBoard_Description",
      """
        function(type_of_board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_board_id", encodeURIComponent(type_of_board_id))})
        }
      """
    )
  
    // @LINE:291
    def edit_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_SingleLibrary",
      """
        function(library_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:240
    def getProcessorSingleLibraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorSingleLibraries",
      """
        function(processor_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/singleLibrary/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id))})
        }
      """
    )
  
    // @LINE:276
    def editLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.editLibraryGroup",
      """
        function(libraryGroup_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryGroup_id", encodeURIComponent(libraryGroup_id))})
        }
      """
    )
  
    // @LINE:282
    def upload_Library_To_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.upload_Library_To_LibraryGroup",
      """
        function(libraryGroup_id,version_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/upload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryGroup_id", encodeURIComponent(libraryGroup_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version_id", encodeURIComponent(version_id))})
        }
      """
    )
  
    // @LINE:287
    def get_SingleLibrary_Versions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_SingleLibrary_Versions",
      """
        function(library_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/versions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:285
    def new_SingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_SingleLibrary",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library"})
        }
      """
    )
  
    // @LINE:274
    def delete_LibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_LibraryGroup",
      """
        function(libraryGroup_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryGroup_id", encodeURIComponent(libraryGroup_id))})
        }
      """
    )
  
    // @LINE:237
    def disconnectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.disconnectProcessorWithLibraryGroup",
      """
        function(processor_id,library_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:243
    def new_Board: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_Board",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board"})
        }
      """
    )
  
    // @LINE:222
    def uploadCompilationToBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploadCompilationToBoard",
      """
        function(c_program_id,board_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/upload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("c_program_id", encodeURIComponent(c_program_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board_id", encodeURIComponent(board_id))})
        }
      """
    )
  
    // @LINE:234
    def connectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibrary",
      """
        function(processor_id,library_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:215
    def edit_C_Program_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_C_Program_Description",
      """
        function(c_program_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/edit/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("c_program_id", encodeURIComponent(c_program_id))})
        }
      """
    )
  
    // @LINE:221
    def generateProjectForEclipse: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.generateProjectForEclipse",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/eclipse"})
        }
      """
    )
  
    // @LINE:265
    def delete_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_TypeOfBoard",
      """
        function(type_of_board_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_board_id", encodeURIComponent(type_of_board_id))})
        }
      """
    )
  
    // @LINE:260
    def get_Producer_TypeOfBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producer_TypeOfBoards",
      """
        function(producer_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/typeOfBoards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("producer_id", encodeURIComponent(producer_id))})
        }
      """
    )
  
    // @LINE:249
    def disconnect_Board_from_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.disconnect_Board_from_Project",
      """
        function(board_id,project_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/disconnect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board_id", encodeURIComponent(board_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id))})
        }
      """
    )
  
    // @LINE:254
    def new_Producer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_Producer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer"})
        }
      """
    )
  
    // @LINE:244
    def edit_Board_User_Description: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_Board_User_Description",
      """
        function(type_of_board_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/userDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_board_id", encodeURIComponent(type_of_board_id))})
        }
      """
    )
  
    // @LINE:267
    def get_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_TypeOfBoard",
      """
        function(type_of_board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_board_id", encodeURIComponent(type_of_board_id))})
        }
      """
    )
  
    // @LINE:232
    def delete_Processor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.delete_Processor",
      """
        function(processor_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id))})
        }
      """
    )
  
    // @LINE:281
    def get_LibraryGroup_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Version",
      """
        function(version_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/versions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version_id", encodeURIComponent(version_id))})
        }
      """
    )
  
    // @LINE:279
    def get_LibraryGroup_Libraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Libraries",
      """
        function(libraryGroup_id,version_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/libraries/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryGroup_id", encodeURIComponent(libraryGroup_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version_id", encodeURIComponent(version_id))})
        }
      """
    )
  
    // @LINE:212
    def get_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_C_Program",
      """
        function(c_program_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("c_program_id", encodeURIComponent(c_program_id))})
        }
      """
    )
  
    // @LINE:235
    def connectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibraryGroup",
      """
        function(processor_id,library_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:245
    def get_Board_Filter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Board_Filter",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/filter"})
        }
      """
    )
  
    // @LINE:250
    def getBoardProjects: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getBoardProjects",
      """
        function(board_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/projects/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board_id", encodeURIComponent(board_id))})
        }
      """
    )
  
    // @LINE:255
    def edit_Producer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_Producer",
      """
        function(producer_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("producer_id", encodeURIComponent(producer_id))})
        }
      """
    )
  
    // @LINE:248
    def connect_Board_with_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connect_Board_with_Project",
      """
        function(board_id,project_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/connect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board_id", encodeURIComponent(board_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id))})
        }
      """
    )
  
    // @LINE:213
    def get_C_Program_All_from_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_C_Program_All_from_Project",
      """
        function(project_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id))})
        }
      """
    )
  
    // @LINE:257
    def get_Producer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producer",
      """
        function(producer_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("producer_id", encodeURIComponent(producer_id))})
        }
      """
    )
  
    // @LINE:247
    def deactivate_Board: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deactivate_Board",
      """
        function(board_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/deactivateBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board_id", encodeURIComponent(board_id))})
        }
      """
    )
  
    // @LINE:239
    def getProcessorLibraryGroups: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorLibraryGroups",
      """
        function(processor_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroups/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processor_id", encodeURIComponent(processor_id))})
        }
      """
    )
  
    // @LINE:296
    def get_LibraryGroup_Version_Libraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_LibraryGroup_Version_Libraries",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "file/listOfFiles/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:216
    def update_C_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.update_C_Program",
      """
        function(c_program_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/update/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("c_program_id", encodeURIComponent(c_program_id))})
        }
      """
    )
  
    // @LINE:286
    def new_SingleLibrary_Version: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.new_SingleLibrary_Version",
      """
        function(library_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("library_id", encodeURIComponent(library_id))})
        }
      """
    )
  
    // @LINE:256
    def get_Producers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.get_Producers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/all"})
        }
      """
    )
  
    // @LINE:264
    def edit_TypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.edit_TypeOfBoard",
      """
        function(type_of_board_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("type_of_board_id", encodeURIComponent(type_of_board_id))})
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
  
    // @LINE:340
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

  // @LINE:70
  class ReversePermissionController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:83
    def get_Role_All: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_Role_All",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/all"})
        }
      """
    )
  
    // @LINE:71
    def remove_Permission_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.remove_Permission_Person",
      """
        function(person_id,permission_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/permission/person/remove" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id)])})
        }
      """
    )
  
    // @LINE:79
    def delete_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.delete_Role",
      """
        function(role_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:70
    def add_Permission_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.add_Permission_Person",
      """
        function(person_id,permission_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/permission/person/add" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id)])})
        }
      """
    )
  
    // @LINE:78
    def new_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.new_Role",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role"})
        }
      """
    )
  
    // @LINE:81
    def add_Role_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.add_Role_Person",
      """
        function(person_id,role_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/person/add" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:76
    def remove_Permission_from_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.remove_Permission_from_Role",
      """
        function(permission_id,role_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/permission/remove" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:74
    def add_Permission_to_Role: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.add_Permission_to_Role",
      """
        function(permission_id,role_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/permission/add" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("permission_id", permission_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:72
    def get_Permission_All: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_Permission_All",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/permission"})
        }
      """
    )
  
    // @LINE:75
    def get_Permission_in_Group: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_Permission_in_Group",
      """
        function(role_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/permission" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:82
    def remove_Role_Person: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.remove_Role_Person",
      """
        function(person_id,role_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/role/person/remove" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("role_id", role_id)])})
        }
      """
    )
  
    // @LINE:85
    def get_System_Acces: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.get_System_Acces",
      """
        function(person_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "secure/person/system_acces" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("person_id", person_id)])})
        }
      """
    )
  
  }

  // @LINE:304
  class ReverseGridController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:315
    def get_M_Program_byQR_Token_forMobile: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Program_byQR_Token_forMobile",
      """
        function(qr_token) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program/app/token/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("qr_token", encodeURIComponent(qr_token))})
        }
      """
    )
  
    // @LINE:314
    def new_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.new_M_Program",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program"})
        }
      """
    )
  
    // @LINE:305
    def get_M_Projects_ByLoggedPerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Projects_ByLoggedPerson",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/person"})
        }
      """
    )
  
    // @LINE:316
    def get_M_Program_all_forMobile: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Program_all_forMobile",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program/app/m_programs"})
        }
      """
    )
  
    // @LINE:328
    def remove_Screen_Size_Type: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.remove_Screen_Size_Type",
      """
        function(screen_size_type_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("screen_size_type_id", encodeURIComponent(screen_size_type_id))})
        }
      """
    )
  
    // @LINE:307
    def edit_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.edit_M_Project",
      """
        function(m_project_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_project_id", encodeURIComponent(m_project_id))})
        }
      """
    )
  
    // @LINE:319
    def edit_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.edit_M_Program",
      """
        function(m_progrm_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_progrm_id", encodeURIComponent(m_progrm_id))})
        }
      """
    )
  
    // @LINE:308
    def remove_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.remove_M_Project",
      """
        function(m_project_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_project_id", encodeURIComponent(m_project_id))})
        }
      """
    )
  
    // @LINE:327
    def edit_Screen_Size_Type: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.edit_Screen_Size_Type",
      """
        function(screen_size_type_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("screen_size_type_id", encodeURIComponent(screen_size_type_id))})
        }
      """
    )
  
    // @LINE:318
    def get_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Program",
      """
        function(m_progrm_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_progrm_id", encodeURIComponent(m_progrm_id))})
        }
      """
    )
  
    // @LINE:325
    def get_Screen_Size_Type_Combination: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_Screen_Size_Type_Combination",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/all"})
        }
      """
    )
  
    // @LINE:304
    def new_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.new_M_Project",
      """
        function(project_id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("project_id", project_id)])})
        }
      """
    )
  
    // @LINE:326
    def get_Screen_Size_Type: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_Screen_Size_Type",
      """
        function(screen_size_type_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/screen_type/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("screen_size_type_id", encodeURIComponent(screen_size_type_id))})
        }
      """
    )
  
    // @LINE:306
    def get_M_Project: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Project",
      """
        function(m_project_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_project_id", encodeURIComponent(m_project_id))})
        }
      """
    )
  
    // @LINE:320
    def remove_M_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.remove_M_Program",
      """
        function(m_progrm_id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("m_progrm_id", encodeURIComponent(m_progrm_id))})
        }
      """
    )
  
    // @LINE:310
    def get_M_Projects_from_GlobalProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GridController.get_M_Projects_from_GlobalProject",
      """
        function(project_id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "grid/m_project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id))})
        }
      """
    )
  
    // @LINE:324
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

  // @LINE:142
  class ReverseProgramingPackageController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:187
    def getBlockBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getBlockBlock",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:191
    def allPrevVersions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.allPrevVersions",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/allPrevVersions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:171
    def getProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgram",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:142
    def postNewProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewProject",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:192
    def deleteBlockVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteBlockVersion",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:149
    def getAll_b_Programs: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAll_b_Programs",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/b_programs/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:174
    def remove_b_Program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.remove_b_Program",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:145
    def getProjectsByUserAccount: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsByUserAccount",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:198
    def getAllTypeOfBlocks: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllTypeOfBlocks",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock"})
        }
      """
    )
  
    // @LINE:153
    def getProgramhomerList: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramhomerList",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/homerList/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:181
    def getProjectsBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/boards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:173
    def update_b_program: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.update_b_program",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/update/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:146
    def deleteProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteProject",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:144
    def getProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProject",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:199
    def deleteTypeOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteTypeOfBlock",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:179
    def listOfUploadedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.listOfUploadedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/listOfUploadedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:160
    def getAllHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllHomers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:172
    def editProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editProgram",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:177
    def uploadProgramToCloud: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToCloud",
      """
        function(id,ver) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/uploadToCloud/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("ver", encodeURIComponent(ver))})
        }
      """
    )
  
    // @LINE:197
    def editTypeOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editTypeOfBlock",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:188
    def getBlockVersions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getBlockVersions",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/versions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:154
    def getProjectOwners: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectOwners",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/owners/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:184
    def newBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newBlock",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock"})
        }
      """
    )
  
    // @LINE:195
    def newTypeOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newTypeOfBlock",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock"})
        }
      """
    )
  
    // @LINE:157
    def newHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newHomer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:161
    def getConnectedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getConnectedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/getAllConnectedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:186
    def editBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editBlock",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:143
    def updateProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.updateProject",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:158
    def removeHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.removeHomer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:175
    def getProgramInString: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramInString",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_programInJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:159
    def getHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getHomer",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:150
    def getAll_c_Programs: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAll_c_Programs",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/c_programs/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:185
    def updateOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.updateOfBlock",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:170
    def postNewBProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewBProgram",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program"})
        }
      """
    )
  
    // @LINE:176
    def uploadProgramToHomer_Immediately: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToHomer_Immediately",
      """
        function(id,ver) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/upload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("ver", encodeURIComponent(ver))})
        }
      """
    )
  
    // @LINE:193
    def deleteBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteBlock",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/block/id" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("id", id)])})
        }
      """
    )
  
    // @LINE:196
    def getByCategory: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getByCategory",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/typeOfBlock/filter"})
        }
      """
    )
  
    // @LINE:148
    def unshareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.unshareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/unshareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:147
    def shareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.shareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/shareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:165
    def connectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.connectHomerWithProject",
      """
        function(project_id,homer_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/connect/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("homer_id", encodeURIComponent(homer_id))})
        }
      """
    )
  
    // @LINE:166
    def disconnectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.disconnectHomerWithProject",
      """
        function(project_id,homer_id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/disconnect/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("project_id", encodeURIComponent(project_id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("homer_id", encodeURIComponent(homer_id))})
        }
      """
    )
  
    // @LINE:151
    def getAll_m_Projects: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAll_m_Projects",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/m_projects/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:180
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
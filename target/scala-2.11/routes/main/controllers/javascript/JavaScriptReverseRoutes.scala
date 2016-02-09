
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Tue Feb 09 16:44:42 CET 2016

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:8
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:273
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:273
    def at: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.at",
      """
        function(file) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
        }
      """
    )
  
  }

  // @LINE:69
  class ReverseOverFlowController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:86
    def getTypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getTypeOfConfirms",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm"})
        }
      """
    )
  
    // @LINE:97
    def likePlus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likePlus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likePlus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:74
    def getPostByFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostByFilter",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/postFilter"})
        }
      """
    )
  
    // @LINE:90
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
  
    // @LINE:83
    def getTypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getTypeOfPost",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost"})
        }
      """
    )
  
    // @LINE:78
    def commentsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.commentsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/comments/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:98
    def likeMinus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likeMinus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likeMinus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:82
    def newTypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newTypeOfPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost"})
        }
      """
    )
  
    // @LINE:85
    def newTypeOfConfirms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newTypeOfConfirms",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm"})
        }
      """
    )
  
    // @LINE:101
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
  
    // @LINE:99
    def linkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.linkWithPreviousAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link"})
        }
      """
    )
  
    // @LINE:75
    def getPostLinkedAnswers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostLinkedAnswers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/linkedAnswers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:73
    def getLatestPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getLatestPost",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/postAll"})
        }
      """
    )
  
    // @LINE:72
    def editPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.editPost",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post"})
        }
      """
    )
  
    // @LINE:89
    def addComment: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addComment",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/comment"})
        }
      """
    )
  
    // @LINE:79
    def answereListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.answereListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/answers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:70
    def getPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:93
    def addAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/answer"})
        }
      """
    )
  
    // @LINE:71
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
  
    // @LINE:100
    def unlinkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.unlinkWithPreviousAnswer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:69
    def newPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post"})
        }
      """
    )
  
    // @LINE:87
    def putTypeOfConfirmToPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.putTypeOfConfirmToPost",
      """
        function(conf,pst) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfConfirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("conf", encodeURIComponent(conf)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pst", encodeURIComponent(pst))})
        }
      """
    )
  
    // @LINE:77
    def hashTagsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.hashTagsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/hashTags/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:102
    def addHashTag: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addHashTag",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/hashTag"})
        }
      """
    )
  
    // @LINE:80
    def textOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.textOfPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/textOfPost/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }

  // @LINE:175
  class ReverseCompilationLibrariesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:242
    def getLibraryGroupLibraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupLibraries",
      """
        function(libraryId,version) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/libraries/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryId", encodeURIComponent(libraryId)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version", encodeURIComponent(version))})
        }
      """
    )
  
    // @LINE:195
    def updateProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateProcessor",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:196
    def deleteProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteProcessor",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:208
    def newBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newBoard",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board"})
        }
      """
    )
  
    // @LINE:245
    def uploudLibraryToLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploudLibraryToLibraryGroup",
      """
        function(libraryId,version) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/upload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryId", encodeURIComponent(libraryId)) + "/" + (""" + implicitly[PathBindable[Double]].javascriptUnbind + """)("version", version)})
        }
      """
    )
  
    // @LINE:236
    def getLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroup",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:177
    def gellAllProgramFromProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.gellAllProgramFromProject",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:255
    def getSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getSingleLibrary",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:261
    def uploadSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploadSingleLibrary",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/uploud/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:219
    def newProducers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newProducers",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer"})
        }
      """
    )
  
    // @LINE:211
    def deactivateBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deactivateBoard",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/deactivateBoard" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:182
    def deleteCProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteCProgram",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:244
    def getVersionLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getVersionLibraryGroup",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/versions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:193
    def getProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessor",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:240
    def getLibraryGroupDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/generalDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:231
    def getTypeOfBoardDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoardDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:232
    def getTypeOfBoardAllBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoardAllBoards",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/boards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:251
    def newSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newSingleLibrary",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library"})
        }
      """
    )
  
    // @LINE:194
    def getProcessorAll: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorAll",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:252
    def newVersionSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newVersionSingleLibrary",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:203
    def getProcessorDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:260
    def uploadSingleLibraryWithVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploadSingleLibraryWithVersion",
      """
        function(id,version) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/uploud/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[Double]].javascriptUnbind + """)("version", version)})
        }
      """
    )
  
    // @LINE:209
    def addUserDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.addUserDescription",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/userDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:235
    def newLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newLibraryGroup",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup"})
        }
      """
    )
  
    // @LINE:248
    def fileRecord: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.fileRecord",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/fileRecord/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:230
    def getTypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:179
    def updateCProgramDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateCProgramDescription",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/update/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:254
    def getSingleLibraryFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getSingleLibraryFilter",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/filter"})
        }
      """
    )
  
    // @LINE:210
    def getBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:259
    def deleteSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteSingleLibrary",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:212
    def getUserDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getUserDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/userDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:189
    def getBoardsFromProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getBoardsFromProject",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/project/board/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:192
    def newProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newProcessor",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:205
    def getProcessorSingleLibraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorSingleLibraries",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/singleLibrary/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:227
    def newTypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newTypeOfBoard",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard"})
        }
      """
    )
  
    // @LINE:175
    def newCProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newCProgram",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program"})
        }
      """
    )
  
    // @LINE:238
    def getLibraryGroupAll: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupAll",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup"})
        }
      """
    )
  
    // @LINE:253
    def getAllVersionSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getAllVersionSingleLibrary",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:220
    def updateProducers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateProducers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:180
    def newVersionOfCProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newVersionOfCProgram",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/newVersion/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:200
    def unconnectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.unconnectProcessorWithLibrary",
      """
        function(id,lbrId) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbr/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrId", encodeURIComponent(lbrId))})
        }
      """
    )
  
    // @LINE:198
    def connectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibrary",
      """
        function(id,lbrId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbr/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrId", encodeURIComponent(lbrId))})
        }
      """
    )
  
    // @LINE:243
    def createNewVersionLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.createNewVersionLibraryGroup",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/versions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:185
    def generateProjectForEclipse: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.generateProjectForEclipse",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/eclipse"})
        }
      """
    )
  
    // @LINE:186
    def uploudCompilationToBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploudCompilationToBoard",
      """
        function(id,board) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/uploud/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("board", encodeURIComponent(board))})
        }
      """
    )
  
    // @LINE:224
    def getProducerTypeOfBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducerTypeOfBoards",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/typeOfBoards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:183
    def deleteVersionOfCProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteVersionOfCProgram",
      """
        function(id,version) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version", encodeURIComponent(version))})
        }
      """
    )
  
    // @LINE:239
    def updateLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateLibraryGroup",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:214
    def unconnectBoardWthProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.unconnectBoardWthProject",
      """
        function(id,pr) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/unconnect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pr", encodeURIComponent(pr))})
        }
      """
    )
  
    // @LINE:237
    def deleteLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteLibraryGroup",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:199
    def connectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibraryGroup",
      """
        function(id,lbrgId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbrgrp/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrgId", encodeURIComponent(lbrgId))})
        }
      """
    )
  
    // @LINE:187
    def uploudBinaryFileToBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploudBinaryFileToBoard",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/binary/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:215
    def getBoardProjects: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getBoardProjects",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/projects/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:222
    def getProducer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducer",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:241
    def getLibraryGroupProcessors: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupProcessors",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/processors/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:247
    def listOfFilesInVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.listOfFilesInVersion",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/listOfFiles/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:221
    def getProducers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer"})
        }
      """
    )
  
    // @LINE:256
    def getSingleLibraryAll: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getSingleLibraryAll",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library"})
        }
      """
    )
  
    // @LINE:213
    def connectBoardWthProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectBoardWthProject",
      """
        function(id,pr) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/connect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pr", encodeURIComponent(pr))})
        }
      """
    )
  
    // @LINE:201
    def unconnectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.unconnectProcessorWithLibraryGroup",
      """
        function(id,lbrgId) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbrgrp/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrgId", encodeURIComponent(lbrgId))})
        }
      """
    )
  
    // @LINE:176
    def getCProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getCProgram",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/c_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:228
    def updateTypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateTypeOfBoard",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:258
    def updateSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateSingleLibrary",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:204
    def getProcessorLibraryGroups: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorLibraryGroups",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroups/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:229
    def getTypeOfBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoards",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard"})
        }
      """
    )
  
    // @LINE:223
    def getProducerDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducerDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }

  // @LINE:8
  class ReverseSecurityController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:29
    def Twitter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.Twitter",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/twitter"})
        }
      """
    )
  
    // @LINE:28
    def Facebook: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.Facebook",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/facebook"})
        }
      """
    )
  
    // @LINE:35
    def GET_facebook_oauth: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.GET_facebook_oauth",
      """
        function(url) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/facebook/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:36
    def GET_github_oauth: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.GET_github_oauth",
      """
        function(url) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/github/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:31
    def Vkontakte: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.Vkontakte",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/vkontakte"})
        }
      """
    )
  
    // @LINE:30
    def GitHub: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.GitHub",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "login/github"})
        }
      """
    )
  
    // @LINE:270
    def optionLink: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.optionLink",
      """
        function(all) {
          return _wA({method:"OPTIONS", url:"""" + _prefix + { _defaultPrefix } + """" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("all", all)})
        }
      """
    )
  
    // @LINE:26
    def logout: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.logout",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/permission/logout"})
        }
      """
    )
  
    // @LINE:33
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
  
    // @LINE:25
    def login: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.login",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/permission/login"})
        }
      """
    )
  
  }

  // @LINE:58
  class ReversePermissionController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:60
    def createGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.createGroup",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/group"})
        }
      """
    )
  
    // @LINE:62
    def getAllPersonPermission: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.getAllPersonPermission",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/personPermission"})
        }
      """
    )
  
    // @LINE:63
    def removeAllPersonPermission: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.removeAllPersonPermission",
      """
        function() {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/personPermission"})
        }
      """
    )
  
    // @LINE:64
    def addAllPersonPermission: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.addAllPersonPermission",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/personPermission"})
        }
      """
    )
  
    // @LINE:58
    def getAllPermissions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.getAllPermissions",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/permisionKeys"})
        }
      """
    )
  
    // @LINE:59
    def getAllGroups: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.getAllGroups",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/permisionGroups"})
        }
      """
    )
  
  }

  // @LINE:11
  class ReverseWikyController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:12
    def test2: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test2",
      """
        function(fields) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test2" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("fields", fields)])})
        }
      """
    )
  
    // @LINE:11
    def test: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WikyController.test",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test"})
        }
      """
    )
  
  }

  // @LINE:44
  class ReversePersonCreateController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:46
    def updatePersonInformation: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.updatePersonInformation",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
    // @LINE:49
    def deletePerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.deletePerson",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:45
    def standartRegistration: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.standartRegistration",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
    // @LINE:51
    def emailPersonAuthentitaction: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.emailPersonAuthentitaction",
      """
        function(mail,authToken) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "emailPersonAuthentication/" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("mail", mail), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("authToken", authToken)])})
        }
      """
    )
  
    // @LINE:47
    def getPerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.getPerson",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:44
    def developerRegistration: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.developerRegistration",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/developer"})
        }
      """
    )
  
  }

  // @LINE:112
  class ReverseProgramingPackageController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:164
    def allPrevVersions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.allPrevVersions",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/allPrevVersions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:141
    def getProgramInJson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramInJson",
      """
        function(id) {
        
          if (true) {
            return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_programInJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
          }
        
        }
      """
    )
  
    // @LINE:166
    def getByFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getByFilter",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/filter"})
        }
      """
    )
  
    // @LINE:138
    def getProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgram",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:112
    def postNewProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewProject",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:162
    def getBlockLast: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getBlockLast",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:154
    def newVersionOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newVersionOfBlock",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:115
    def getProjectsByUserAccount: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsByUserAccount",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:159
    def generalDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.generalDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/generalDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:120
    def getProgramhomerList: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramhomerList",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/homerList/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:156
    def designJsonVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.designJsonVersion",
      """
        function(id,version) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/designJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[Double]].javascriptUnbind + """)("version", version)})
        }
      """
    )
  
    // @LINE:149
    def getProjectsBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/boards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:116
    def deleteProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteProject",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:114
    def getProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProject",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:146
    def listOfUploadedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.listOfUploadedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/listOfUploadedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:127
    def getAllHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllHomers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:119
    def getAllPrograms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllPrograms",
      """
        function(id) {
        
          if (true) {
            return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/b_programs/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
          }
        
        }
      """
    )
  
    // @LINE:139
    def editProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editProgram",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:121
    def getProjectOwners: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectOwners",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/owners/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:153
    def newBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newBlock",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock"})
        }
      """
    )
  
    // @LINE:124
    def newHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newHomer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:140
    def removeProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.removeProgram",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:143
    def uploadProgramToCloud: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToCloud",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/uploudToCloud/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:157
    def logicJsonLast: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.logicJsonLast",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/logicJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:128
    def getConnectedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getConnectedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/getAllConnectedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:113
    def updateProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.updateProject",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:125
    def removeHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.removeHomer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:155
    def logicJsonVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.logicJsonVersion",
      """
        function(id,version) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/logicJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[Double]].javascriptUnbind + """)("version", version)})
        }
      """
    )
  
    // @LINE:161
    def getBlockVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getBlockVersion",
      """
        function(id,version) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[Double]].javascriptUnbind + """)("version", version)})
        }
      """
    )
  
    // @LINE:126
    def getHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getHomer",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:158
    def designJsonLast: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.designJsonLast",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/designJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:132
    def connectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.connectHomerWithProject",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/connectHomerWithProject"})
        }
      """
    )
  
    // @LINE:165
    def deleteBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteBlock",
      """
        function(url) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:118
    def unshareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.unshareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/unshareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:160
    def versionDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.versionDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/versionDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:137
    def postNewProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewProgram",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program"})
        }
      """
    )
  
    // @LINE:142
    def uploadProgramToHomer_Immediately: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToHomer_Immediately",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/b_program/uploud"})
        }
      """
    )
  
    // @LINE:117
    def shareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.shareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/shareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:133
    def unConnectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.unConnectHomerWithProject",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/unconnectHomerWithProject"})
        }
      """
    )
  
    // @LINE:147
    def listOfHomersWaitingForUpload: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.listOfHomersWaitingForUpload",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/listOfHomersWaitingForUpload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }

  // @LINE:15
  class ReverseWebSocketController_Incoming(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:18
    def getWebSocketStats: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WebSocketController_Incoming.getWebSocketStats",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "websocket/webSocketStats"})
        }
      """
    )
  
    // @LINE:19
    def sendTo: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WebSocketController_Incoming.sendTo",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "websocket/sendTo/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:16
    def mobile_connection: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.WebSocketController_Incoming.mobile_connection",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "websocket/mobile/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:15
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
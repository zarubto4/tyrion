
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/zaruba/ownCloud/Git/Tyrion/conf/routes
// @DATE:Fri Jan 22 11:11:57 CET 2016

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:6
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:44
  class ReverseOverFlowController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:69
    def likePlus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likePlus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likePlus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:49
    def getPostByFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostByFilter",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/postFilter"})
        }
      """
    )
  
    // @LINE:62
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
  
    // @LINE:58
    def getTypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getTypeOfPost",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost"})
        }
      """
    )
  
    // @LINE:53
    def commentsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.commentsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/comments/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:70
    def likeMinus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.likeMinus",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/likeMinus/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:57
    def newTypeOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newTypeOfPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/typeOfPost"})
        }
      """
    )
  
    // @LINE:73
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
  
    // @LINE:71
    def linkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.linkWithPreviousAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link"})
        }
      """
    )
  
    // @LINE:50
    def getPostLinkedAnswers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPostLinkedAnswers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/linkedAnswers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:48
    def getLatestPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getLatestPost",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/postAll"})
        }
      """
    )
  
    // @LINE:47
    def editPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.editPost",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post"})
        }
      """
    )
  
    // @LINE:61
    def addComment: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addComment",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/comment"})
        }
      """
    )
  
    // @LINE:54
    def answereListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.answereListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/answers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:77
    def removeConfirmType: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.removeConfirmType",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/confirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:45
    def getPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.getPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:65
    def addAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addAnswer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/answer"})
        }
      """
    )
  
    // @LINE:46
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
  
    // @LINE:72
    def unlinkWithPreviousAnswer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.unlinkWithPreviousAnswer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/link/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:44
    def newPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.newPost",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post"})
        }
      """
    )
  
    // @LINE:76
    def addConfirmType: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addConfirmType",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/confirm/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:52
    def hashTagsListOnPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.hashTagsListOnPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/hashTags/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:74
    def addHashTag: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.addHashTag",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/hashTag"})
        }
      """
    )
  
    // @LINE:55
    def textOfPost: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.OverFlowController.textOfPost",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "overflow/post/textOfPost/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }

  // @LINE:145
  class ReverseCompilationLibrariesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:197
    def getLibraryGroupLibraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupLibraries",
      """
        function(libraryId,version) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/libraries/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryId", encodeURIComponent(libraryId)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("version", encodeURIComponent(version))})
        }
      """
    )
  
    // @LINE:148
    def updateProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateProcessor",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:149
    def deleteProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteProcessor",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:162
    def newBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newBoard",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board"})
        }
      """
    )
  
    // @LINE:200
    def uploudLibraryToLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploudLibraryToLibraryGroup",
      """
        function(libraryId,version) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/upload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("libraryId", encodeURIComponent(libraryId)) + "/" + (""" + implicitly[PathBindable[Double]].javascriptUnbind + """)("version", version)})
        }
      """
    )
  
    // @LINE:191
    def getLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroup",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:209
    def getSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getSingleLibrary",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:173
    def newProducers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newProducers",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer"})
        }
      """
    )
  
    // @LINE:165
    def deactivateBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deactivateBoard",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/deactivateBoard" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:199
    def getVersionLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getVersionLibraryGroup",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/versions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:146
    def getProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessor",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:195
    def getLibraryGroupDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/generalDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:185
    def getTypeOfBoardDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoardDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:186
    def getTypeOfBoardAllBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoardAllBoards",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/boards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:206
    def newSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newSingleLibrary",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library"})
        }
      """
    )
  
    // @LINE:147
    def getProcessorAll: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorAll",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:207
    def newVersionSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newVersionSingleLibrary",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:156
    def getProcessorDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:214
    def uploadSingleLibraryWithVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.uploadSingleLibraryWithVersion",
      """
        function(id,version) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/uploud/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[Double]].javascriptUnbind + """)("version", version)})
        }
      """
    )
  
    // @LINE:163
    def addUserDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.addUserDescription",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/userDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:190
    def newLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newLibraryGroup",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup"})
        }
      """
    )
  
    // @LINE:203
    def fileRecord: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.fileRecord",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/fileRecord/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:184
    def getTypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:208
    def getSingleLibraryFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getSingleLibraryFilter",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/filter"})
        }
      """
    )
  
    // @LINE:164
    def getBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:213
    def deleteSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteSingleLibrary",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:166
    def getUserDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getUserDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/userDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:145
    def newProcessor: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newProcessor",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor"})
        }
      """
    )
  
    // @LINE:158
    def getProcessorSingleLibraries: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorSingleLibraries",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/singleLibrary/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:181
    def newTypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.newTypeOfBoard",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard"})
        }
      """
    )
  
    // @LINE:193
    def getLibraryGroupAll: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupAll",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup"})
        }
      """
    )
  
    // @LINE:174
    def updateProducers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateProducers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:153
    def unconnectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.unconnectProcessorWithLibrary",
      """
        function(id,lbrId) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbr/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrId", encodeURIComponent(lbrId))})
        }
      """
    )
  
    // @LINE:151
    def connectProcessorWithLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibrary",
      """
        function(id,lbrId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbr/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrId", encodeURIComponent(lbrId))})
        }
      """
    )
  
    // @LINE:198
    def createNewVersionLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.createNewVersionLibraryGroup",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/version/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:217
    def generateProjectForEclipse: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.generateProjectForEclipse",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/project/eclipse"})
        }
      """
    )
  
    // @LINE:178
    def getProducerTypeOfBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducerTypeOfBoards",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/typeOfBoards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:194
    def updateLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateLibraryGroup",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:168
    def unconnectBoardWthProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.unconnectBoardWthProject",
      """
        function(id,pr) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/unconnect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pr", encodeURIComponent(pr))})
        }
      """
    )
  
    // @LINE:192
    def deleteLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.deleteLibraryGroup",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:152
    def connectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectProcessorWithLibraryGroup",
      """
        function(id,lbrgId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbrgrp/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrgId", encodeURIComponent(lbrgId))})
        }
      """
    )
  
    // @LINE:215
    def getSingleLibraryDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getSingleLibraryDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:169
    def getBoardProjects: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getBoardProjects",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/projects/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:176
    def getProducer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducer",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:196
    def getLibraryGroupProcessors: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getLibraryGroupProcessors",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/libraryGroup/processors/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:202
    def listOfFilesInVersion: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.listOfFilesInVersion",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/listOfFiles/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:175
    def getProducers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer"})
        }
      """
    )
  
    // @LINE:210
    def getSingleLibraryAll: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getSingleLibraryAll",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library"})
        }
      """
    )
  
    // @LINE:167
    def connectBoardWthProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.connectBoardWthProject",
      """
        function(id,pr) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/board/connect/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("pr", encodeURIComponent(pr))})
        }
      """
    )
  
    // @LINE:154
    def unconnectProcessorWithLibraryGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.unconnectProcessorWithLibraryGroup",
      """
        function(id,lbrgId) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/lbrgrp/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id)) + "/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("lbrgId", encodeURIComponent(lbrgId))})
        }
      """
    )
  
    // @LINE:182
    def updateTypeOfBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateTypeOfBoard",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:212
    def updateSingleLibrary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.updateSingleLibrary",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/library/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:157
    def getProcessorLibraryGroups: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProcessorLibraryGroups",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/processor/libraryGroups/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:183
    def getTypeOfBoards: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getTypeOfBoards",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/typeOfBoard"})
        }
      """
    )
  
    // @LINE:177
    def getProducerDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CompilationLibrariesController.getProducerDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "compilation/producer/description/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }

  // @LINE:15
  class ReverseSecurityController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:16
    def logout: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.logout",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/permission/logout"})
        }
      """
    )
  
    // @LINE:226
    def optionLink: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.optionLink",
      """
        function(all) {
          return _wA({method:"OPTIONS", url:"""" + _prefix + { _defaultPrefix } + """" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("all", all)})
        }
      """
    )
  
    // @LINE:15
    def login: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SecurityController.login",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/permission/login"})
        }
      """
    )
  
  }

  // @LINE:33
  class ReversePermissionController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:35
    def createGroup: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.createGroup",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/group"})
        }
      """
    )
  
    // @LINE:37
    def getAllPersonPermission: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.getAllPersonPermission",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/personPermission"})
        }
      """
    )
  
    // @LINE:38
    def removeAllPersonPermission: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.removeAllPersonPermission",
      """
        function() {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/personPermission"})
        }
      """
    )
  
    // @LINE:39
    def addAllPersonPermission: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.addAllPersonPermission",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/personPermission"})
        }
      """
    )
  
    // @LINE:33
    def getAllPermissions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.getAllPermissions",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/permisionKeys"})
        }
      """
    )
  
    // @LINE:34
    def getAllGroups: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PermissionController.getAllGroups",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "permission/permisionGroups"})
        }
      """
    )
  
  }

  // @LINE:6
  class ReverseApplication(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:6
    def index: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.index",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + """"})
        }
      """
    )
  
  }

  // @LINE:24
  class ReversePersonCreateController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:27
    def deletePerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.deletePerson",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:26
    def getPerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.getPerson",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:25
    def updatePersonInformation: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.updatePersonInformation",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
    // @LINE:24
    def createNewPerson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PersonCreateController.createNewPerson",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "coreClient/person/person"})
        }
      """
    )
  
  }

  // @LINE:85
  class ReverseProgramingPackageController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:134
    def allPrevVersions: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.allPrevVersions",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/allPrevVersions/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:113
    def getProgramInJson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramInJson",
      """
        function(id) {
        
          if (true) {
            return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/programInJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
          }
        
        }
      """
    )
  
    // @LINE:136
    def getByFilter: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getByFilter",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/filter"})
        }
      """
    )
  
    // @LINE:110
    def getProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgram",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:85
    def postNewProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewProject",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:127
    def newVersionOfBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newVersionOfBlock",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:88
    def getProjectsByUserAccount: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsByUserAccount",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project"})
        }
      """
    )
  
    // @LINE:130
    def generalDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.generalDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/generalDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:93
    def getProgramhomerList: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramhomerList",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/homerList/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:120
    def getProjectsBoard: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectsBoard",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/boards/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:89
    def deleteProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteProject",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:132
    def getBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getBlock",
      """
        function(url) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:87
    def getProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProject",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:117
    def listOfUploadedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.listOfUploadedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/listOfUploadedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:101
    def getAllHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllHomers",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:116
    def getAllPrograms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getAllPrograms",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/getallprograms/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:122
    def uploadProgramToHomer_AsSoonAsPossible: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToHomer_AsSoonAsPossible",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/uploudtohomerAsSoonAsPossible"})
        }
      """
    )
  
    // @LINE:111
    def editProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.editProgram",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:94
    def getProjectOwners: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProjectOwners",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/owners/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:126
    def newBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newBlock",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock"})
        }
      """
    )
  
    // @LINE:97
    def newHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.newHomer",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer"})
        }
      """
    )
  
    // @LINE:112
    def removeProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.removeProgram",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/program/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:92
    def getProgramPrograms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getProgramPrograms",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/programs/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:100
    def getConnectedHomers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getConnectedHomers",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/getAllConnectedHomers/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:86
    def updateProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.updateProject",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:98
    def removeHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.removeHomer",
      """
        function(id) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:99
    def getHomer: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.getHomer",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/homer/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:105
    def connectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.connectHomerWithProject",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/connectHomerWithProject"})
        }
      """
    )
  
    // @LINE:135
    def deleteBlock: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.deleteBlock",
      """
        function(url) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:91
    def unshareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.unshareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/unshareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:131
    def versionDescription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.versionDescription",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/versionDescription/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:109
    def postNewProgram: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.postNewProgram",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "project/program"})
        }
      """
    )
  
    // @LINE:121
    def uploadProgramToHomer_Immediately: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToHomer_Immediately",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/uploudtohomerImmediately"})
        }
      """
    )
  
    // @LINE:90
    def shareProjectWithUsers: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.shareProjectWithUsers",
      """
        function(id) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/project/shareProject/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:129
    def designJson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.designJson",
      """
        function(url) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/designJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:123
    def uploadProgramToHomer_GivenTimeAsSoonAsPossible: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.uploadProgramToHomer_GivenTimeAsSoonAsPossible",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/uploudtohomerGivenTime"})
        }
      """
    )
  
    // @LINE:106
    def unConnectHomerWithProject: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.unConnectHomerWithProject",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "project/unconnectHomerWithProject"})
        }
      """
    )
  
    // @LINE:128
    def logicJson: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.logicJson",
      """
        function(url) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/blockoBlock/logicJson/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("url", url)})
        }
      """
    )
  
    // @LINE:118
    def listOfHomersWaitingForUpload: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ProgramingPackageController.listOfHomersWaitingForUpload",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "project/listOfHomersWaitingForUpload/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }


}
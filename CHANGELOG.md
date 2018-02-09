# TYRION 2.0

**!! API is not fully tested, so there might be bugs and errors !!**

**!! Some DB queries might have old names inside !!**

### Model changes

 - Model_Board => Model_Hardware
 - Model_TypeOfBoard => Model_HardwareType
 - Model_TypeOfBoardBatch => Model_HardwareBatch
 - Model_TypeOfBoardFeature => Model_HardwareFeature
 - Model_BoardGroup => Model_HardwareGroup
 - Model_BlockoBlock => Model_Block
 - Model_BlockoBlockVersion => Model_BlockVersion
 - Model_GridWidget => Model_Widget
 - Model_GridWidgetVersion => Model_WidgetVersion
 - Model_MProject => Model_GridProject
 - Model_MProgram => Model_GridProgram
 - Model_CProgramUpdatePlan => Model_HardwareUpdate
 - Model_ActualizationProcedure => Model_UpdateProcedure
 - Model_HomerInstance => Model_Instance
 - Model_HomerInstanceRecord => Model_InstanceSnapshot
 - Model_VersionObject => Model_Version
 - Model_FileRecord => Model_Blob
 
 All models have to extend *utilities.model.BaseModel*, this class contains <br> some common fields and methods
 like id, created, updated, save(), delete().
 
 Class utilities.model.NamedModel extends BaseModel with name and description.
 
 Class utilities.model.TaggedModel extends NamedModel with tags and tagging API.
 
### New models

 - Model_HardwareRegistration 
    - used as a connection to project
    - all Instances and UpdateProcedures **must** have relations to this object instead of Model_Hardware
    - if user removes hw from project, Model_Hardware is only disconnected from this object
    - if registration does not have hardware anymore, should be display as inactive
    - name and description for hardware should be probably stored in this object
    
 - Model_Tag
    - should be used for searching, filtering and grouping of various models
    - API for adding and removing tags is complete
 
### Removed models

 - Model_TypeOfBlock
    - Model_Tag should be used instead of this

 - Model_TypeOfWidget
    - Model_Tag should be used instead of this

## TODOs
 
 - create update procedure when new instance snapshot is deployed
 - Model_Blob: make only one method for uploading and one for downloading files
 - create APIs for searching by Model_Tag
 - synchronization of hardware and batches from central authority, incompatible now, must do some changes

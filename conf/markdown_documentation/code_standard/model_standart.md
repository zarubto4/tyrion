
# How to write Model Class (ORM)

### Example

Description:

   * @Entity - Is required for Postgres database
   * @ApiModel -Is required for Swagger - Rest-Api documentation tool
      * **value** - Name of Model without "Model_" - for example ---> **C_Program**
      * **description** - option value for documentation - for example ---> **(Object represented C_Program in database)**
   * Example of extended class with Model - for ORM operations
           
   ```java 
           
            @Entity
            @ApiModel(value="C_Program", description="Object represented C_Program in database")
            public class Model_CProgram extends Model {
    
                ** Content **
    
            }
   ```
   
   * Best fullsupported example is in code in **app.models._Model_ExampleModelName**  where you can find lot of  **"shotcuts"**  

___

### Content:

   Each model has snippet segments of the code. (LOGGER, DATABASE VALUE etc... ) We sort them according to the scheme below. If you want create new model 
   we recommend copying from the documentation directly to the new class Model. 

   ### LAW! 
   * The beginning of the comment is without spaces at the beginning of the line. 
   * We don't change the order of each snippet segments!!
   * When we add a new snippet type - we add it to all the classes in the right place.
   * We leave the document lines in the Model classes, though there will be no content under it!


##  /* LOGGER  */

   Reserved for Global defined own Byzance Loger. Its complex and huge tool for Tyrion developers and Tester. 
   We have two kind of Logs. 
   
   * System Logger 
   * User activity Logger  
     
     ```java 
     
        // System Logger  
        private static final Class_Logger terminal_logger = new Class_Logger( C_Program.class);
        
        // User activity Logger  
        // TODO 
        
     ```
     
     
   #### System Logger    
     
     
     
   #### User activity Logger 
   
       
##  /* DATABASE VALUE  */
   
##  /* JSON PROPERTY METHOD && VALUES */
 
 
##  /* JSON IGNORE METHOD && VALUES */


## /* SAVE && UPDATE && DELETE */

   To save, delete, and update the Model in the database, we use the ORM Override method.
   In addition to CRUD (Create, Read, Update, Delete), we update the cache memory, we can send change notifications to frontend,
   or synchronize server parameters with Homer Server, Compilation server And etc.

  ```java 
        
        @JsonIgnore @Override public void save() {


        }

        @JsonIgnore @Override public void update() {

             // Case 1.1 :: We delete the object
             super.update();

             // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
            new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project.id, object.id))).start();

        }


        @JsonIgnore @Override public void delete() {

            // Case 1.1 :: We delete the object
            super.delete();

            // Case 1.2 :: After Delete - we send notification to frontend (Only if it is desirable)
            new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project.id))).start();




            // Case 2.1 :: We delete the object with change of ORM parameter  @JsonIgnore  public boolean removed_by_user;
            this.removed_by_user = true;
            this.update();

            // Case 1.2 :: After Delete - we send notification to frontend (Only if it is desirable)
            new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project.id))).start();




            // Case 3 :: In some cases, it is not possible to delete an object - it is therefore impossible to delete the object overright by the method
            logger.error(this.getClass().getSimpleName() + ":: delete :: This object is not legitimate to remove. ");


            // Case 3 :: In some cases, it is not possible to delete an object - it is therefore impossible to delete the object overright by the method
            logger.error(this.getClass().getSimpleName() + ":: delete :: This object is not legitimate to remove. ");

        }
  ```
    
## /* SERVER WEBSOCKET */

## /* HELP CLASSES */

## /* NOTIFICATION */

## /* BlOB DATA */

  ```java 
      
      // Link, which is randomly generated for Azure - and which is a path to file
      // The variable String in ORM is private, because in some objects the path is composed (We have a tree structure)
      @JsonIgnore private String azure_c_program_link;

      @JsonIgnore @Transient
      public String get_path(){
         return  azure_c_program_link;
      }
      
   ```

## /* PERMISSION Description */
## /* PERMISSION */
## /* CACHE */

   ```java 

        // For Cache Name, we use Something.class.getSimpleName()
        // For the other CACHE in the same object we use an additional prefix with uppercase letters "_SOMETHING"
        // This variable is static final identifier across program for this CACHE - It can also be on another server
        // For more details please read [CACHE DOCU](../cache/cache.markdown)
    
        public static final String CACHE         = Model_CProgram.class.getSimpleName();
        public static final String CACHE_VERSION = Model_CProgram.class.getSimpleName() + "_VERSION";
 
     
        // Method must be null on beginning. If server start - their references are created in a separate class or connected with
        // external cache on another server
        public static Cache<String, Model_CProgram> cache = null;               // < Model_CProgram_id, Model_CProgram>
        public static Cache<String, Model_VersionObject> cache_versions = null; // < Model_VersionObject_id, Model_VersionObject>
    
    
          @JsonIgnore
            public static Model_CProgram get_byId(String id) {
    
                Model_CProgram c_program = cache.get(id);
                if (c_program == null){
    
                    c_program = Model_CProgram.find.byId(id);
                    if (c_program == null){
                        logger.warn( Model_CProgram.class.getSimpleName() + ":: get_byId :: This object id:: " + id + " wasn't found");
                        return null;
                    }
    
                    cache.put(id, c_program);
                }
    
                return c_program;
            }
   ```
    
   
## /* FINDER */
  
  ```java 
    
    // If we are using Cache - its good idea set find object to private.
    // ->>> Then it is guaranteed that all queries are processed through Cache

     // Case 1 :: NO Cache
     public static Model.Finder<String,Model_CProgram> find = new Finder<>(Model_CProgram.class);

     // Case 2 :: With Cache
     private static Model.Finder<String,Model_CProgram> find = new Finder<>(Model_CProgram.class);

  ```

# Classes
 * Names should start with capital letter and should by written in **CamelCase**. (e.g. Model_TypeOfBlock)
 * Classes of models, controllers or swagger documentation classes are **prefixed** with "type" and underscore.

#### Controllers
 * name is prefixed with "**Controller_**"
 * pattern: *Controller_NameOfController*
 * e.g. **Controller_Blocko**

#### Models
 * name is prefixed with "**Model_**"
 * pattern: *Model_NameOfModel*
 * e.g. **Model_Person**

#### Swagger
 * name is prefixed with "**Swagger_**"
 * pattern: *Swagger_NameOfObject_Operation*
 * e.g. **Swagger_TypeOfBlock_New**

#### Others
 * Other classes like scheduler jobs or other utilities should just use CamelCase.

# Methods
 * **Every** method name should start with lowercase letter and use camelCase.
 * Action methods (in Controllers) have following pattern: *givenObject_operationOnObject()*

        public Result typeOfBlock_getByFilter(){
            //code
            return Result;
        }


 * Methods in model classes which are annotated with **@JsonProperty** should be named like a field. Pattern: *every_letter_lowercase()*

        @JsonProperty
        public String project_id(){
            return this.project.id;
        }

 * other methods should just use camelCase and first lowercase letter. Pattern: *firstLetterLowercase()*

        public boolean isValid(){
            return true;
        }

# Fields and variables
 * Class fields in models have **every** letter lowercase and words are divided by underscore. Pattern: *this_is_field*
 * Local variables and other fields that are **not returned** in JSON can be named in camelCase. Pattern: *localVariable*

# Logging
#### Errors from request
 * **Every** action method contains **try-catch** block. In *try* block is actual code and in *catch* block is only exception logging. Loggy is used to log internal server errors. Method *result_internalServerError()* returns Result with http status code 500.

        ...
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

#### Errors from code and non request methods
 * When exception occurs, but we do not want to return status 500 to the client. (Sending email, CRON jobs, other errors)

        ...

        // Some code

        try{
            // Sending email here
        } catch (Exception e) {
            Loggy.internalServerError("Controller_Person:: person_create:", e);
        }

        // Code continues

        ...

#### Logging application behaviour
 * Sometimes we want to know, what the application is doing, for that purpose some classes have static logger defined with name *Loggy*. Declaration looks like this:

        private static play.Logger.ALogger logger = play.Logger.of("Loggy");

 * Logger has different levels of logging, depending on how detailed information we want to know.
 * Levels are: "*error, warn, info, debug, trace*" in descending order of importance.
 * We do not use level **error**, because exceptions are logged via **Loggy** class.
 * Level **warning** is used to log application lifecycle (e.g. onStart events, onStop events etc.) or behaviour that is unwanted, but code still can continue.

        logger.warn("Global:: onStart: Initializing cache layer");

 * Level **info** should be used to show which processes and procedures are happening within the application. There should be about one log per method.

        logger.info("Controller_Project:: project_create: creating new Project");

 * Level **debug** shows more detailed logs, for example if some condition is met, so we know where the program is running.

        logger.debug("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread:: concurrent thread started on {}", new Date());

        ...
        if(tokens.isEmpty()){

            logger.debug("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread: no tokens to remove");

        } else {

        logger.debug("Job_OldFloatingTokenRemoval:: remove_floating_person_token_thread: removing old tokens (100 per cycle)");

        ...
 * Last level is **trace**. It is the most detailed level, for example it can log values of parameters of a method and so on, but we do not this level much.
 * Remember that in well logged application, errors can be easily found, so it is **important**, but you should use your **own reason** where to place logs and how many of them, since because it could cause performance problems. If you get **confident** about some part of your code, you can even remove some logs or switch level.
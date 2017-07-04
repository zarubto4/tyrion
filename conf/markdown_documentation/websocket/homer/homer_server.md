
## Tyrion <-> Homer-Server ##

Channel Code:: **homer-server**

**List of Json Objects for Comunication between Tyrion and Homer Instance**

Any incoming or outgoing message to Tyrion and from Tyrion contains

        {
            "message_type"    :  "nazev_zpravy"              
            "message_id"      :  "some_uuid_string"
            "message_channel" :  "channel_name"  
        }
        
 - yoda_unauthorized_logging 
 - checkUserPermission
 - checkPersonToken
 - removePersonLoginToken
 
 
### Tyrion OutComing ###  
 Message for Tyrion for Homer
 
 #### [0-A] Server is not Verify ####
 [A] Veškerou komunikaci Tyrion blokuje dokud nedojde k ověření 
 
     Response: (Blokační zpráva, když není Homer ověřen)
          {  
            "message_type"    :  "homer_verification_first_required"              
            "message_id"      :  "some_uuid_string"
            "message_channel" :  "homer_server"  
          }
          
     Po této zprávě by Homer jako první měl zaslat Tyrionovi zprávu 
     Tato procedura je žádoucí i v případe, kdy přestane platit rest-api token. 
          
       Request To Tyrion From Homer:
           {  
              "message_type"    :  "homer_verification_first_required"              
              "message_id"      :  "some_uuid_string"
              "message_channel" :  "homer_server"  
           }   
           
       Response from Tyrion if Success (Token sedí || nesedí):
         {
               "message_type"   :   "homer_verification_result"    
               "status"         :   "success | error"   
               "message_id"     :   "same_uuid_string" 
               "token"          :   "rest_api_token"   <--- Only if status is success 
         }           
                     
 [B] Tyrion potvrdí oprávnění a homer může volně komunikovat
 
    Response:
          {  
            "message_type"    :  "homer_verification_token_approve"              
            "message_id"      :  "some_uuid_string"
            "message_channel" :  "homer_server"  
          }    
          
 #### [0-B] Get Verification Token ####
         
     Request: 
          {
            "message_type"    :   "homer_get_verification_token"              
            "message_id"      :   "some_uuid_string"
            "message_channel" :   "homer_server"  
          }               
      
     Response from Homer:
           {
             "status"         :   "success | error"
             "message_id"     :   "same_uuid_string"  
             "hash_token"     :   "loooooong_hash"   <--- Only if status is success 
             "error_code"     :   414 (Int)          <--- Only if status is error
           }   
           
     Response from Tyrion if Success (Token sedí || nesedí):
           {
             "message_type"   :   "homer_verification_result"    
             "status"         :   "success | error"   
             "message_id"     :   "same_uuid_string" 
             "token"          :   "rest_api_token"   <--- Only if status is success 
           }  
              
 
 #### [1] Get server configuration #### 
 
     Request: 
            {
               "message_type"    :   "homer_get_verification_token"              
               "message_id"      :   "some_uuid_string"
               "message_channel" :   "homer_server"  
               
             
             }   
            
     Response:
              {
                "status"         :   "success | error"
                "message_id"      :   "same_uuid_string" 
                 
                "server_name"    :   "homer"   <--- Only if status is success 
                "mqtt_port"      :   "homer"   <--- Only if status is success  
                "mqtt_user"      :   "homer"   <--- Only if status is success 
                "mqtt_password"  :   "homer"   <--- Only if status is success 
                "error_code"     :   414 (Int) <--- Only if status is error 
              }         
               
              
 #### [1-TODO] SET server configuration #### 
 
      // Zatím nepodporováno TODO 
 
 #### [2] Add Instance ####
     
     Request: 
         {
            "message_type"    :   "instance_create"              
            "message_id"      :   "some_uuid_string"
            "message_channel" :   "homer_server"  
            "instance_id:    :   "id_1" 
         }
         
         
      Response:
         {
           "status"         :   "success | error"
           "message_id"      :   "same_uuid_string"  
           "error_code"     :   414 (Int)  (Only if status is error) 
         }
 
 #### [3] Remove Instance #### 
 Odstraním-li Instanci - tak veškerý hardware se z instance vyřadí, ale zůstane připojený,
 nadále hardware nevrací id instance že v ní j. 
 
     Request: 
             {
                "message_type"    :   "instance_destroy"              
                "message_id"      :   "some_uuid_string"
                "message_channel" :   "homer_server"  
                "instance_id:    :   "id_1"
             }
             
     Response:
             {
               "status"         :   "success | error"
               "message_id"      :   "same_uuid_string"  
               "error_code"     :    414 (Int)  (Only if status is error) 
             }      
  
  
  #### [4] Get Instance Numbers ####  
  
    Request: 
             {
                "message_type"    :   "homer_instance_size"              
                "message_id"      :   "some_uuid_string"
                "message_channel" :   "homer_server"  
             }  
  
    Result: 
            {
                "status"         :   "success | error"
                "message_id"      :   "same_uuid_string"  
                "value"          :   123 (Int)  (Only if status is success)    
                "error_code"     :   414 (Int)  (Only if status is error) 
            }
            
  #### [5] Get Total Online Hardware ####  
    
      Request: 
               {
                  "message_type"    :   "homer_hardware_size"              
                  "message_id"      :   "some_uuid_string"
                  "message_channel" :   "homer_server"  
               }  
    
      Result: 
              {
                  "status"         :   "success | error"
                  "message_id"      :   "same_uuid_string"  
                  "value"          :   123 (Int)  (Only if status is success)    
                  "error_code"     :   414 (Int)  (Only if status is error) 
              }          


  #### [6] Get Verification Token ####  
  
       Request: 
                {
                  "message_type"    :   "homer_hardware_size"              
                  "message_id"      :   "some_uuid_string"
                  "messag_channel" :   "homer_server"  
                }  
        
       Result: 
                {
                   "status"         :   "success | error"
                   "message_id"      :   "same_uuid_string"  
                   "value"          :   123 (Int)  (Only if status is success)    
                   "error_code"     :   414 (Int)  (Only if status is error) 
                }          
        
   #### [7] Ping Homer Server ####  
    
         Request: 
                  {
                    "message_type"    :   "homer_ping"              
                    "message_id"      :   "some_uuid_string"
                    "message_channel" :   "homer_server"  
                  }  
          
         Result: 
                  {
                     "status"         :   "success | error"
                     "message_id"      :   "same_uuid_string"  
                     "response_time"  :   123 (ms)  (Only if status is success)   
                     "error_code"     :   414 (Int) (Only if status is error) 
                  }  
                  
                  
                  
### Tyrion Incoming ###
 Message from Homer to Tyrion 
     
     
     
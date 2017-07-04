
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
 [A] Veškerou komunikaci Tyrion blokuje dokud nedojde k ověření. Tu Homer server vykonává sám a to hned 
     na začátku zasláním zprávy *message_type::homer_verification_token*. Dokud homer tuto zprávu s validním
     klíčem nepošle, Tyrion žádnou zprávu nevputí a NEUTORIZUJE HOMER SERVER JAKO ONLINE.
     + je dobré počítat s tím, že Tyrion může revalidovat kdykoliv ověření.
     Token slouží ke komunikaci s Tyrionem. Vždy unikátní vždy nově generovaný. 
     
 
     Response: (Blokační zpráva, když není Homer ověřen)
          {  
            "message_type"    :  "homer_verification_first_required"              
            "message_id"      :  "some_uuid_string"      <---  Same as ID of incoming message
            "message_channel" :  "homer_server"  
          }
          
     Po této zprávě by Homer jako první měl zaslat Tyrionovi zprávu 
     Tato procedura je žádoucí i v případe, kdy přestane platit rest-api token. 
          
       Request To Tyrion From Homer:
           {  
              "message_type"    :  "homer_verification_token"              
              "message_id"      :  "some_uuid_string"       
              "hash_token"      :  "hash_token_looong_token"
              "message_channel" :  "homer_server"  
           }   
           
       Response from Tyrion if Success (Token sedí || nesedí):
         {
               "message_type"   :   "homer_verification_result"    
               "status"         :   "success | error"   
               "error_code"     :   301                <--- Opravit  
               "message_id"     :   "same_uuid_string" <---  STEJNÉ UUID JAKO PŘIŠEL HASH_TOKEN             
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
               
              
 #### [2] SET server configuration #### 
 
      // Zatím nepodporováno TODO 
 
 #### [3] Add Instance ####
     
     Request: 
         {
            "message_type"    :   "instance_create"              
            "message_id"      :   "some_uuid_string"
            "message_channel" :   "homer_server"  
            "instance_id:     :   "id_1" 
         }
         
      Response: (Odpovídá se na příkaz, nikoliv na celou exekutivitu vkonání přidání instance) 
      // Error - Nelze přidat stejnou instanci 
         {
           "status"         :   "success | error"
           "message_id"      :   "same_uuid_string"  
           "error_code"     :   414 (Int)  (Only if status is error) 
         }
 
 
 #### [4] Remove Instance #### 
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
     // Error - Lze Smazat neexistující instanci 
             {
               "status"         :   "success | error"
               "message_id"      :   "same_uuid_string"  
               "error_code"     :    414 (Int)  (Only if status is error) 
             }      
  
  
  #### [5] Get Instance Numbers ####  
  
    Request: 
             {
                "message_type"    :   "homer_instance_number"              
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
            
              
  #### [6] Get Instance List ####  
  
      Request: 
               {
                    "message_type"    :   "homer_instance_list"              
                    "message_id"      :   "some_uuid_string"
                    "message_channel" :   "homer_server"  
               }  
  
      Result: 
               {
                    "status"          :   "success | error"
                    "message_id"      :   "same_uuid_string"  
                    "instnace_ids"    :   ["id_1", "id_2"] 
                    "error_code"      :   414 (Int)  (Only if status is error) 
               }
            
  #### [7] Get Total Online Hardware ####  
    
      Request: 
               {
                    "message_type"     :   "homer_hardware_number"              
                    "message_id"       :   "some_uuid_string"
                    "message_channel"  :   "homer_server"  
               }  
    
      Result: 
               {
                    "status"           :   "success | error"
                    "message_id"       :   "same_uuid_string"  
                    "value"            :   123 (Int)  (Only if status is success)    
                    "error_code"       :   414 (Int)  (Only if status is error) 
               }          

   #### [8] Get Hardware List Ids ####  
    
      Request: 
               {
                  "message_type"    :   "homer_hardware_list"              
                  "message_id"      :   "some_uuid_string"
                  "message_channel" :   "homer_server"  
               }  
    
      Result: 
               { 
                  "status"         :   "success | error"
                  "message_id"     :   "same_uuid_string"  
                  "device_ids"     :   ["id_1", "id_2"] 
                  "error_code"     :   414 (Int)  (Only if status is error) 
               }          
        
   #### [9] Ping Homer Server ####  
    
         Request: 
                  {
                    "message_type"    :   "homer_ping"              
                    "message_id"      :   "some_uuid_string"
                    "message_channel" :   "homer_server"  
                  }  
          
         Result: 
                  {
                     "status"         :   "success | error"
                     "message_id"     :   "same_uuid_string"  
                     "response_time"  :   123 (ms)  (Only if status is success)   
                     "error_code"     :   414 (Int) (Only if status is error) 
                  }  
                  
                  
                  
### Tyrion Incoming ###
 Message from Homer to Tyrion 
     
     
     
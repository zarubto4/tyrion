
## Tyrion <-> Homer-Instance ##

Channel Code:: **instance**

**List of Json Objects for Comunication between Tyrion and Homer Instance**

Any incoming or outgoing message to Tyrion and from Tyrion contains

        {
            "message_type"    :  "nazev_zpravy"              
            "message_id"      :  "some_uuid_string"
            "message_channel" :  "channel_name"  <<-- In this case its instance
            "instance_id:    :  "............"  
        }
        
 - All Instances are in Flat structure
 
 - We have also 2 kind of Commands - "Singleton" for only one device or Multiple Command for List od device
   or with list of Independent Commands.
 
 
  
#### [1] SET Hardware to Instance ####
Pokud byl hardware v jiné instanci, zpřethrá vazby a přesune se do druhé instance. 
Podle dohody jde o snapshot toho v jakém stavu to má být (Neexistuje ADD ani REMOVE)
V případě ADD a Remove se pošle SNAP znovu. Homer se má postarat o vyřešení stavu. 

    Request: 
        {
           "message_type"    :   "instance_set_hardware"              
           "message_id"      :   "some_uuid_string"
           "message_channel" :   "instance"  
           
           "instance_id     :   "............"  
           "device_ids"     :  ["id_1", "id_2"]
        }
    
    Response:
         {
            "status"         :   "success | error"
            "message_id"      :   "same_uuid_string"  
                          
            "error_code"     :   414 (Int)  (Only if status is error) 
            
         }  
                

#### [2] Set Blocko Program to Instance ####

      Request: 
          {
               "message_type"    :   "instance_set_program"              
               "message_id"      :   "some_uuid_string"
               "message_channel" :   "instance"  
               
               "instance_id"    :   "............"  
               "b_program_name" :   "............"  
               "b_program_id"   :   "............"  
               "program_version_name"   :   "............"  
               "program_version_id"     :   "............"  <--- This ID is used for Homer API (Get File)
          }        

      Response: 
         {
             "status"         :   "success | error"
             "message_id"     :   "same_uuid_string"  
                                  
             "error_code"     :   414 (Int)  (Only if status is error)   
         }  
         
         
#### [3] Set Terminals to Instance ####         
    
    Request: 
          {
               "message_type"    :   "instance_set_terminals"              
               "message_id"      :   "some_uuid_string"
               "message_channel" :   "instance"  

               "terminals"      :   [
                  "terminal_id" : "......"
                  "target_id"   : "......"
                  "settings"    : "enum"   <--- (absolutely_public, 
                                              only_for_project_members, 
                                              only_for_project_members_and_imitated_emails) 
               ]
          }     
                      
     Response:
           {
               "status"         :   "success | error"
               "message_id"      :   "same_uuid_string"  
                                             
               "error_code"     :   414 (Int)  (Only if status is error)   
           }             
           
           

#### [4] Get Instance Status ####
Nahrazuje Ping a synchro s online stavem a instance exist (Podle Error codu)
    
    Request: 
    
          {
               "message_type"    :   "instance_status"              
               "message_id"      :   "some_uuid_string"
               "message_channel" :   "instance"  
               
               "instance_ids"   :   ["id_1", "id_2"] 
          }   
     
    Response:
    
          {
              "status"         :   "success | error"
              "error_code"     :   414 (Int)  (Only if status is error)  <--- Totální selhání odpovědi 
              "instance_list"   : [
                  {
                    "instance_id"   : "............"
                    "online_status" : boolean  
                    "error_code"    : 123 (Integer)    <-- Instnace neexistuje, atd..
                  }
              ]
          
          }
             


### Message from Homer for Tyrion ###  

#### [1] token_grid_verification ####
         
    Request: 
    
        {
           "message_type"    :   "instance_token_grid_verification"              
           "message_id"      :   "some_uuid_string"
           "message_channel" :   "instance"  
            
            
           "token"          :  "string"
           "instance_id:    :  "............" 
        }
        
    Response:
    
        {
           ""
        }
        
#### [2] token_webView_verification ####
        
    Request:  
         
         {
            "message_type"    :  "instance_token_webView_verification"              
            "token"          :  "string"
            "message_id"      :  "somer_uuid_string"
            "message_channel" :  "channel_name"
            "instanceId:     :  "instanceId"  
         }
        
    Response:
    
        {
           ""
        }

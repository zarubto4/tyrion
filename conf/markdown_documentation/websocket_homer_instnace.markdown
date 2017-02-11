
## Tyrion <-> Homer-Instance ##

Channel Code:: **tyrion**

**List of Json Objects for Comunication between Tyrion and Homer Instance**



### Tyrion Incoming ###  

 - deviceConnected  (Information about connection device throw Master-Device)
        
        Response: Not Required 
        
        {
           "messageType"  :  "deviceConnected" !!!!!!!!!!
           
            "deviceId"                      : "string"
            "online"                        : boolean 
            "firmware_version_core"         : "string"
            "firmware_version_mbed"         : "string"
            "firmware_version_lib"          : "string"
            "firmware_build_id"             : "string"   // Číslo Buildu
            "firmware_build_datetime"       : "string"   // Kdy bylo vybylděno
                         
            "bootloader_version_core"       : "string"
            "bootloader_version_mbed"       : "string"
            "bootloader_build_id"           : "string"
            "bootloader_build_datetime"     : "string"
            
            "messageId"      :  "somer_uuid_string"
            "messageChannel" :  "channel_name"
            "instanceId:   :  "instanceId"  
        }
        
 - yodaConnected (Yoda is connected)
         
        Response: Not Required 
        
         {
             "messageType"  :  "yodaConnected" !!!!!!!!!!
        
             "deviceId"                      : "string"
             "online"                        : boolean 
             "firmware_version_core"         : "string"
             "firmware_version_mbed"         : "string"
             "firmware_version_lib"          : "string"
             "firmware_build_id"             : "string"   // Číslo Buildu
             "firmware_build_datetime"       : "string"   // Kdy bylo vybylděno
                                 
             "bootloader_version_core"       : "string"
             "bootloader_version_mbed"       : "string"
             "bootloader_build_id"           : "string"
             "bootloader_build_datetime"     : "string"
             
             "devices_summary" : [
                {
                    "deviceId"                      : "string"
                    "online"                        : boolean 
                    "firmware_version_core"         : "string"
                    "firmware_version_mbed"         : "string"
                    "firmware_version_lib"          : "string"
                    "firmware_build_id"             : "string"   // Číslo Buildu
                    "firmware_build_datetime"       : "string"   // Kdy bylo vybylděno
                                      
                    "bootloader_version_core"       : "string"
                    "bootloader_version_mbed"       : "string"
                    "bootloader_build_id"           : "string"
                    "bootloader_build_datetime"     : "string"
                }
             ]
                    
             "messageId"      :  "somer_uuid_string"
             "messageChannel" :  "channel_name"
             "instanceId:     :  "instanceId"  
         }
        
 - yodaDisconnected (Yoda is disconnected)
  
        Response: Not Required 
        {
            "messageType"    :  "yodaDisconnected"              
            "deviceId"       :  "string"
            "messageId"      :  "somer_uuid_string"
            "messageChannel" :  "channel_name"
            "instanceId:     :  "instanceId"  
        }
        
 - instanceSummary  (All details about instance)
         
        Response: Not Required 
      
        {
          "messageType"    :  "yodaDisconnected"              
          "deviceId"       :  "string"
          "messageId"      :  "somer_uuid_string"
          "messageChannel" :  "channel_name"
          "instanceId:     :  "instanceId"  
        }
        
 - token_grid_verification
         
        Response: Required 
        {
           "messageType"    :  "token_grid_verification"              
           "token"          :  "string"
           "messageId"      :  "somer_uuid_string"
           "messageChannel" :  "channel_name"
           "instanceId:     :  "instanceId"  
        }
        
 - token_webView_verification
        
         Response: Required 
         {
            "messageType"    :  "token_webView_verification"              
            "token"          :  "string"
            "messageId"      :  "somer_uuid_string"
            "messageChannel" :  "channel_name"
            "instanceId:     :  "instanceId"  
         }
        



## Tyrion <-> Homer-Server ##

Channel Code:: **homer-server**

**List of Json Objects for Comunication between Tyrion and Homer Instance**

Any incoming or outgoing message to Tyrion and from Tyrion contains

        {
            "messageType"    :  "nazev_zpravy"              
            "messageId"      :  "some_uuid_string"
            "messageChannel" :  "channel_name"  
        }
        
 - yoda_unauthorized_logging 
 - checkUserPermission
 - checkPersonToken
 - removePersonLoginToken
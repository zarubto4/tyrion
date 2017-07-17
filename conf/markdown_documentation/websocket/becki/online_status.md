
# Změna online stavu  #

Online_change_status je JSON zpráva, která mění pouze online stav zařízení, instnace, serveru atd...
Úkolem Becki je tyto stavy měnit. Jelikož Nestačí jen boolean stav - je zaveden Enum. 


     { 
            "messageType"       :   "becki_object_update",
            "message_id"        :   "1",                      
            "message_channel"   :   "becki",
         
            "model"             :   "CProgram",
            "model_id"          :   "id",
            "online_state"      :   "ENUM", 
            
     }
     
     
####  Seznam objektů, které mohou přijít skrz Online change status(websocket) #### 
  
   * Board
   * HomerInstance
   * HomerServer

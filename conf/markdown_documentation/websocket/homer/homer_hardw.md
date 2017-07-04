
## Tyrion <-> Homer-Instance ##

Channel Code:: **hardware**

**List of Json Objects for Communication between Tyrion and each Hardware (Device)**

 - Any incoming or outgoing message to Tyrion and from Tyrion contains

        {
            "message_type"    :  "message type"
            "message_Id"      :  "some_uuid_string"
            "message_channel" :  "channel_name"  <-- In this case "hardware"

            "device_id":     :  "123423567854367436"   <-- Full Hardware ID
        }
 - We have also 2 kind of Commands - "Singleton" for only one device or Multiple Command for List od device
   or with list of Independent Commands.

 - You are sure that the name of the message_channel starts with the prefix "hardware_"

 - All Hardware are in Flat structure



### Tyrion Commands for Homer ###

#### [1] Relocate Device (Multiple Command) ####

 - Pokud po diskuzi není device online a nemá úkoly, homer ho vymaže a šetří RAM

 - Mazání Zařízení teoreticky neexistuje - Pouze Relokace. Tyrion zašle infomraci o nutnosti relokovat device
   na Homer server. Ten z pohledu Tyriona device odstraní ale stále si objekt drží dokud se device nepřipojí a
   neoznámí mu aby se přepojit na jiný server. Pokud to device udělá - pak teprve objekt smaže. (Na jiném serveru Tyrion už přidal device a čeká až se připojí)
   Tyrion si poradí i s tím, když se device připojí na jiný server než by měl.
   Pokud je device online - Homer to udělá okamžitě.

   Pokud vydá Tyrion příkaz k relocate - chápe ho jako smazaný z Konkrétního server


     Result (Ok)
     
        {
         "message_type"    :  "hardware_change_server"
         "message_Id"      :  "some_uuid_string"
         "message_channel" :  "hardware"
    
         "main_server_url"  :  "........."
         "mqtt_port"        :  "........."
         "mqtt_password"    :  "........."
         "mqtt_user_name"   :  "........."
         "device_ids"       : ["id", "id_2" ..]
        }


#### [2] Get Online Status (Multiple Command) ####

    Request:

    {
         "message_type"     :  "hardware_online_state"
         "message_Id"       :  "some_uuid_string"
         "message_channel"  :  "hardware"
         "device_ids"       :  ["id", "id_2" ..]
    }

    Response:

    {
        "status"         :   "success | error"
        "error_code"     :   414 (Int) 
        "device_list"    : [
                                {
                                    "device_id"     : "........"
                                    "online_status" : TRUE || FALSE
                                }
                            ]
    }


#### [3] Get OverView (Multiple Command) ####
(Hardware Info)

    Request:

     {
         "message_type"     :  "hardware_online_state"
         "message_Id"       :  "some_uuid_string"
         "message_channel"  :  "hardware"
         "device_ids"       : ["id", "id_2" ..]
     }

     Response:

     {
        "status"         :   "success | error"
        "error_code"     :   414 (Int) 
        "device_list" : [
          {
             "device_id"            : "........"
             "instance_id"          : "........" Optional Value
             "firmware_build_id"    : "........"
             "backup_build_id"      : "........"
             "bootloader_build_id"  : "........"
             "interface_name"       : "........"
             "online_status"        : TRUE || FALSE
             "auto_backup"          : TRUE || FALSE
          }
        ]
     }


#### [4] Set Parameters (Multiple Command) ####

 Alias (Set Alias)
 
     Request:
     {
         "message_type"    :  "hardware_set_alias"
         "message_Id"      :  "some_uuid_string"
         "message_channel" :  "hardware"
         "device_pairs"    : [
                                {
                                    "device_id" : "........."
                                    "hardware_alias" : "........."
                                }
                             ]
     }

  AutoBackup 
  (Přepíše hardware pokud má Statický backup)

     Request: 
     
     {
         "message_type"    :  "hardware_set_autobackup"
         "message_Id"      :  "some_uuid_string"
         "message_channel" :  "hardware"
         "device_ids"      : ["id", "id_2" ..]
     }



#### [5] Ping Device ####


    Request:
         {
             "message_type"    :  "hardware_set_alias"
             "message_Id"      :  "some_uuid_string"
             "message_channel" :  "hardware"
             "device_pairs"    : [
                                    {
                                        "device_id" : "........."
                                        "hardware_alias" : "........."
                                    }
    
                                 ]
         }
         
     Result:
          {
              "status"          :   "success | error"
              "error_code"      :   414 (Int) 
              "message_channel" :   "hardware"
              "response_time"   :   123 (ms)  (Only if status is success)   
          }    


#### Update Device (Multiple Command) ####

 - Update Device (Firmware, Bootloader, Backup)
   Command is send and its required do that immediately (ass soon as possible)

   Komentář pro vývoj: Očekává se, že Homer si každou proceduru zařadí do zásobníku v RAM a pokusí se každou proceduru splnit.
   Asynchroně každá procedura oznamuje sama za sebe v jakém je stavu (Tyrion to v Cache interpretuje uivateli)
   Což znamená že na začátku když Tyrion pošle Homerovi procedu, kteoru nastaví na stav " Pending" (Viz Enum Stavy)
   Homer pokud jí začne provádět zašle tyrionovi "in_progress",
   Pokud je device offline - zašle "waiting_for_device" atd...
   Pokud dostane Homer nový update - Ten předchozí zahazuje a nahrazuje ho novým (Vždy platí ten poslední zaslaný)
   Může se stát v návaznosti na rozhození vláken na začátku, že Tyrion tu samou proceduru k updatu pošle dvakrát
   (Jednou dosynchronizuje Hardware - Pošle mu update proceduru, podruhé se dosynchronizuje instance a pošle to samé do jakého
   stavu chce dostat hardware.

   Je povolené mít jen jednu update proceduru v zásobníku stejného typu.


    Response: (ok)

    {
       "message_channel" :  "hardware"
       "message_type" :  "hardware_update_execution"
       "update_task" : [
            {
                "collection_tracking_id" : "........."
                "tracking_id"   : "........."
                "device_id"                  : "........."
                "progress_subscribe"         : TRUE || FALSE
                "firmware_type"              : "........." (FIRMWARE, BOOTLOADER, BACKUP, WIFI)
                "build_id"                   : "........." (For Version its Bootloader Version identificator for example
                "blob_link"                  : "........." (Link for download file)
                "program_name"               : "........." (Optional - can be null - Its a name for identification program in Bootloader)
                "program_version_name"       : "........." (Optional - can be null - Its a name for identification program in Bootloader)
            }
       ]
    }



### Tyrion Incoming ###
Message From Homer to Tyrion About Hardware.

#### Device Status Change ####

 Device_connected  (Information about connection device throw Master-Device)

        Response: Not Required

        {
            "message_channel" :  "hardware"
            "message_type" :  "hardware_connected"
            "device_id" : "............."
        }

 Device_disconnected

        Response: Not Required

        {
          "message_channel"  :  "hardware"
          "message_type"     :  "hardware_disconnected"
          "device_id"       : "............."
        }

#### Backup Information ####
If Device Make autobackup on some version - Device this information send to Homer. It required resend it to Tyrion.

 Komentář pro vývoj: Device po 30 až 5 minutách zazálohuje program - Homer musí vědět co za Backup na devicu běží
 z důvodů toho aby poznal kritické zhroucení programu a naběhnutí na zálohu - o tomto musí také Tyriona Informovat.
 Ten označí verzi Firmwaru za kritickou a potencionálně nebezpečnou.

 Lze zasílat jak u Backupu auto, tak i u staticky udělaného (tyrion hledá i anomalie, když se HW dostane jinam než by měl
 Tyrion pak Homerovi posílá korekce.


        {
           "message_channel"  :  "hardware"
           "message_type"     :  "hardware_autobackup_maked"
           "device_id"       :  "............."
           "build_id"        :  "............."     <-- Firmware backupu
        }



#### Update Procedure State ####
Status Change on Update Procedure

  - Information about Progress

         Response: Not Required
         {
           "message_channel" :  "hardware"
           "message_type" :  "hardware_connected"
           "status" : "............."
           "device_id"
         }

  - Information about update State
    Komentář pro vývoj: Homer každou změnu nad Update procedurou reportuje Tyrionovi. Což znamená každá změna stavu.


          Response: Not Required
          {
            "message_channel" :  "hardware"
            "message_type" :  "update_hardware_status"
            "tracking_id" : "............."
            "collection_tracking_id" : "............."
            "update_state" : ".....ENUM..." [IN_PROGRESS, ]
            "device_id" : "............."
          }

          Enum status na které umí reagovat Tyrion a propisuje je do Cache Databáze:
                IN_PROGRESS,                // Homer Updatuje device
                SUCCESSFULLY_UPDATE,        // Homer úspěšně updatovat na požadovný stav
                DEVICE_WAS_OFFLINE,         // Device je offline a homer na něj čeká
                INVALID_DEVICE_STATE,       // Chyby dle dokumentace
                UPDATE_PROGRESS_STACK,      // Device se během přenosu zaseknul nebo přenos selhal - Homer by to měl zkusit víckárt než Tyrionovi oznámi
                DEVICE_NOT_RECONNECTED,     // Device se po restartu na novou verzi nepřihlásil
                DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION, (Chyba napříkad device naběhl z backupu - firmware je nestabilní)
                ERROR;                      // NEpopsaná chyba - Zpráva obsahuuje error_code


  - Informace o Progressu

      V případě že přišel pokyn od Tyriona o důkladném streamu updatu - Homer zasílá podrobnější informace

      Response: Not Required
      {
         "message_channel"               : "hardware"
         "message_type"                  : "update_hardware_progress"
         "c_program_update_plan_id"     : "............."
         "actualization_procedure_id"   : "............."
         "type_of_progress"             : ".....ENUM..."    [MAKING_BACKUP, TRANSFER_DATA_TO_DEVICE, CHECKING_RESULT]
         "percentage_progress"          : 12                // Integer 0-100
         "device_id"                    : "............."
      }

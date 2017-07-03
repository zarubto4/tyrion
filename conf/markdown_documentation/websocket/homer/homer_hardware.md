
## Tyrion <-> Homer-Instance ##

Channel Code:: **hardware**

**List of Json Objects for Communication between Tyrion and each Hardware (Device)**

 - Any incoming or outgoing message to Tyrion and from Tyrion contains

        {
            "messageType"    :  "message type"
            "messageId"      :  "some_uuid_string"
            "messageChannel" :  "channel_name"  <-- In this case "hardware"

            "device_id":     :  "123423567854367436"   <-- Full Hardware ID
        }
 - We have also 2 kind of Commands - "Singleton" for only one device or Multiple Command for List od device
   or with list of Independent Commands.

 - You are sure that the name of the messageChannel starts with the prefix "hardware_"



### Tyrion Commands for Homer ###

#### ADD Device (Multiple Command) ####

  Result (Ok)
 - Add Device
     {
        "messageType"    :  "hardware_add"
        "messageId"      :  "some_uuid_string"
        "messageChannel" :  "hardware"
        "device_ids" : ["id", "id_2" ..]
     }

#### Relocate Device (Multiple Command) ####

 - Mazání Zařízení teoreticky neexistuje - Pouze Relokace. Tyrion zašle infomraci o nutnosti relokovat device
   na Homer server. Ten z pohledu Tyriona device odstraní ale stále si objekt drží dokud se device nepřipojí a
   neoznámí mu aby se přepojit na jiný server. Pokud to device udělá - pak teprve objekt smaže. (Na jiném serveru Tyrion už přidal device a čeká až se připojí)
   Tyrion si poradí i s tím, když se device připojí na jiný server než by měl.
   Pokud je device online - Homer to udělá okamžitě.

   Pokud vydá Tyrion příkaz k relocate - chápe ho jako smazaný z Konkrétního server

    Result (Ok)
    {
     "messageType"    :  "hardware_change_server"
     "messageId"      :  "some_uuid_string"
     "messageChannel" :  "hardware"

     "main_server_url"  :  "........."
     "mqtt_port"        :  "........."
     "mqtt_password"    :  "........."
     "mqtt_user_name"   :  "........."
     "device_ids"       : ["id", "id_2" ..]
    }

#### Get Online Status (Multiple Command) ####

    Request:

    {
         "messageType"    :  "hardware_online_state"
         "messageId"      :  "some_uuid_string"
         "messageChannel" :  "hardware"
         "device_ids"       : ["id", "id_2" ..]
    }

    Response:

    {
        .....
        "device_list" : [
            {
                "device_id" : "........"
                "online_status" : TRUE || FALSE
            }
        ]
    }


#### Get OverView (Multiple Command) ####

    Request:

     {
         "messageType"    :  "hardware_online_state"
         "messageId"      :  "some_uuid_string"
         "messageChannel" :  "hardware"
         "device_ids"       : ["id", "id_2" ..]
     }

     Response:

     {
        .....
        "device_list" : [
          {
             "device_id"            : "........"
             "instance_id"          : "........" Optional Value
             "firmware_build_id"    : "........"
             "backup_build_id"      : "........"
             "bootloader_build_id"  : "........"
             "interface_name"       : "........"
             "device_id"            : "........"
             "online_status"        : TRUE || FALSE
             "auto_backup"          : TRUE || FALSE
          }
        ]
     }


#### Set Parameters (Multiple Command) ####

 - Alias
    Result: (OK)
     {
         "messageType"    :  "hardware_set_alias"
         "messageId"      :  "some_uuid_string"
         "messageChannel" :  "hardware"
         "device_pairs"   : [
            {
                "device_id" : "........."
                "hardware_alias" : "........."
            }

         ]
     }

  - Set AutoBackup : (Přepíše hardware pokud má Statický backup)

     Request: (OK)
     {
         "messageType"    :  "hardware_set_autobackup"
         "messageId"      :  "some_uuid_string"
         "messageChannel" :  "hardware"
         "device_ids"     : ["id", "id_2" ..]
     }

#### Update Device (Multiple Command) ####

 - Update Device (Firmware, Bootloader, Backup)
   Command is send and its required do that immediately (ass soon as possible)

   Komentář pro vývoj: Očekává se, že Homer si každou proceduru zařadí do zásobníku v RAM a pokusí se každou proceduru splnit.
   Asynchroně každá procedura oznamuje sama za sebe v jakém je stavu (Tyrion to v Cache interpretuje uivateli)
   Což znamená že na začátku když Tyrion pošle Homerovi proceduu je ve stavu "Pending" (Viz Enum Stavy)
   Homer pokud jí začne provádět zašle tyrionovi "in_progress",
   Pokud je device offline - zašle "waiting_for_device" atd...
   Pokud dostane Homer nový update - Ten předchozí zahazuje a nahrazuje ho novým (Vždy platí ten poslední zaslaný)
   Může se stát v návaznosti na rozhození vláken na začátku, že Tyrion tu samou proceduru k updatu pošle dvakrát
   (Jednou dosynchronizuje Hardware - Pošle mu update proceduru, podruhé se dosynchronizuje instance a pošle to samé do jakého
   stavu chce dostat hardware.

   Je povolené mít jen jednu update proceduru v zásobníku stejného typu.


    Response: (ok)

    {
       "messageChannel" :  "hardware"
       "messageType" :  "update_hardware_execution"
       "update_task" : [
            {
                "actualization_procedure_id" : "........."
                "c_program_update_plan_id"   : "........."
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

 - device_connected  (Information about connection device throw Master-Device)

        Response: Not Required

        {
            "messageChannel" :  "hardware"
            "messageType" :  "hardware_connected"
            "device_id" : "............."
        }

- device_disconnected

        Response: Not Required

        {
          "messageChannel"  :  "hardware"
          "messageType"     :  "hardware_disconnected"
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
           "messageChannel"  :  "hardware"
           "messageType"     :  "hardware_autobackup_maked"
           "device_id"       :  "............."
           "build_id"        :  "............."     <-- Firmware backupu
        }



#### Update Procedure State ####
Status Change on Update Procedure

  - Information about Progress

         Response: Not Required
         {
           "messageChannel" :  "hardware"
           "messageType" :  "hardware_connected"
           "status" : "............."
           "device_id"
         }

  - Information about update State
    Komentář pro vývoj: Homer každou změnu nad Update procedurou reportuje Tyrionovi. Což znamená každá změna stavu.


          Response: Not Required
          {
            "messageChannel" :  "hardware"
            "messageType" :  "update_hardware_status"
            "c_program_update_plan_id" : "............."
            "actualization_procedure_id" : "............."
            "update_state" : ".....ENUM..." [IN_PROGRESS, ]
            "device_id" : "............."
          }

          status:
                WAITING_IN_QUE,             // Zatím asi nepotřebný ze strany Homera - ale Tyrion dávkuje updaty po cca 100. Když toho přijde víc dává pauzy na zpracování
                IN_PROGRESS,                // Homer Updatuje device
                SUCCESSFULLY_UPDATE,        // Homer úspěšně updatovat na požadovný stav
                DEVICE_WAS_OFFLINE,         // Device je offline a homer na něj čeká
                TRANSMISSION_CRC_ERROR,     // Chyby dle dokumentace
                INVALID_DEVICE_STATE,       // Chyby dle dokumentace
                UPDATE_PROGRESS_STACK,      // Device se během přenosu zaseknul nebo přenos selhal - Homer by to měl zkusit víckárt než Tyrionovi oznámi
                DEVICE_NOT_RECONNECTED,     // Device se po restartu na novou verzi nepřihlásil
                DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION,
                ERROR;                      // NEpopsaná chyba - Zpráva obsahuuje error_code

  - Informace o Progressu

      V případě že přišel pokyn od Tyriona o důkladném streamu updatu - Homer zasílá podrobnější informace

      Response: Not Required
      {
         "messageChannel"               : "hardware"
         "messageType"                  : "update_hardware_progress"
         "c_program_update_plan_id"     : "............."
         "actualization_procedure_id"   : "............."
         "type_of_progress"             : ".....ENUM..."    [MAKING_BACKUP, TRANSFER_DATA_TO_DEVICE, CHECKING_RESULT]
         "percentage_progress"          : 12                // Integer 0-100
         "device_id"                    : "............."
      }



 - device_connected  (Information about connection device throw Master-Device)

                Response: Not Required

                {
                    "messageType"  :  "deviceConnected"

                    "device_id"                     : "string"
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
                    "instanceId:     :  "instanceId"
                }¨



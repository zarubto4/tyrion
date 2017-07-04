
## Tyrion <-> Homer ##


Spojení mezi Homer server a Tyrion Server probíhá pomocí Websocketu.
Homer server je vždy client, a Tyrion Server. 

Každá příchozí nebo odchozí zpráva na Tyrion a z Tyriona obsahuje

        {
            "message_type"    :  "nazev_zpravy"              
            "message_id"      :  "some_uuid_string"
            "message_channel" :  "channel_name"  
        }

Známe 3 kanály. 

 - "homer_server"
 - "instance"
 - "hardware"



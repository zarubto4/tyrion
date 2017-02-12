
## Tyrion <-> Homer ##


Spojení mezi Homer server a Tyrion Server probíhá pomocí Websocketu.
Homer server je vždy client, a Tyrion Server. 

Každá příchozí nebo odchozí zpráva na Tyrion a z Tyriona obsahuje

        {
            "messageType"    :  "nazev_zpravy"              
            "messageId"      :  "some_uuid_string"
            "messageChannel" :  "channel_name"  
        }


Channel Code:: **tyrion**

**List of Json Objects for Comunication between Tyrion and Homer Instance**


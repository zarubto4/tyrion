
# Notifikace #

Pokud jiný uživatel provede změny na objektu, nebo je provede Tyrion. Například u C_Programu dojde k dohotovení kompilace,
změní se tak příznak u dané verze a Tyrion zašle Beckia  všem uživatelům, kteří daný projekt mají právo číst a jsou online
informaci o updatu daného objektu. 
  
Notifikace slouží ke sdělování informací uživatelovi. To znamená že server, ke kterému je frontend připojený sděluje přihlášenému uživateli jak se například mění stav jednotlivých zařízení. Kdy proběhla konkrétní procedura. Zda se zařízení aktualizovalo. Žádosti ostatních uživatelů, například při pozvání do projektu a další.


### Odběr notifikací ###

Pro odebírání notifikací je nutné pomocí Websocketu požádat Back-End o zasílání notifikací. 
Je umožněno být s jedním uživatelem přihlášen ve více oknech. Každé takové přihlášení, 
které má vlastní websocket je naprosto nezávislé a je tedy nutné v každém spojení provádět jednotlivé úkony.

### Žádost o odběr notifikací ###

**Pro přihlášení k notifikacím je nutné zaslat Json v podobě:**

    { 
         "message_channel" : "becki",
         "message_type" : "subscribe_notification", 
         "message_id" : "your identification number"
    }

   Odpověď serveru v případě úspěchu:

    {
         "message_channel" : "becki",
         "message_type" : "subscribe_notification",
         "message_id" : " your identification number",
         "status" : "success"
    }

   Odpověď serveru v případě neúspěchu:

    {
         "message_channel" : "becki",
         "message_type" : "subscribe_notification",
         "message_id" : " your identification number",
         "status" : "error",
         "reason" : "Fault state of backend server"
    }
    
### Zastavení odběru notifikací ###


**K odhlášení odběru notifikací je nutné zaslat Json v podobě:**

    { 
         "message_channel" : "becki",
         "message_type" : "unsubscribe_notification", 
         "message_id" : "your identification number"
    }


Odpověď serveru v případě úspěchu:

    {
         "message_channel" : "becki",
         "message_type" : "unsubscribe_notification",
         "message_id" : " your identification number",
         "status" : "success"
    }

Odpověď serveru v případě neúspěchu: Velmi nepravděpodobné - Becki to cyklem může zkoušet znovu a znovu
Neúspěch je často doprovázen tím, že server ani nepovolil odběr - tedy nepoví ani zrušení odběru. 

    {
         "message_channel" : "becki",
         "message_type" : "unsubscribe_notification",
         "message_id" : " your identification number",
         "status" : "error",
         "reason" : "Fault state of backend server"
    }



------


### Vhled Notifikace ###
Notifikace lze rozdělit na jednotkové a Chain (Které se dokáží sami sebe přepisovat - například u notifikace,
 kde chceme měnit progres updatu v procentech )


#### **Jednotková Notifikace vypadá například takto:** #### 

    { 
        "message_type":  "notification",
        "id": "1",                      
        "message_channel": "becki",
        "notification_type" : "INDIVIDUAL",  <<----  Jednotková notifikace - odpočet času atd. v Becki
        "notification_level" "info",
        "notification_importance":"normal",
        "confirmation_required":"false", 
        "confirmed" : false 
        "notification_body" : [ 
            // Obsah zprávy 
        ],
        "buttons" : [ 
            // Pokud obsahuje tlačítka, jinak prázdné 
        ],
        "state" : "created", // enum Notification_state, pokud je na notifikaci nějaká změna, pošle se znovu, kvůli synchronizaci
        "was_read":"false",
        "created": "1466163478925",
        "message_id" : "číslo zprávy"
    }
  
  
**"message_type","message_channel"** a **"message_id"**  obsahují notifikace poslané pouze přes websocket!

  * "message_type" : "notification" <--- Vyžadovaná položka - Typ Notifikace (Existují "object_update" atd..) 
   

  * **notification_level**   může nabývat pouze hodnot:
  - **info**        
  (Notifikační bublina by měla být pouze upozorňující)
  - **success**     
  (Notifikační bublina by měla být pozitivní)
  - **warning**     
  (Notifikační bublina upozorňující na nestandardní stav)
  - **error**       
  (Notifikační bublina upozorňující na zásadní problém vyžadující uživatelovu pozornost)


  * **notification_importance**  může nabývat pouze hodnot:
  - **low**    
  (Notifikace se neuloží, pouze odešle přes websocket, upozorňuje například na stav zařízení nebo průběh kompilace atd.)
  - **normal** 
  (Notifikace se uloží, pozvánky do projektu atd.)
  - **high**  
  (Notifikace se uloží, důležité zprávy vyžadující potvrzení, objeví se uprostřed obrazovky, změna tarifu atd.)

  * **confirmation_required:**
  - **true** 
  (Notifikace vyžaduje potvrzení, je třeba k ní vytvořit tlačítko, kterým se potvrdí. Existuje API na potvrzení notifikace
        
        PUT {{url_Tyriona}}/notification/confirm/{notification_id}
      
  - **false** 
  (Notifikace nevyžaduje potvrzení)

  * **was_read:**
  - true (Notifikace je už přečtena, nemusí být zvýrazněna)
  - false (Notifikace je nepřečtená, je třeba ji zvýraznit, aby uživatel opticky odlišil nepřečtené notifikace 
  
  - **state** 
  TODO
  
  - **confirmed** true / false 
  (Zda už došlo k potvrzení notifikace - například Velký Alert že dochá kredit a my chceme potvrzení od uživatele, že to ví)
  
  - **created** 
  (Datum vytvoření notifikace - nikoliv datum jejího odeslání)
  
  - **buttons** 
    TODO
     
  - **notification_body** 
    TODO
    
####  **Chain Notifikace vypadá například takto:** #### 

Chain notifikace má jednak parameter určující že jde o Chain. V aktuální Becki to zanemá že u ní 
 není proveden běžný odpočet, který by jí po 5 vteřinách skryl a za druhé se u ní nemění message id.   
 Pro usnadnění programování v Tyrionovi je často využívané ID objektu, kterého se to týká v kombinaci s ID uživatele. 
 
 Například u Updatu Hardwaru se požívá Model_CProgramUpdatePlan.id + Model_Board.id 
 
 
 To co dělá z notifikace Chain je parameter 
 
      "notification_type" : ENUM 
      
 Který může nabývat 4 Enum hodnot. 
 
 - INDIVIDUAL
 (Což je předchozí typ notifikace)
 - CHAIN_START 
 (Což znanemá začátek Chain notifikací)
 - CHAIN_UPDATE
 (Překreslení předhozí notifikace se sejným ID - včetně jejího podbarvení novým obsahem )
 - CHAIN_END 
 (Ukončení řetězce notifikací - což u portálu znamená že se rozběhne časové ukončení (zakrytí) notifikace ))
    
    
## Stavba notifikací ##
 
 
 Notifikace mají vlastní syntaxi pro zobrazení ve frontendu. Skládají se z pole objektů, kde záleží na pořadí, definujících 
 jak vypadá příchozí notifikace. (Co Backend potřebuje předat uživatelovi)
 
 Tímto systémem není třeba, aby Frontend musel rozumět každé příchozí notifikaci. (Je pak na Backendu, jaké informace 
 bude uživatelovi zasílat). 
 
 
 **notification_body obsahuje definované objekty notifikací: "text", "newLine", "date", "link" a "object"**
 
 **buttons může obsahovat tlačítka pokud se v notifikaci mají nějaká zobrazit**
 
 V JSONu objektu notifikací je vždy položka "text", která obsahuje zobrazitelný text, který by měl uživatel vidět.
 

     {  
         .
         .
         "notification_body" : [
             {
                 // first object  
             },
             {
                 // sedond object
             }
             ....
         ]
         "buttons" : [
             {
                 // first button  
             },
             {
                 // sedond button
             }
             ....
         ]
     
     }

 
### Text ### 
 
 Vloží do notifikace text. Podle nastavení parametrů "bold", "italic", "underline" na true, by se měl text zvýraznit, podtrhnout atd...

     {
         "type"  : "text",
         "text"  : "Normální text",
         "color"      : "black",
         "bold"       : "false",
         "italic"     : "false",
         "underline"  : "false"
     }

### newLine ### 

 Vloží mezeru za text (odřádkování na nový řádek uvnitř noifikace)
 
      {
          "type"  : "newLine"
      }

 
### Link ### 
 
 Vloží do notifikace link do určité API v Tyrionovi, např. pro potvrzení pozvání do projektu  **Zastaralé, do Tyriona se bude odkazovat přes buttons a endpoint /notification/confirm/{notification_id}**
 
 
     {
         "type" : "link",
         "url" :  "{{url_Tyriona}}/daná/API",
         "text" : "Název linku",
         "button"     : "false",
         "color"      : "black",
         "bold"       : "false",
         "italic"     : "false",
         "underline"  : "false"
     }

 
 
### Object ### 
 
 Vloží do notifikace objekt(jeho id, jméno a název)
 
 
     {
         "type" :  "object",
         "name" : "project",  // Shodný název objektu se všemi JSON objekty z dokumentace, se kterými pracujete na REST-API 
         "id"    : "1",
         "text"  : "velkolepý project",
         "project_id" : "když je objekt v projektu",
         "color"      : "black",
         "button"     : "false",
         "bold"       : "false",
         "italic"     : "false",
         "underline"  : "false"
     }

 
####  Seznam objektů, které mohou přijít skrz notifikaci #### 
  
   * Project
   * Person
   * Version_Object (B_Program_Version, C_Program_Version)
   * BProgram
   * Board
   * CProgram
   * Product
   * HomerInstance
   * ActualizationProcedure
   * Invoice
   
###  Buttons ### 
 
 Vloží do notifikace tlačítko

     {
         "text" :  "OK",
         "action" : "confirm_notification", // enum Notification_action
         "payload"    : "1", // vrátí se na endpoint potvrzení notifikace, např. ID pozvánky
         "color"      : "black",
         "bold"       : "false",
         "italic"     : "false",
         "underline"  : "false"
     }

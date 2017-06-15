
## Tyrion <-> Becki ##

Spojení mezi Becki (Uživatelskou aplikací dále jen portál) a Tyrion Server dále jen server probíhá pomocí Websocketu 
a Rest-api zdokumentovaného zvlášt ve Swaggeru.

**Becki je vždy client, a Tyrion je vždy Server.** 

Základním úkolem websocketu je přenos notifikací k uživatelovi  a upozornování na změny na objektech. 
Tyrion dokáže synchronizovat odesílání notifikací i na více přihlášených portálů jednoho uživatele 
a to zabalením objektu **WS_Becki_Website**, která se chová navenek jako běžná websocket třída, 
ale obsahuje seznam přihlášených WS_Becki_Single_Connection, jež drží fyzické websocket spojení. 
  
  
  ### Notifikace ###
  **Aby Mohl Websocket klient odebírat notifikace je nutné aby o to požádal (Přihlásil se k odběru)!** 
  
  Pro Více informací o typu a způsobu používání notifikací viz záložka notifkací 
  
  
  ### Object Update (Aktualizace obsahu) ###
  Pokud jiný uživatel provede změny na objektu, nebo je provede Tyrion. Například u C_Programu dojde k dohotovení kompilace,
  změní se tak příznak u dané verze a Tyrion zašle Beckia  všem uživatelům, kteří daný projekt mají právo číst a jsou online
  informaci o updatu daného objektu. 
  
  Vice informací v záložce Object Update
  

  ### Change Online Status  ###
  Jelikož je velmi variabilní to jak se Devices přihlašují a odpoují zasíláme o tom notifikace. Ale není to trvalé řešení. 
  Notifikace o tom zda se device připojil jsou řízeny parametrem u devicu zda jde o vývojářský kit u kterého je informování 
  každého spojení žádoucí. U hardwaru v produkčních kolekcí by to bylo neudržitelné a tak se synchronizuje ouze online 
  stav pomocí websocketu. 
  
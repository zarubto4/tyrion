
Balíček HoumerComunication má firemní programovou struktůru

#config
     1) Konfigurační třídy, pomocné třídy, bez logiky a bez návaznosti a provázanosti s ostatními balíčky!

#controllerPackage
---->1) OutsideCommunicationPackageController - Třída dědící Interface common.generalInterfaces.PackageIntegrator daný programovou struktůrou.
                                              - Třída jež jedinná z balíčku může mít návaznost na Routy
                                              - Každý balíček má hlavní třídu, která je interfejsovaná common.generalInterfaces.PackageIntegrator
                                              - Tato třída umí přijmou zprávu a každý vývojář balíčku se tak může zcela
                                                sám rozhodovat jak bude jednotlivé příchozí zprávy členit.
                                                Viz podoba "Komunikační packet"

#controllers
---->1) Distributor - Třída jež separuje jednotlivé příchozý zprávy z houmra a rozhazuje je do příslušných balíčků
                      tak aby bylo co nejméně cuplingu!

     2) ThreadMaster - je třída obsuhující pravidelnou komunikaci s Houmry.
                     - Třída udržuje v HashMapě také správně připojená zařízení. Po ztrátě připojení uloí do databáze log.
                     - Třída také zprostředkovává přeposílání zpráv do daného zařízení kdy klíč HashMapy je macAdresa zařízení.

     3) WebSocketServer - Třída pro WebSocket (Nezajímavé)


#incomingMessage
    ?) Každý objekt může představovat objekt, který se vykástí z příchozího JSON z "Komunikační packet"
       Objekt může obsahovat celou řadu metod - je vykonána ta, která je určena z komunikačního paketu



#messageHolder
    ?) Statické objekty nebo metody - vždy void!!! - které vyvoláním provedou konkrétní operaci. (Vemou data,
       zabalí je a odešlou OutsideCommunicationPackageController.sendMessage - záleží pak jestli mám jen macAdresu
       nebo přímo držím Websocket<Out> a odpovídám okamžitě zpět.

models_ORM
    ?) Databázové objekty typu @Entity a také extendované třídou Model.

models_JSON
    ?) Pomocné objekty kástěné na nebo do JSON


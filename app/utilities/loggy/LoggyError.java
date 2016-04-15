package utilities.loggy;


// TODO dej to do databáze - a zároveň do souboru  - tam bude bug trace se všema píčovinama okolo
public class LoggyError {

    public String hash_identification = "sadas"; // nebo ID - to je fuk - ale měl by si podle tohohle "ID" umět dohledat hledáním v souboru konrkétní zaznamenanou chybu

    public String description;  // sem dáš všechno co lze zobrazit (ten tvůj složený StringBuilder)

    public String url = "dasdas"; // URL na chybu pokud je nahraná na youtrack

}

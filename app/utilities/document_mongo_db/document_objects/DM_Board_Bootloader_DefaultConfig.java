package utilities.document_mongo_db.document_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.beans.Transient;

public class DM_Board_Bootloader_DefaultConfig {

    public DM_Board_Bootloader_DefaultConfig() {}

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean autobackup;   // Default 0          // user configurable ( 0 or 1)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean blreport;     // Default 0          // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean wdenable;     // Default 1          // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String  netsource;    // Default ethernet   // user configurable ( 0 or 1)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean webview;      // Default  1         // user configurable via Bootloader & Portal ( 0 or 1)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer webport;      // Default  80        // user configurable via Bootloader & Portal ( 80 - 9999)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer timeoffset;   // Default  0         // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean timesync;     // Default  1         // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean lowpanbr;     // Default  0         // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer autojump;     // Default  0         // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer wdtime;       // Default 30         // user configurable

    @JsonIgnore @Transient
    public static DM_Board_Bootloader_DefaultConfig generateConfig() {

        DM_Board_Bootloader_DefaultConfig configuration = new DM_Board_Bootloader_DefaultConfig();
        configuration.autobackup = true;        // command: autobackup=0          (1/0)                          Funkce, která zajišťuje, že při nahrátí nové binárky se stará zálohuje. Tato hodnota je ynchronizovaná přímo s DB objektem v Model_Board
        configuration.blreport = false;         // command: blreport=0            (1/0)                          Bootloader report; zap nebo vyp textu do konzole, který píše
        configuration.wdenable = false;         // command: wdenable=0            (1/0)                          Watchdog enable; zap nebo vyp
        configuration.netsource = "ethernet";   // command: netsource=NETSOURCE_ETHERNET  (String)               Zdroj internetu pro zařízení
        configuration.webview = true;           // command: webview=1              (1/0)                         Zapnutí nebo vypnutí webového rozhraní
        configuration.webport = 80;             // command: webport=80             (počet sekund 32b)            Port na kterém běží stránka
        configuration.wdtime = 30;              // command: wdtime=1               (počet sekund od 0 do 32b)    Nastavení periody resetu watchdogu
        configuration.timeoffset = 0;           // command: timeoffset=0           (po vteřinách - takže hodina  3600 povolené je i munus hodnota)  Slouží pro lokalizovanou práci s časem. Nastavení offsetu lokálního času od UTC času.
        configuration.timesync = true;          // command: timesync=1             (1/0)                         Zapnutí synchronizace času s Homerem - Homer čas přidělí - Hardware to buď ignoruje - nebo to uloží
        configuration.lowpanbr = false;         // command: lowpanbr=1             (1/0)                         Zap nebo vyp funkce [[feature:lowpanbr|lowpan border router]]
        configuration.autojump = 300;           // command: autojump=300           (počet sekund od 0 do 32b)

        return configuration;
    }
}
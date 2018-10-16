package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Block;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Status;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Unblock;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_DataGram;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GSM", description = "Model of GSM")
@Table(name="gsm")
public class Model_GSM extends TaggedModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GSM.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    public Long msi_number;
    public String provider; // Sem ukládáme kdo dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)
    @JsonIgnore public UUID registration_hash; // Sem ukládáme kro dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)


    @JsonIgnore @Column(columnDefinition = "TEXT")
    public String json_history; // Sem si ukládáme dodatečné informace, třeba kdy jsme provedli billing

    public boolean blocked;

    /* Konfigurace SIMkarty - je nutné vždy spárovat se službou ThingsMobile  */
    public Long    daily_traffic_threshold;                     // Přípustná hodnota v KB
    public boolean daily_traffic_threshold_exceeded_limit;      // Umožnit překročit limit
    public boolean daily_traffic_threshold_notify_type;         // Zákazník bude informován o překročení

    public Long    monthly_traffic_threshold;                   // Přípustná hodnota v KB
    public boolean monthly_traffic_threshold_exceeded_limit;    // Umožnit překročit limit
    public boolean monthly_traffic_threshold_notify_type;       // Zákazník bude informován o překročení

    public Long    total_traffic_threshold;                     // Přípustná hodnota v KB
    public boolean total_traffic_threshold_exceeded_limit;      // Umožnit překročit limit
    public boolean total_traffic_threshold_notify_type;         // Zákazník bude informován o překročení

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient
    @ApiModelProperty(dataType = "utilities.gsm_services.things_mobile.statistic_class.DataSim_overview")
    public JsonNode data_overview() {
        try {

            if(json_history != null) {
                return Json.parse(this.json_history);
            }

            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_project_id() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, Model_Project.find.query().where().eq("gsm.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("gsm.id", id).findOne();
    }

    @JsonIgnore
    public DataSim_overview get_dataSim_overview() {
        try {

            if(json_history != null) {
                DataSim_overview overview = formFromJsonWithValidation(DataSim_overview.class, Json.parse(this.json_history));
                return overview;
            }

            // Mus9m naj9t a ypracovat a pak vr8tit



        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
        return null;
    }


    public DataSim_overview louskani() {

        // Objekt do ktereho vsechnoo ulozim - vsech 12 mesicu
        DataSim_overview overview = new DataSim_overview();

        // Status kde mám vsechny crc teto simkary
        TM_Sim_Status status = Controller_Things_Mobile.sim_status(msi_number);
        DateTimeFormatter day_formater = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        int pointer_to_cdr_array = 0;

        // For cyckle ktery projde vsechny crc a separuje je podle mesicu
        for (int month_selector = 1; month_selector <= 12; month_selector++) {

            // Datagram konkretniho mesice
            DataSim_DataGram datagram_month = new DataSim_DataGram();

            // NAjdu si aktuální mesic podle cisla for cyklu
            LocalDate date = LocalDate.of( Year.now().getValue() , Month.of(month_selector), 1);

            // Zacatek mesice (prvni minuta prvniho dne podle For Cyklu, kde naprkilad 1 = leden, 2 = unor
            LocalDate selected_month_start = date.withDayOfMonth(1);

            // Konec mesice posledni minuta posledniho dne podle For Cyklu, kde naprkilad 1 = leden, 2 = unor
            // Tady bude poslední den mesice
            LocalDate selected_month_end = date.withDayOfMonth(date.lengthOfMonth());

            // Pro kontrolu vypisu
            // System.out.println("Datamum DATE> local_start " + selected_month_start);
            // System.out.println("Datamum DATE> local_end "   + selected_month_end);

            datagram_month.to = selected_month_start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            datagram_month.from = selected_month_end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Doplnim název mesice podle enumu Moth
            datagram_month.period_name = Month.of(month_selector).name();

            //formatter, který mi poupravý datum z yyyy-MM-d HH:mm:ss na yyyy-MM-d
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d HH:mm:ss");
            datagram_month.detailed_datagram = new ArrayList<>();

            // Vytvořím Záznam pro celý měsíc - Den za dnem
            for(int d = 1; d < selected_month_start.lengthOfMonth(); d++) {

                // Záznam pro den d  - > 1.. až poslední den měsíce tedy lengthOfMonth()
                DataSim_DataGram daily_datagram = new DataSim_DataGram();
                daily_datagram.period_name = LocalDateTime.of( selected_month_start.withDayOfMonth(d), LocalTime.MIDNIGHT).format(day_formater);

                // Uložím první sekundu toho dne
                daily_datagram.from = LocalDateTime.of( selected_month_start.withDayOfMonth(d), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC).toEpochMilli();
                // A První sekundu předchozího dne
                daily_datagram.to   = LocalDateTime.of( selected_month_start.withDayOfMonth(d), LocalTime.MIDNIGHT).plusDays(1L).toInstant(ZoneOffset.UTC).toEpochMilli();

                datagram_month.detailed_datagram.add(daily_datagram);
            }

            System.out.println("status.cdrs SIZE: " + status.cdrs.size());
            System.out.println("Latest Pointer for USE: " + pointer_to_cdr_array);

            //for cyklus přes který procházím všechny Sim_status_cdrs
            // Procházím dokola celý seznam a chci vybrat jen ty které splnuji podminku ze jejich cas je mezi prvnim a poslednim dnem mesice
            // Přidám Všechny Dny toho měsíce
            int latest_used_id = pointer_to_cdr_array;
            for (int i = pointer_to_cdr_array; i < status.cdrs.size(); i++) {

                System.out.println("Actual Pointer: " + i);

                LocalDate cdr_start = LocalDate.parse(status.cdrs.get(i).cdrDateStart, formatter);
                LocalDate cdr_stop  = LocalDate.parse(status.cdrs.get(i).cdrDateStop, formatter);

                System.out.println("CDR START: " + status.cdrs.get(i).cdrDateStart + "(" + cdr_start + ")" + " END: " + status.cdrs.get(i).cdrDateStop + "(" + cdr_stop + ")");

                // Tady potřebujeme porovnat zda date start je později než date_fist
                if (cdr_stop.isBefore(selected_month_end) && cdr_start.isAfter(selected_month_start)) {

                    // Udělám Záznam do správného dne
                    datagram_month.data_consumption += status.cdrs.get(i).cdrTraffic.longValue();


                    System.out.println("Set Value for Day " + cdr_start.getDayOfMonth());
                    datagram_month.detailed_datagram.get(cdr_start.getDayOfMonth()).data_consumption += status.cdrs.get(i).cdrTraffic.longValue();

                } else {
                    System.out.println("Údaj nesplňuje podmínku pro přičtení");
                }

                // Nasel jsem udaj, ktery je ale az z pristiho mesice, tim padem jsem vycerpal vsechny moznosti aktualniho mesice a neni treba pole prochazet dal
                if(cdr_start.isAfter(selected_month_end)) {
                    System.out.println("Udaj je z jiného mesice, kterz teprve budu parssovat, neni potreba ho ještě procházet");
                    break;
                }

                latest_used_id++;
            }

            pointer_to_cdr_array = latest_used_id;
            overview.datagram.add(datagram_month);
        }

        json_history = Json.toJson(overview).toString();
        update();

        return overview;
    }

/* OPERATIONS ----------------------------------------------------------------------------------------------------------*/

    // For users - owners of SIM modules
    public void block() {
       TM_Sim_Block result = Controller_Things_Mobile.sim_block(this.msi_number);
       if (result.done) {
           this.blocked = true;
       } else {
           // Error
       }
    }

    // For users - owners of SIM modules
    public void unblock() {
        TM_Sim_Unblock result = Controller_Things_Mobile.sim_unblock(this.msi_number);
        if (result.done) {
            this.blocked = false;
        } else {
            // Error
        }
    }

    // For users - owners of SIM modules
    // TODO 3.10. Setup sim traffic threshold z PDF dokumentace
    public void set_trashholds(Long daily_traffic_threshold,   boolean daily_traffic_threshold_exceeded_limit,
                               Long monthly_traffic_threshold, boolean monthly_traffic_threshold_exceeded_limit,
                               Long total_traffic_threshold,   boolean total_traffic_threshold_exceeded_limit) {

        // https://www.thingsmobile.com/services/business- api/setupSimTrafficThreeshold
        // Pozor dokumentace říká "Block sim exceed limit (0 = false, 1 = true)"
        // takže budeš muset true převest na "1" a false na "O"

        String hokus_pokus = total_traffic_threshold_exceeded_limit ? "1" : "0";

        // TODO Controller_Things_Mobile.set_trashholds(......)
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void save() {

        // Defaultně vypnuté
        this.daily_traffic_threshold = -1L;
        this.monthly_traffic_threshold = -1L;
        this.total_traffic_threshold = -1L;

        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.GSM;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_GSM.class)
    public static CacheFinder<Model_GSM> find = new CacheFinder<>(Model_GSM.class);
}
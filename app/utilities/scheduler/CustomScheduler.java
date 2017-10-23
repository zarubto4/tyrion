package utilities.scheduler;

import models.Model_HomerInstanceRecord;
import org.quartz.*;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import utilities.Server;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.Class_Logger;
import utilities.scheduler.jobs.*;
import utilities.update_server.ServerUpdate;

import java.util.*;
import java.util.Calendar;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever;
import static org.quartz.TriggerBuilder.newTrigger;

public class CustomScheduler {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(CustomScheduler.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @Inject
    public Scheduler scheduler;

    private static CustomScheduler customScheduler;

    private void start() throws SchedulerException {
        try {

            // Nastavení schedulleru (Aktivity, která se pravidelně v časových úsecích vykonává)
            //scheduler = StdSchedulerFactory.getDefaultScheduler();

            //-------------------------

            // Klíč / identifikátor Trrigru definující, kdy se konkrétní job zapne.
            // Jednodenní Klíče
            TriggerKey every_day_key0       = TriggerKey.triggerKey("every_day_00:00"); // 0)
            TriggerKey every_day_key1       = TriggerKey.triggerKey("every_day_03:00"); // 1)
            TriggerKey every_day_key2       = TriggerKey.triggerKey("every_day_03:10"); // 2)
            TriggerKey every_day_key3       = TriggerKey.triggerKey("every_day_03:20"); // 3)
            TriggerKey every_day_key4       = TriggerKey.triggerKey("every_day_03:30"); // 4)
            TriggerKey every_day_key5       = TriggerKey.triggerKey("every_day_03:40"); // 5)
            TriggerKey every_day_key6       = TriggerKey.triggerKey("every_day_03:50"); // 6)


            // 2 a více-denní klíče
            TriggerKey every_second_day_key = TriggerKey.triggerKey("every_second_day_4:00"); //

            // Minutové - hodinové klíče
            TriggerKey every_10_min_key7 = TriggerKey.triggerKey("every_ten_minutes"); // 7)
            TriggerKey spend_credit_key = TriggerKey.triggerKey("spend_credit_key");
            TriggerKey every_minute_key2 = TriggerKey.triggerKey("every_minute2");
            TriggerKey every_minute_key = TriggerKey.triggerKey("every_minute");
            TriggerKey every_hour_key = TriggerKey.triggerKey("every_hour");

            //-------------------------

            // Spending credit period
            CronScheduleBuilder spend_credit_period;
            switch (Server.financial_spendDailyPeriod) {

                case 1: spend_credit_period = dailyAtHourAndMinute(3,15);break;
                case 2: spend_credit_period = cronSchedule("0 0 3,15 * * ?");break;
                case 3: spend_credit_period = cronSchedule("0 0 3,11,19, * * ?");break;
                case 4: spend_credit_period = cronSchedule("0 0 0,3,7,12,16,20 * * ?");break;
                case 12: spend_credit_period = cronSchedule("0 0 3/2 * * ?");break;
                case 24: spend_credit_period = cronSchedule("0 0 * * * ?");break;
                case 48: spend_credit_period = cronSchedule("0 0,30 * * * ?");break;
                default: {
                    terminal_logger.internalServerError(new Exception("Cannot start scheduler job - SpendingCredit, wrong configuration - using default '1'. " +
                            "Check the conf/application.conf file. Property Financial.{mode}.spendDailyPeriod should contain only 1-4, 12, 24 or 48."));
                    spend_credit_period = dailyAtHourAndMinute(3,15);
                    Server.financial_spendDailyPeriod = 1;
                    break;
                }
            }

            // Mažu scheduler v operační paměti po předchozí instanci - není doporučeno mít aktivní
            // slr pomáhá v případě problémů s operační pamětí - v režimu developer  je v metodě která ukončuje server třeba při buildu procedura, která vyčistí RAM
            // scheduler.clear();

            /** NÁVOD NA PSANÍ ČASOVÝCH TARGETŮ
             * !!!!
             * http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06.html
             * !!!!
             */

            // Definované Trigry
            if(!scheduler.checkExists(every_day_key1)){

                // Přidání úkolů do scheduleru

                // Přesouvání logu z tyriona do BLOB serveru
                if(Server.server_mode != Enum_Tyrion_Server_mode.developer ) {

                    terminal_logger.debug("start: Scheduling new Job - Log_Azure_Upload");
                    scheduler.scheduleJob(newJob(Job_LogAzureUpload.class).withIdentity(JobKey.jobKey("log_azure_upload")).build(),
                             newTrigger().withIdentity(every_day_key0).startNow()
                            .withSchedule(dailyAtHourAndMinute(0,0))// Spuštění každý den v 00:00 AM
                            .build()
                    );

                }

                // 1) Odstraňování starých auth-tokenů z přihlášení, které mají živostnost jen 72h
                terminal_logger.debug("start: Scheduling new Job - Old_Floating_Person_Token_Removal");
                scheduler.scheduleJob( newJob(Job_OldFloatingTokenRemoval.class).withIdentity( JobKey.jobKey("removing_old_floating_person_tokens") ).build(),
                        newTrigger().withIdentity(every_day_key1).startNow()
                                .withSchedule(dailyAtHourAndMinute(3,0))// Spuštění každý den v 03:00 AM
                                .build()
                );

                // 2) Odstraňování notifikací starších, než měsíc
                terminal_logger.debug("start: Scheduling new Job - Old_Notification_Removal");
                scheduler.scheduleJob( newJob(Job_OldNotificationRemoval.class).withIdentity( JobKey.jobKey("removing_old_notifications") ).build(),
                        newTrigger().withIdentity(every_day_key2).startNow()
                                .withSchedule(dailyAtHourAndMinute(3,10))// Spuštění každý den v 03:10 AM
                                .build()
                );

                // 3) Odstraňování nepřihlášených tokenů ze sociálních sítí, které mají živostnost jen 24h TODO - http://youtrack.byzance.cz/youtrack/issue/TYRION-501
                // terminal_logger.info("start: Scheduling new Job - Removing_Unused_Tokens");
                // scheduler.scheduleJob( newJob(Job_RemovingUnusedTokens.class).withIdentity( JobKey.jobKey("removing_unused_tokens") ).build(), every_day_3);

                // 4) Odstraňování nezvalidovaných účtů, které jsou starší, než měsíc
                terminal_logger.debug("start: Scheduling new Job - Unauthenticated_Person_Removal");
                scheduler.scheduleJob( newJob(Job_UnauthenticatedPersonRemoval.class).withIdentity( JobKey.jobKey("unauthenticated_person_removal") ).build(),
                        newTrigger().withIdentity(every_day_key4).startNow()
                                .withSchedule(dailyAtHourAndMinute(3,30))// Spuštění každý den v 03:30 AM
                                .build()
                );

                // 5) Kontrola a fakturace klientů na denní bázi
                terminal_logger.debug("start: Scheduling new Job - Spending Credit");
                scheduler.scheduleJob( newJob(Job_SpendingCredit.class).withIdentity( JobKey.jobKey("spending_credit") ).build(),
                        newTrigger().withIdentity(spend_credit_key).startNow()
                                .withSchedule(spend_credit_period)
                                .build()
                );

                // 6) Slouží ke kontrole plateb na localhostu, kam nám gopay nemůže poslat notifikace
                if(Server.server_mode == Enum_Tyrion_Server_mode.developer ) {
                    terminal_logger.debug("start: Scheduling new Job - Artificial Financial Callback");
                    scheduler.scheduleJob(newJob(Job_ArtificialFinancialCallback.class).withIdentity(JobKey.jobKey("artificial_financial_callback")).build(),
                            newTrigger().withIdentity(every_minute_key2).startNow()
                                .withSchedule(cronSchedule("10 0/1 * * * ?"))// Spuštění každou minutu
                                .build()
                    );
                }

                // 7) Kontrola zaseknutých kompilací - těch co jsou in progress déle než 5 minut.
                terminal_logger.debug("start: Scheduling new Job - Checking stuck compilations");
                scheduler.scheduleJob( newJob(Job_StuckCompilationCheck.class).withIdentity( JobKey.jobKey("stuck_compilation_check") ).build(),
                        newTrigger().withIdentity(every_10_min_key7).startNow()
                                .withSchedule(cronSchedule("17 0/10 * * * ?"))// Spuštění každých 10 minut a to v 17 vteřině každé minuty
                                .build()
                );

                // 8) Update statistiky o requestech
                terminal_logger.debug("start: Scheduling new Job - Request Stats Update");
                scheduler.scheduleJob( newJob(Job_RequestStatsUpdate.class).withIdentity( JobKey.jobKey("request_stats_update") ).build(),
                        newTrigger().withIdentity(every_hour_key).startNow()
                            .withSchedule(repeatHourlyForever())// Spuštění každou minutu
                            .build()
                );

                try {

                    List<Model_HomerInstanceRecord> records = Model_HomerInstanceRecord.find.where().gt("planed_when", new Date()).findList();

                    terminal_logger.debug("start: Scheduling new Job - Upload Blocko To Cloud for {} record(s)", records.size());

                    records.forEach(CustomScheduler::scheduleBlockoUpload);

                } catch (Exception e) {
                    terminal_logger.internalServerError(e);
                }

            }else {
                terminal_logger.warn("start: CRON (Every-Day) is in RAM yet. Be careful with that!");
            }

            // Nastartování scheduleru
            scheduler.start();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    public static void startScheduler() {
        Injector injector = Guice.createInjector(new SchedulerModule());
        customScheduler = injector.getInstance(CustomScheduler.class);
        try {
            customScheduler.start();
        } catch (SchedulerException e) {
            terminal_logger.internalServerError(e);
        }
    }

    public static void stopScheduler() {
        try {
            customScheduler.scheduler.clear();
        } catch (SchedulerException e) {
            terminal_logger.internalServerError(e);
        }
    }

    /**
     * Schedules a new job to be executed on the given date. Job will be executed only once and uploads blocko to homer.
     * @param record of instance to upload to cloud.
     */
    public static void scheduleBlockoUpload(Model_HomerInstanceRecord record) {
        try {

            String name = "upload-" + record.main_instance_history.id;

            terminal_logger.debug("scheduleJob: Scheduling new Job - {}", name);

            customScheduler.scheduler.scheduleJob(newJob(Job_UploadBlockoToCloud.class).withIdentity(JobKey.jobKey(name)).usingJobData("record_id", record.id).build(),
                    newTrigger().withIdentity(name + "-key").startNow().withSchedule(toCron(record.planed_when)).build()); // Spuštění na základě data

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    public static void scheduleUpdateServer(ServerUpdate update) {
        try {

            String name = "update-server-" + update.server + (update.identifier != null ? "-" + update.identifier : "") + "-to-version-" + update.version;

            terminal_logger.debug("scheduleJob: Scheduling new Job - {}", name);

            JobDataMap data = new JobDataMap();
            data.put("server", update.server);
            data.put("version", update.version);
            data.put("url", update.url);

            if (update.identifier != null) {
                data.put("identifier", update.identifier);
            }

            customScheduler.scheduler.scheduleJob(newJob(Job_UpdateServer.class).withIdentity(JobKey.jobKey(name)).usingJobData(data).build(),
                    newTrigger().withIdentity(name + "-key").startNow().withSchedule(toCron(new Date(update.time))).build()); // Spuštění na základě data

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    /**
     * Converts java Date to Cron schedule.
     * @param date the cron expression will be build from.
     * @return cron like schedule.
     */
    public static CronScheduleBuilder toCron(Date date){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Integer second  = calendar.get(Calendar.SECOND);
        Integer minute  = calendar.get(Calendar.MINUTE);
        Integer hour    = calendar.get(Calendar.HOUR_OF_DAY);
        Integer day     = calendar.get(Calendar.DAY_OF_MONTH);
        Integer month   = calendar.get(Calendar.MONTH) + 1; // Months starts at zero
        Integer year    = calendar.get(Calendar.YEAR);

        String cron = second.toString() + " " + minute.toString() + " " + hour.toString() + " " + day.toString() + " " + month.toString() + " ? " + year.toString();

        terminal_logger.debug("toCron: expression = {}", cron);

        return cronSchedule(cron);
    }
}
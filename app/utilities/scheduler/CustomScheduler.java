package utilities.scheduler;

import org.quartz.*;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import utilities.Server;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.independent_threads.Security_WS_token_confirm_procedure;
import utilities.logger.Class_Logger;
import utilities.scheduler.jobs.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever;
import static org.quartz.SimpleScheduleBuilder.repeatMinutelyForever;
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
            TriggerKey every_fifteen_minute_key = TriggerKey.triggerKey("every_fifteen_minutes");
            TriggerKey every_minute_key = TriggerKey.triggerKey("every_minute");
            TriggerKey every_hour_key = TriggerKey.triggerKey("every_hour");

            //-------------------------

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

                Trigger every_day_0 = newTrigger().withIdentity(every_day_key0).startNow()
                        .withSchedule(dailyAtHourAndMinute(0,0))// Spuštění každý den v 00:00 AM
                        .build();

                Trigger every_day_1 = newTrigger().withIdentity(every_day_key1).startNow()
                        .withSchedule(dailyAtHourAndMinute(3,0))// Spuštění každý den v 03:00 AM
                        .build();

                Trigger every_day_2 = newTrigger().withIdentity(every_day_key2).startNow()
                        .withSchedule(dailyAtHourAndMinute(3,10))// Spuštění každý den v 03:10 AM
                        .build();

                Trigger every_day_3 = newTrigger().withIdentity(every_day_key3).startNow()
                        .withSchedule(dailyAtHourAndMinute(3,20))// Spuštění každý den v 03:20 AM
                        .build();

                Trigger every_day_4 = newTrigger().withIdentity(every_day_key4).startNow()
                        .withSchedule(dailyAtHourAndMinute(3,30))// Spuštění každý den v 03:30 AM
                        .build();

                // TODO 5
                Trigger every_day_5 = newTrigger().withIdentity(every_day_key5).startNow()
                        .withSchedule(dailyAtHourAndMinute(3,40))// Spuštění každý den v 03:20 AM
                        .build();

                Trigger every_day_6 = newTrigger().withIdentity(every_day_key6).startNow()
                        .withSchedule(dailyAtHourAndMinute(3,50))// Spuštění každý den v 03:20 AM
                        .build();

                // TODO 6

                Trigger every_10_minutes_7 = newTrigger().withIdentity(every_10_min_key7).startNow()
                        .withSchedule(cronSchedule("17 0/10 * * * ?"))// Spuštění každých 10 minut a to v 17 vteřině každé minuty
                        .build();

                Trigger every_fifteen_minute = newTrigger().withIdentity(every_fifteen_minute_key).startNow()
                        .withSchedule(repeatMinutelyForever(15))// Spuštění každých 15 minut
                        .build();

                Trigger every_minute = newTrigger().withIdentity(every_minute_key).startNow()
                        .withSchedule(cronSchedule("30 0/1 * * * ?"))// Spuštění každou minutu
                        .build();

                Trigger every_hour = newTrigger().withIdentity(every_hour_key).startNow()
                        .withSchedule(repeatHourlyForever())// Spuštění každou minutu
                        .build();

                /**
                 *  !!!
                 *  Každý Job musí mít Trigger, který má unikátní TriggerKey
                 *  !!!
                 */

                // Přidání úkolů do scheduleru

                // 0) Přesouvání logu z tyriona do BLOB serveru
                if(Server.server_mode != Enum_Tyrion_Server_mode.developer ) {
                    terminal_logger.debug("start: Scheduling new Job - Log_Azure_Upload");
                    scheduler.scheduleJob(newJob(Job_LogAzureUpload.class).withIdentity(JobKey.jobKey("log_azure_upload")).build(), every_day_0);
                }
                // 1) Odstraňování starých auth-tokenů z přihlášení, které mají živostnost jen 72h
                terminal_logger.debug("start: Scheduling new Job - Old_Floating_Person_Token_Removal");
                scheduler.scheduleJob( newJob(Job_OldFloatingTokenRemoval.class).withIdentity( JobKey.jobKey("removing_old_floating_person_tokens") ).build(), every_day_1);

                // 2) Odstraňování notifikací starších, než měsíc
                terminal_logger.debug("start: Scheduling new Job - Old_Notification_Removal");
                scheduler.scheduleJob( newJob(Job_OldNotificationRemoval.class).withIdentity( JobKey.jobKey("removing_old_notifications") ).build(), every_day_2);

                // 3) Odstraňování nepřihlášených tokenů ze sociálních sítí, které mají živostnost jen 24h
                //logger.info("start: Scheduling new Job - Removing_Unused_Tokens");
                //scheduler.scheduleJob( newJob(Job_RemovingUnusedTokens.class).withIdentity( JobKey.jobKey("removing_unused_tokens") ).build(), every_day_3);

                // 4) Odstraňování nezvalidovaných účtů, které jsou starší, než měsíc
                terminal_logger.debug("start: Scheduling new Job - Unauthenticated_Person_Removal");
                scheduler.scheduleJob( newJob(Job_UnauthenticatedPersonRemoval.class).withIdentity( JobKey.jobKey("unauthenticated_person_removal") ).build(), every_day_4);

                // 5) Kontrola a fakturace klientů na měsíční bázi
                terminal_logger.debug("start: Scheduling new Job - Sending_Invoices");
                scheduler.scheduleJob( newJob(Job_SpendingCredit.class).withIdentity( JobKey.jobKey("sending_invoices") ).build(), every_day_5);

                // 6) Obnovení certifikátu od Lets Encrypt
                if(Server.server_mode != Enum_Tyrion_Server_mode.production ) {
                    terminal_logger.debug("start: Scheduling new Job - Certificate_Renewal");
                    scheduler.scheduleJob(newJob(Job_CertificateRenewal.class).withIdentity(JobKey.jobKey("certificate_renewal")).build(), every_day_6);
                }
                // 7) Kontrola zaseknutých kompilací - těch co jsou in progress déle než 5 minut.
                terminal_logger.debug("start: Scheduling new Job - Checking stuck compilations");
                scheduler.scheduleJob( newJob(Job_StuckCompilationCheck.class).withIdentity( JobKey.jobKey("stuck_compilation_check") ).build(), every_10_minutes_7);

                // 8) Update statistiky o requestech
                terminal_logger.debug("start: Scheduling new Job - Request Stats Update");
                scheduler.scheduleJob( newJob(Job_RequestStatsUpdate.class).withIdentity( JobKey.jobKey("request_stats_update") ).build(), every_hour);

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
}
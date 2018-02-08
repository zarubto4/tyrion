package utilities.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.Model_UpdateProcedure;
import models.Model_InstanceSnapshot;
import org.quartz.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import utilities.Server;
import utilities.logger.Logger;
import utilities.scheduler.jobs.*;
import utilities.update_server.ServerUpdate;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever;
import static org.quartz.TriggerBuilder.newTrigger;

@Singleton
public class SchedulerController {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(SchedulerController.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @Inject
    public SchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private Scheduler scheduler;

    /**
     * Method looks into utilities.scheduler.jobs package and finds all classes
     * annotated with {@link Scheduled} and schedules the jobs based on config
     * that was stored in the annotation.
     */
    @SuppressWarnings("unchecked")
    public void start() {
        try {

            logger.info("start - scheduling jobs");

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("utilities.scheduler.jobs"))
                    .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));

            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Scheduled.class);

            classes.forEach(cls -> {

                Class<? extends Job> job = (Class<? extends Job>) cls; // Cast to job

                Scheduled annotation = job.getAnnotation(Scheduled.class);
                Restrict restrict = job.getAnnotation(Restrict.class);
                if (restrict != null && Server.mode == restrict.value()) return; // If this job is restricted in this mode skip the scheduling

                String value = annotation.value();

                logger.debug("start - scheduling job: '{}' with schedule: '{}'", job.getSimpleName(), value);

                try {

                    JobKey jobKey = JobKey.jobKey(job.getSimpleName() + "_JobKey");

                    if (this.scheduler.checkExists(jobKey)) {
                        throw new SchedulerException("Job with key: " + jobKey.getName() + " already exists.");
                    }

                    this.scheduler.scheduleJob(newJob(job).withIdentity(jobKey).build(),
                            newTrigger().withIdentity(TriggerKey.triggerKey(job.getSimpleName() + "_TriggerKey")).startNow()
                                    .withSchedule(cronSchedule(value))
                                    .build());

                } catch (SchedulerException e) {
                    logger.internalServerError(e);
                }
            });

/*
                // TODO - je nutné zajistit i kontrolu toho co se neudělalo v historii - třeba když spadne server, tak tu hodinu zpětně je třeba odstartovat
                try {

                    //List<Model_HomerInstanceRecord> records = Model_HomerInstanceRecord.find.query().where().gt("planed_when", new Date()).findList();

                    //logger.debug("start: Scheduling new Job - Upload Blocko To Cloud for {} record(s)", records.size());

                    //records.forEach(SchedulerController::scheduleInstanceDeployment);

                } catch (Exception e) {
                    logger.internalServerError(e);
                }

                // TODO - je nutné zajistit i kontrolu toho co se neudělalo v historii - třeba když spadne server, tak tu hodinu zpětně je třeba odstartovat
                try {

                    //List<Model_ActualizationProcedure> procedures = Model_ActualizationProcedure.find.query().where().gt("date_of_planing", new Date()).findList();

                    //logger.debug("start: Scheduling new Job - Start With {} update procedures for Hardware.", procedures.size());

                    //procedures.forEach(SchedulerController::scheduleUpdateProcedure);

                } catch (Exception e) {
                    logger.internalServerError(e);
                }

            } else {
                logger.warn("start: CRON (Every-Day) is in RAM yet. Be careful with that!");
            }*/

            // Nastartování scheduleru
            this.scheduler.start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void stop() {
        try {
            this.scheduler.clear();
        } catch (SchedulerException e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Schedules a new job to be executed on the given date. Job will be executed only once and uploads blocko to homer.
     * @param snapshot of instance to upload to cloud.
     */
    public void scheduleInstanceDeployment(Model_InstanceSnapshot snapshot) {
        try {

            String name = "deploy-instance-" + snapshot.id;

            logger.debug("scheduleInstanceDeployment - scheduling new job: {}", name);

            scheduler.scheduleJob(newJob(Job_UploadBlockoToCloud.class).withIdentity(JobKey.jobKey(name)).usingJobData("snapshot_id", snapshot.id.toString()).build(),
                    newTrigger().withIdentity(name + "-key").startNow().withSchedule(toCron(snapshot.deployed)).build());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Schedules a new job to be executed on the given date. Job will be executed only once and start update procedure on all hardware.
     * @param procedure
     */
    public void scheduleUpdateProcedure(Model_UpdateProcedure procedure) {
        try {

            String name = "actualization-procedure-update-" + procedure.id.toString();

            logger.debug("scheduleUpdateProcedure - scheduling new job - {}", name);

            this.scheduler.scheduleJob(newJob(Job_StartUpdateProcedure.class).withIdentity(JobKey.jobKey(name)).usingJobData("procedure_id", procedure.id.toString()).build(),
                    newTrigger().withIdentity(name + "-key").startNow().withSchedule(toCron(procedure.date_of_planing)).build()); // Spuštění na základě data

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void scheduleUpdateServer(ServerUpdate update) {
        try {

            String name = "update-server-" + update.server + (update.identifier != null ? "-" + update.identifier : "") + "-to-version-" + update.version;

            logger.debug("scheduleJob: Scheduling new Job - {}", name);

            JobDataMap data = new JobDataMap();
            data.put("server", update.server);
            data.put("version", update.version);
            data.put("url", update.url);

            if (update.identifier != null) {
                data.put("identifier", update.identifier);
            }

            this.scheduler.scheduleJob(newJob(Job_UpdateServer.class).withIdentity(JobKey.jobKey(name)).usingJobData(data).build(),
                    newTrigger().withIdentity(name + "-key").startNow().withSchedule(toCron(new Date(update.time))).build()); // Spuštění na základě data

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Converts java Date to Cron schedule.
     * @param date the cron expression will be build from.
     * @return cron like schedule.
     */
    public static CronScheduleBuilder toCron(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Integer second  = calendar.get(Calendar.SECOND);
        Integer minute  = calendar.get(Calendar.MINUTE);
        Integer hour    = calendar.get(Calendar.HOUR_OF_DAY);
        Integer day     = calendar.get(Calendar.DAY_OF_MONTH);
        Integer month   = calendar.get(Calendar.MONTH) + 1; // Months start from zero
        Integer year    = calendar.get(Calendar.YEAR);

        String cron = second.toString() + " " + minute.toString() + " " + hour.toString() + " " + day.toString() + " " + month.toString() + " ? " + year.toString();

        logger.debug("toCron: expression = {}", cron);

        return cronSchedule(cron);
    }
}
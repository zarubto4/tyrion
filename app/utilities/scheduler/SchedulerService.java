package utilities.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.Model_InstanceSnapshot;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import play.inject.ApplicationLifecycle;
import utilities.Server;
import utilities.enums.ServerMode;
import utilities.logger.Logger;
import utilities.logger.ServerLogger;
import utilities.scheduler.jobs.*;
import utilities.update_server.ServerUpdate;

import java.util.*;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Singleton
public class SchedulerService {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(SchedulerService.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @Inject
    @SuppressWarnings("unchecked")
    public SchedulerService(Scheduler scheduler, ApplicationLifecycle appLifecycle, ServerLogger serverLogger) {
        this.scheduler = scheduler;

        appLifecycle.addStopHook(() -> {
            this.scheduler.clear();
            return CompletableFuture.completedFuture(null);
        });

        try {

            logger.info("constructor - scheduling jobs");

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("utilities.scheduler.jobs"))
                    .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));

            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Scheduled.class);

            ServerMode mode = Server.mode;

            classes.forEach(cls -> {

                Class<? extends Job> job = (Class<? extends Job>) cls; // Cast to job

                // Check Restriction
                Restrict restrict = job.getAnnotation(Restrict.class);
                if (restrict != null) {
                    List<ServerMode> arrays = new ArrayList<>(Arrays.asList(restrict.value()));
                    if(!arrays.contains(mode)) return; // If this job is restricted in this mode skip the scheduling
                }

                // Check Scheduled Anotation
                Scheduled annotation = job.getAnnotation(Scheduled.class);
                String value = annotation.value();

                logger.debug("constructor - scheduling job: '{}' with schedule: '{}'", job.getSimpleName(), value);

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

            // Nastartování scheduleru
            this.scheduler.start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public Scheduler scheduler;

    public void schedule(JobDefinition jobDefinition) {
        logger.info("schedule - scheduling new job: {}", jobDefinition.getJobKey());
        try {
            this.scheduler.scheduleJob(
                    newJob(jobDefinition.getJob()).withIdentity(jobDefinition.getJobKey()).usingJobData(new JobDataMap(jobDefinition.getDataMap())).build(),
                    newTrigger().withIdentity(jobDefinition.getJobKey()).withSchedule(jobDefinition.getSchedule()).startNow().build());
        } catch (Exception e) {
            logger.internalServerError(e);
        }

    }

    public void unschedule(String jobKey) {
        logger.info("unschedule - unscheduling job: {}", jobKey);
        try {
            this.scheduler.deleteJob(new JobKey(jobKey));
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public boolean isScheduled(String jobKey) {
        try {
            return this.scheduler.checkExists(new JobKey(jobKey));
        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }
    }

    public void show_all_jobs() throws SchedulerException {

        for (String groupName : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                //get job's trigger
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                for(Trigger trigger : triggers) {
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);
                }
            }
        }
    }

    /**
     * Return Trigger Job where jobKey_sub_part is a part of name of Scheduler Job
     * For example "update-instance-" return list of all jobs waiting for running
     *
     * @param jobKey_sub_part
     * @return
     * @throws SchedulerException
     */
    public List<Trigger> get_Job(String jobKey_sub_part) throws SchedulerException {
        List<Trigger> triggers_jobs = new ArrayList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                if(jobKey.getName().contains(jobKey_sub_part)){
                    triggers_jobs.add(scheduler.getTriggersOfJob(jobKey).get(0));
                }
            }
        }

        return triggers_jobs;
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
     */
    public void scheduleUpdateProcedure() {
        /*try {

            String name = "actualization-procedure-update-" + procedure.id.toString();

            logger.debug("scheduleUpdateProcedure - scheduling new job - {}", name);

            this.scheduler.scheduleJob(newJob(Job_StartUpdateProcedure.class).withIdentity(JobKey.jobKey(name)).usingJobData("procedure_id", procedure.id.toString()).build(),
                    newTrigger().withIdentity(name + "-key").startNow().withSchedule(toCron(procedure.date_of_planing)).build()); // Spuštění na základě data

        } catch (Exception e) {
            logger.internalServerError(e);
        }*/
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
                    newTrigger().withIdentity(name + "-key").withDescription(update.version).startNow().withSchedule(toCron(new Date(update.time))).build()); // Spuštění na základě data

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
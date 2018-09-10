package test;

import org.junit.After;
import org.junit.Before;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;

public class TyrionTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.url", "jdbc:postgresql://:5432/byzance_test");

        Application app = Helpers.fakeApplication(settings);
        return app;
    }

    @Override
    public void stopPlay() {
        cleanDatabase();
        super.stopPlay();
    }

    public void cleanDatabase() {
        Database database = app.injector().instanceOf(Database.class);
        Evolutions.cleanupEvolutions(database);
        database.shutdown();
    }
}

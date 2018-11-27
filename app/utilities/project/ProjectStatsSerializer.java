package utilities.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.inject.Inject;
import models.Model_Project;
import utilities.swagger.output.Swagger_ProjectStats;

import java.io.IOException;

public class ProjectStatsSerializer extends StdSerializer<Swagger_ProjectStats> {

    private final ProjectService projectService;

    @Inject
    public ProjectStatsSerializer(ProjectService projectService) {
        super(Swagger_ProjectStats.class);
        this.projectService = projectService;
    }

    @Override
    public void serialize(Swagger_ProjectStats value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (gen.getCurrentValue() instanceof Model_Project) {
            gen.writeObject(this.projectService.getOverview((Model_Project) gen.getCurrentValue()));
        } else {
            gen.writeNull();
        }
    }
}

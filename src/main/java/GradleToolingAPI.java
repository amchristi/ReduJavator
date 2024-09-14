import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;

public class GradleToolingAPI
{
    private final GradleConnector connector;

    public GradleToolingAPI(String inGRADLE_DIR, String inBUILD_FILE)
    {
        connector = GradleConnector.newConnector();
        connector.useInstallation(new File(inGRADLE_DIR));
        connector.forProjectDirectory(new File(inBUILD_FILE));
    }

    public int executeTask(String tasks)
    {
        int result = 1; // Make 1 so default is error
        ProjectConnection connection = connector.connect();
        BuildLauncher build = connection.newBuild();
        build.forTasks(tasks);

        try {
            build.run();
            result = 0;
        } catch (Exception e) {
            if (Main.logging) {
                e.printStackTrace();
            }
        }

        connection.close();
        return result;
    }
}

// Created by Brandon Wilber, 07/2022
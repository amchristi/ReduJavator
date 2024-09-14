import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class Execution {
    enum Tool {
        MAVEN,
        ANT,
        GRADLE
    }

    enum ExecuteType {
        COMPILE,
        TEST
    }

    Tool automationType;
    public static String xmlPath = "";

    InvocationRequest compileCheck;
    InvocationRequest testCheck;
    Invoker invoker;
    String testMethod = "";

    String antCompileTarget = "";
    String antTestTarget = "";

    String gradleCompile = "";
    String gradleTest = "";
    GradleToolingAPI gradleRunner;

    public static int timeoutDuration = 0;
    public static int testFrequencyCheck = 0;

    public Execution(String inTestMethod, Tool type, String inXMLPath, String fileName) { // Maven Specific
        automationType = type;
        testMethod = inTestMethod;

        compileCheck = new DefaultInvocationRequest();
        testCheck = new DefaultInvocationRequest();
        compileCheck.setInputStream(InputStream.nullInputStream()); // To prevent console error messages
        testCheck.setInputStream(InputStream.nullInputStream()); // To prevent console error messages
        invoker = new DefaultInvoker();

        if (System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH).contains("win")) {
            invoker.setMavenHome(new File(System.getenv().get("MAVEN_HOME")));
        }

        // This command will compile the code AND tests, but NOT run them
        String compileCommand = "-Dcompile=" + fileName + " -DskipTests -Dmaven.javadoc.skip=true";


        // This command will NOT compile, it will simply run the code/tests (Will only happen after compile success)
        String testCommand = "surefire:test -Dtest=" + fileName + " -Dmaven.javadoc.skip=true test";
        //String testCommand = "surefire:test -Dmaven.javadoc.skip=true test";

        compileCheck.setPomFile(new File(inXMLPath));
        testCheck.setPomFile(new File(inXMLPath));

        compileCheck.setGoals(Collections.singletonList(compileCommand));
        testCheck.setGoals(Collections.singletonList(testCommand));
    }

    public Execution(Tool type, String inXMLPath, String compileTarget, String testTarget) { // Ant Specific
        automationType = type;

        xmlPath = inXMLPath;
        antCompileTarget = compileTarget;
        antTestTarget = testTarget;
    }

    public Execution(Tool type, String inBUILD_FILE, String inGRADLE_DIR, String inGRADLE_COMPILE_TARGET, String inGRADLE_TEST_TARGET) { // Gradle Specific
        automationType = type;

        gradleCompile = inGRADLE_COMPILE_TARGET;
        gradleTest = inGRADLE_TEST_TARGET;
        gradleRunner = new GradleToolingAPI(inGRADLE_DIR, inBUILD_FILE);
    }

    public void setTimeOut(int inTimeOut, int inFrequencyCheck) {
        timeoutDuration = inTimeOut;
        testFrequencyCheck = inFrequencyCheck;
    }

    public int Execute(ExecuteType type) throws MavenInvocationException {
        if (type == ExecuteType.COMPILE) {
            switch (automationType) {
                case MAVEN -> {
                    InvocationResult result;

                    result = invoker.execute(compileCheck);
                    return result.getExitCode();
                }
                case ANT -> {
                    return executeAntTask(antCompileTarget) ? 0 : 1;
                }
                case GRADLE -> {
                    return gradleRunner.executeTask(gradleCompile);
                }
                default -> {
                    return 2; // Unsupported type
                }
            }
        } else {
            final int[] testResult = {2};

            Thread compile = new Thread (() -> {
                switch (automationType) {
                    case MAVEN -> {
                        InvocationResult result;
                        try {
                            result = invoker.execute(testCheck);
                            testResult[0] = result.getExitCode();
                        } catch (MavenInvocationException e) {
                            e.printStackTrace();
                        }
                    }
                    case ANT -> {
                        boolean antResult;
                        antResult = executeAntTask(antTestTarget);
                        testResult[0] = antResult ? 0 : 1;
                    }
                    case GRADLE -> testResult[0] = gradleRunner.executeTask(gradleTest);
                    default -> {
                    }
                }
            });

            compile.setDaemon(true);
            compile.start();

            try { // Maven/Gradle will use many cycles, ANT usually will only need a few
                int sleepDuration = timeoutDuration * 1_000 / testFrequencyCheck;
                for (int i = 0; i < testFrequencyCheck; i++) {
                    Thread.sleep(sleepDuration);
                    if (testResult[0] < 2) {
                        return testResult[0];
                    }
                }
            } catch (Exception e) {
                if (Main.logging) {
                    e.printStackTrace();
                }
            }

            return testResult[0];
        }
    }

    public static boolean executeAntTask(String target) {
        boolean result = false;
        String xmlFile = xmlPath + "build.xml";

        Project antProj = new Project();
        File buildFile = new File(xmlFile);
        antProj.setUserProperty("ant.file", buildFile.getAbsolutePath());

        try {
            antProj.init();
            ProjectHelper projHelper = ProjectHelper.getProjectHelper();
            antProj.addReference("ant.projectHelper", projHelper);
            projHelper.parse(antProj, buildFile);

            if (Main.logging) {
                System.out.println("<< Executing Ant Task >>");
            }
            antProj.executeTarget(target);
            result = true;
        } catch (BuildException | AssertionError e) {
            if (Main.logging) {
                e.printStackTrace();
            }
        }

        return result;
    }
}

// Created by Brandon Wilber, 06/2022

/** Java Reducer (Main)
 *                                      Java Reducer Summary
 *  --------------------------------------------------------------------------------------------------------------------
 *  This program will scan a targeted Class containing some Junit tests that fail. Multiple Classes may be queued and
 *  each class may contain multiple failing methods. Maven, Ant, and Gradle project types are supported, but require
 *  those specific frameworks to be installed on your system, IDE bundles will not be sufficient. The program will
 *  attempt to remove any code statements that are not require for that particular test method to fail. @Test cases
 *  are not required and TestSuites can be used as well.

 *  There is a file in the root directory, TEST_EXAMPLES.txt which contains several types of tests that can be run to
 *  verify the reducer is working as expected.

 *  *** NOTE: A "_backup" directory will be created at the root of every project, this will be populated with a
 *      "backup_ANYCLASSNAME.txt" copy of the original Class, to ensure there is no accidentally lost work due to
 *      the reduction of the Class. The Classes will only be backed up once, if a backup_ of a class already exists,
 *      and you want it updated to a more recent version of a Class, simply delete the backup_ file and a new one
 *      will be created ***

 *                                      Main SPECIFIC DETAILS
 *  --------------------------------------------------------------------------------------------------------------------
 *  int, TIMEOUT: The number of seconds to wait for a test before returning a fault, which is NOT seen as a test
 *      failure, This would be for ending long-running or infinite loop test situations. A value of at least 5 is
 *      recommended, lower and the test results may time out before they are returned for slower build managers
 *      (maven/gradle)

 *  int, TEST_FREQUENCY_CHECK: Busy waiting, breaks TIMEOUT into this many segments to see if the test has completed,
 *      total time spent waiting is up to TIMEOUT. The current configuration seems like a good amount of waiting from
 *      what I have tested, I would not increase it beyond what it is set to by default

 *  boolean, logging: Enables/disables the Reducer level println comments that show iteration/process, as well as
 *      exception stack traces

 *  Integer[], projects: Projects to be reduced are DEFINED in TestObject.java, specify the INDEX of projects
 *      (Classes) to be reduced in this array. More than 1 project of more than one type (Maven/Ant/Gradle) can be
 *      processed in sequence. Classes containing multiple methods to reduce are specified as such in the project
 */

public class Main {
    private static final int TIMEOUT = 20;
    private static final int TEST_FREQUENCY_CHECK = TIMEOUT * 100;
    public static boolean logging = false;

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();

        TestObject testObjects = new TestObject();
        Integer[] projects = {9};

        for (int project : projects) {
            TestData currProject = testObjects.projectList[project];
            currProject.Initialize();
            currProject.SetTimeouts(TIMEOUT, TEST_FREQUENCY_CHECK);

            System.out.println("Reducing Class: " + currProject.testClass);
            for (int i = 0; i < currProject.testMethods.size(); i++) {
                currProject.Reduce(i);
            }

            testObjects.projectList[project].Finalize();
            System.out.println("............................................................................");
        }

        double totalTime = (System.nanoTime() - start) / 1000000000.0;
        System.out.printf("%.2f seconds", totalTime);
    }
}

// Created by Brandon Wilber, 06/2022
/** Java Reducer (TestObject)
 *                                      TestData CONSTRUCTOR DETAILS
 *  --------------------------------------------------------------------------------------------------------------------
 *  TestData[] projectList = {}; Several key pieces of information are required for every project, and depending on
 *      the method being used (Maven, Ant, Gradle), additional details may be required. All of this information can
 *      be entered here and the information will be used to set up any projects selected in the Main class

 *  TestData has 3 different constructors, 1 for Maven, 1 for Ant, and 1 for Gradle. Essentially, Maven contains the
 *  core elements for every project. Ant builds off of the Maven requirements and adds 2 more. Gradle builds off of
 *  the Ant specification by adding 1 more parameter

 *  MAVEN CONSTRUCTOR: (Core)
 *      String, inIdentifier: A string name for the project, not required for anything, put whatever you want here
 *      Execution.Tool: The management method used to build the project, acceptable methods are, MAVEN/ANT/GRADLE
 *      String, inTestClass: The name of the class to be reduced
 *      String, inProjectPath: The path to the project root directory (e.g. Where pom.xml / build.xml / build.gradle
 *          are located)
 *      String, inSourcePath: The relative location from the project root to where the source class files to be reduced
 *          are located

 *      String[], new: An array containing the method name for every test failing method within the specified Class.
 *          EVERY FAILING METHOD IN THE CLASS MUST BE INCLUDED for reduction to function properly for more than the
 *          final method indicated. This is set as the LAST item for all 3 constructors, simply for looks so a
 *          potentially long list of method names can occupy their own line(s)

 *  ANT CONSTRUCTOR: (Core + 2)
 *      String, inCompileTarget: The name of the compile target in the build.xml file, compilation and testing are
 *          run as 2 separate processes
 *      String, inTestTarget: The name of the test target in the build.xml file, a Class will only be tested if it
 *          has already successfully compiled, so this target could have the compile-target as a dependency, or it
 *          can simply run the test without compiling again

 *      String[], new: NOTE, this isn't added for the ANT CONSTRUCTOR, simply moved to be the final element

 *  GRADLE CONSTRUCTOR: (ANT + 1)
 *      String, inGradleDir: The GradleToolingAPI is used to execute Gradle projects, the directory to the location
 *          where Gradle is installed on the local system is set here

 *      String[], new: NOTE, this isn't added for the GRADLE CONSTRUCTOR, simply moved to be the final element

 *                                      Personal_* Test Projects
 *  --------------------------------------------------------------------------------------------------------------------
 *  An example project for each build tool is located within this project, for Maven, Ant, and Gradle. These can all be
 *      opened and operated completely independently of this Java Reducer project. All the path locations for these
 *      projects should be relative, and they should be able to be tested out of the box as they are currently
 *      configured, the one exception being for Gradle: you will need to specify Gradle's install location. Maven will
 *      require Maven to be installed, Ant will require Ant to be installed, and Gradle will require Gradle to be
 *      installed. Bundled versions of these tools may be included with your IDE, but this will not be sufficient.
 *      A Gradle project may include a "gradlew" wrapper, but the method used here involves the GradleToolingAPI,
 *      requiring Gradle to be installed on the system.

 *  *** NOTE ABOUT PERSONAL_ANT PROJECT ***
 *  The source file being modified in this project isn't actually the /src/ java file. As part of the clean/compile
 *      task, the /src/ java file is created as a copy of a file in the /_files_/ directory. This is simply because
 *      editing the source file directly was not consistently working as expected, likely due to a small issue with
 *      a parameter in my build.xml file. This may be investigated later, but is not a top priority as it pertains
 *      to issue with an external program, and not the reducer itself.
 */

public class TestObject {
    TestData[] projectList = {
        // 0
        new TestData("Personal_MAVEN", Execution.Tool.MAVEN, "SimpleTest",
                "./TEST_PROJECTS/MAVEN/", "/src/test/java/",
                new String[] {"simpleTest01"/*, "simpleTest02", "simpleTest03"*/}),

        // 1
        new TestData("Personal_ANT", Execution.Tool.ANT, "SimpleTest",
            "./TEST_PROJECTS/ANT/", "/_files_/",
            "compile", "justTest", //"compile failonerror=false"
                new String[] {"simpleTest01"/*, "simpleTest02", "simpleTest03"*/}),

        // 2
        new TestData("Personal_GRADLE", Execution.Tool.GRADLE, "SimpleTest",
            "./TEST_PROJECTS/GRADLE/", "/src/test/java/",
            "compileTestJava", "test", "/home/spectre/.sdkman/candidates/gradle/7.6", //"/usr/share/java/gradle/",
                new String[] {"simpleTest01"/*, "simpleTest02", "simpleTest03"*/}),

        // 3 --------------- BEGIN: JODA-TIME BUGGY EXAMPLES
        new TestData("Joda-Time, Bug 1", Execution.Tool.MAVEN, "TestDateMidnight_Properties",
                "/home/_tynrael_/Documents/Weber/Graduate/Work/FINAL_EXTERNAL_TESTPROJECTS/time_1_buggy/", "/src/test/java/org/joda/time/",
                new String[] {"testPropertyGetMonthOfYear"}),

        // 4
        new TestData("Joda-Time, Bug 1", Execution.Tool.MAVEN, "TestDateTime_Properties",
                "/home/_tynrael_/Documents/Weber/Graduate/Work/FINAL_EXTERNAL_TESTPROJECTS/time_1_buggy/", "/src/test/java/org/joda/time/",
                new String[] {"testPropertyGetMonthOfYear"}),

        // 5
        new TestData("Joda-Time, Bug 1", Execution.Tool.MAVEN, "TestMutableDateTime_Properties",
                "/home/_tynrael_/Documents/Weber/Graduate/Work/FINAL_EXTERNAL_TESTPROJECTS/time_1_buggy/", "/src/test/java/org/joda/time/",
                new String[] {"testPropertyGetMonthOfYear"}),

        // 6
        new TestData("Joda-Time, Bug 1 (Multiple Methods)", Execution.Tool.MAVEN, "TestDateTimeFormat",
                "/home/_tynrael_/Documents/Weber/Graduate/Work/FINAL_EXTERNAL_TESTPROJECTS/time_1_buggy/", "/src/test/java/org/joda/time/format/",
                new String[] {"testFormat_halfdayOfDay", "testFormatParse_textHalfdayAM_UK", "testFormatParse_textEraBC_France"}),

        // 7 --------------- BEGIN: COMMONS-LANG BUGGY EXAMPLES
        new TestData("Commons-Lang, Bug 1", Execution.Tool.MAVEN, "NumberUtilsTest",
                "/home/spectre/Documents/GradSchool/Thesis_Project/Test_Projects/lang_1_buggy/",
                "/src/test/java/org/apache/commons/lang3/math/",
                new String[] {"TestLang747"}),
        // 8 --------------- BEGIN: JODA-TIME BUGGY EXAMPLES
        new TestData("Joda-Time, Bug 1", Execution.Tool.MAVEN, "TestPartial_Constructors",
                "/home/spectre/Documents/GradSchool/Thesis_Project/Test_Projects/time_1_buggy/",
                "src/test/java/org/joda/time/",
                new String[] {"testConstructorEx7_TypeArray_intArray"}),
        // 9 --------------- BEGIN: CODEC BUGGY EXAMPLES
        new TestData("Codec, Bug 1", Execution.Tool.MAVEN, "StringEncoderAbstractTest",
                "/home/spectre/Documents/GradSchool/Thesis_Project/Test_Projects/codec_1_buggy/",
                "src/test/org/apache/commons/codec/",
                new String[] {"testLocaleIndependence"}),
    };
}

// Created by Brandon Wilber, 07/2022
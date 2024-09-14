import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestData {
    Execution.Tool type;

    String identifier;
    String testClass;
    String testClassPath;
    String projectPath;
    String sourcePath;

    String compileTarget;
    String testTarget;

    String gradleDir;

    CompilationUnit cu;
    List<String> testMethods = new ArrayList<>();
    List<Execution> buildObjects = new ArrayList<>();
    List<BlockStmt> mainBlockStmts = new ArrayList<>();
    List<BlockStmt> backupStmts = new ArrayList<>();
    List<SimplifiedTree> simplifiedTrees = new ArrayList<>();
    List<Reducer> blockReducers = new ArrayList<>();

    public TestData (String inIdentifier, Execution.Tool inType, String inTestClass, String inProjectPath, String inSourcePath, String[] inTestMethods) {
        identifier = inIdentifier;
        type = inType;
        testClass = inTestClass;
        projectPath = inProjectPath;
        sourcePath = inSourcePath;

        Collections.addAll(testMethods, inTestMethods);
    }

    public TestData (String inIdentifier, Execution.Tool inType, String inTestClass, String inProjectPath, String inSourcePath, String inCompileTarget,
                     String inTestTarget, String[] inTestMethods) {
        this(inIdentifier, inType, inTestClass, inProjectPath, inSourcePath, inTestMethods);
        compileTarget = inCompileTarget;
        testTarget = inTestTarget;
    }

    public TestData (String inIdentifier, Execution.Tool inType, String inTestClass, String inProjectPath, String inSourcePath, String inCompileTarget,
                     String inTestTarget, String inGradleDir, String[] inTestMethods) {
        this(inIdentifier, inType, inTestClass, inProjectPath, inSourcePath, inCompileTarget, inTestTarget, inTestMethods);
        gradleDir = inGradleDir;
    }

    public void Initialize() throws FileNotFoundException {
        testClassPath = projectPath + sourcePath + testClass + ".java";
        cu = StaticJavaParser.parse(new File(testClassPath));
        BackupClass();
        ParseClass();
        BuildExecutionObjects();
        BuildReducers();
    }

    public void BackupClass() {
        if (Files.isDirectory(Paths.get(projectPath + "_backup"))) {
            if (!Files.exists(Paths.get(projectPath + "_backup/backup_" + testClass + ".txt"))) {
                FileWriterUtil.write(projectPath + "_backup/backup_" + testClass + ".txt", cu.toString());
            }
        } else {
            new File(projectPath + "_backup").mkdirs();
            FileWriterUtil.write(projectPath + "_backup/backup_" + testClass + ".txt", cu.toString());
        }
    }

    public void ParseClass() {
        for (int i = 0; i < cu.getTypes().size(); i++) {
            for (int j = 0; j < cu.getType(i).getMembers().size(); j++) {
                if (cu.getType(i).getMember(j).isMethodDeclaration()) {
                    for (String testMethod : testMethods) {
                        if (cu.getType(i).getMember(j).asMethodDeclaration().getName().asString().equals(testMethod)) {
                            mainBlockStmts.add(cu.getType(i).getMember(j).asMethodDeclaration().getBody().get());
                            backupStmts.add(cu.getType(i).getMember(j).asMethodDeclaration().getBody().get());
                            simplifiedTrees.add(new SimplifiedTree(mainBlockStmts.get(mainBlockStmts.size() - 1)));
                            break;
                        }
                    }
                }
            }
        }
    }

    public void BuildExecutionObjects() {
        for (String testMethod : testMethods) {
            switch (type) {
                case MAVEN -> buildObjects.add(new Execution(testMethod, type, projectPath, testClass));
                case ANT -> buildObjects.add(new Execution(type, projectPath, compileTarget, testTarget));
                case GRADLE -> buildObjects.add(new Execution(type, projectPath, gradleDir, compileTarget, testTarget));
                default -> {
                }
            }
        }
    }

    public void BuildReducers() {
        for (int i = 0; i < testMethods.size(); i++) {
            blockReducers.add(new Reducer(cu, testClassPath, simplifiedTrees.get(i), buildObjects.get(i)));
        }
    }

    public void SetTimeouts(int inTimeout, int inFrequency) {
        for (int i = 0; i < testMethods.size(); i++) {
            buildObjects.get(i).setTimeOut(inTimeout, inFrequency);
        }
    }

    public void Reduce(int method) throws MavenInvocationException {
        for (int i = 0; i < mainBlockStmts.size(); i++) {
            if (i == method && mainBlockStmts.get(method).getStatements().size() == 0) {
                TempClearMethod(i, false);
            } else if (i != method) {
                TempClearMethod(i, true);
            }
        }

        if (method > 0) {
            System.out.println();
        }

        System.out.println(">> Reducing Method: " + testMethods.get(method));
        blockReducers.get(method).Reduce(mainBlockStmts.get(method), 1, null);

        backupStmts.set(method, new BlockStmt());
        for (int i = 0; i < mainBlockStmts.get(method).getStatements().size(); i++) {
            backupStmts.get(method).addStatement(mainBlockStmts.get(method).getStatement(i));
        }
    }

    public void TempClearMethod(int method, boolean clearStatements) {
        NodeList<Statement> temp = new NodeList<>();
        if (clearStatements) {
            mainBlockStmts.get(method).setStatements(temp);
        } else {
            for (int i = 0; i < backupStmts.get(method).getStatements().size(); i++) {
                temp.add(backupStmts.get(method).getStatement(i));
            }
            mainBlockStmts.get(method).setStatements(temp);
        }
    }

    public void Finalize() {
        for (int i = 0; i < mainBlockStmts.size(); i++) {
            TempClearMethod(i, false);
        }
        FileWriterUtil.write(testClassPath, cu.toString());
    }
}

// Created by Brandon Wilber, 07/2022
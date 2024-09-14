import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.util.*;

public class Reducer {
    String filePath;
    CompilationUnit cu;
    SimplifiedTree simplifiedTree;
    Execution executor;

    int statementCount;
    int stepCount;
    int failedCompile;
    int duplicateStep;
    int removalCount;
    boolean timeout = false;
    String spacing = "     ";

    public Reducer(CompilationUnit inCU, String inFilePath, SimplifiedTree inTree, Execution inBuildObj) {
        cu = inCU;
        filePath = inFilePath;
        simplifiedTree = inTree;
        executor = inBuildObj;
        statementCount = inTree.simplifiedTree.get(inTree.simplifiedTree.size() - 1).id + 1;
    }

    public void Reduce(BlockStmt inBlockStmt, int level, List<SimplifiedTreeNode> inReducedList) throws MavenInvocationException {
        List<SimplifiedTreeNode> freshPass = simplifiedTree.GetLevelX(inBlockStmt, level);
        int groupSize;

        if (inReducedList == null) {
            groupSize = Math.max(freshPass.size() / 2, 1);
        } else {
            groupSize = Math.max(inReducedList.size() / 2, 1);
        }

        StringBuilder nestedSpacing = new StringBuilder(spacing);
        if (Main.logging) {
            for (int i = level; i > 1; i--) {
                nestedSpacing.append(spacing).append("     ");
            }
        }

        List<SimplifiedTreeNode> firstHalf = new ArrayList<>();
        List<SimplifiedTreeNode> secondHalf = new ArrayList<>();

        for (int i = 0; i < freshPass.size(); i++) {
            if (inReducedList == null) {
                if (i >= freshPass.size() - groupSize) {
                    secondHalf.add(freshPass.get(i));
                    freshPass.get(i).testRemoval = true;
                } else {
                    firstHalf.add(freshPass.get(i));
                }
            } else {
                for (int j = 0; j < inReducedList.size(); j++) {
                    if (freshPass.get(i).id == inReducedList.get(j).id) {
                        if (j >= inReducedList.size() - groupSize) {
                            secondHalf.add(freshPass.get(i));
                            freshPass.get(i).testRemoval = true;
                        } else {
                            firstHalf.add(freshPass.get(i));
                        }
                    }
                }
            }
        }

        if (secondHalf.size() > 0) { // Removed OR check for if build was greater than 0, if not removing, no point in checking
            if (Main.logging) {
                System.out.println(nestedSpacing + "Attempting to remove: " + secondHalf.size() + "/" + freshPass.size() + " Statements, Level: " + level);
            }

            String stepID = "";

            for (SimplifiedTreeNode node : freshPass) {
                if (!node.isDisabled && !node.testRemoval) {
                    stepID += String.valueOf(node.id) + "_";
                }
            }

            inBlockStmt.setStatements(SetStatements(freshPass));
            FileWriterUtil.write(filePath, cu.toString());

            if (Main.logging) {
                System.out.println(nestedSpacing + ">>> Attempting Compile, Level: " + level);
            }

            stepCount++;

            if (simplifiedTree.stepSet.contains(stepID)) {
                duplicateStep++;
            } else {
                if (freshPass.size() > 0) {
                    simplifiedTree.stepSet.add(stepID);
                }

                if (executor.Execute(Execution.ExecuteType.COMPILE) == 0) { // Successfully compiles
                    if (Main.logging) {
                        System.out.println(nestedSpacing + ">>> Compile Success, Attempting Test, Lvl: " + level);
                    }

                    int testResult = executor.Execute(Execution.ExecuteType.TEST);

                    if (testResult == 1) { // Fails test (desired)
                        if (Main.logging) {
                            System.out.println(nestedSpacing + ">>> Test Failed (Yay!), Lvl: " + level);
                        }
                        for (SimplifiedTreeNode node : secondHalf) {
                            simplifiedTree.simplifiedTree.get(node.id).isDisabled = true;
                            removalCount++;
                        }
                    } else { // If it compiles but passes the test (undesired)
                        if (Main.logging) {
                            if (testResult == 2) {
                                System.out.println(nestedSpacing + ">>> Test TIMED OUT, Lvl: " + level);
                            } else {
                                System.out.println(nestedSpacing + ">>> Test Passes (Boo), Lvl: " + level);
                            }
                        }
                    }
                } else {
                    failedCompile++;

                    if (Main.logging) {
                        System.out.println(nestedSpacing + ">>> Compile Failed, Lvl: " + level);
                    }
                }
            }

            if (!secondHalf.get(0).isDisabled && secondHalf.size() > 1) { // Recurse and breakdown second group further
                inBlockStmt.setStatements(SetStatements(freshPass));
                Reduce(inBlockStmt, level, secondHalf); // If not removed, recurse and subdivide again
            }

            // If nested statement, dig through the child statements
            if (groupSize == 1 && !secondHalf.get(0).isDisabled && secondHalf.get(0).hasChildren) {
                inBlockStmt.setStatements(SetStatements(freshPass));

                List<SimplifiedTreeNode> childDive = simplifiedTree.GetNodesByParent(secondHalf.get(0).id);

                BlockStmt tempBlock = new BlockStmt();
                for (int i = childDive.size() - 1; i > 0; i--) {
                    if (childDive.get(i).containerBlock != tempBlock) {
                        tempBlock = childDive.get(i).containerBlock;
                        Reduce(tempBlock, level + 1, null);
                    }
                }
            }
        } else {
            if (Main.logging) {
                System.out.println("Remove list is EMPTY!!!!!");
            }
        }

        if (firstHalf.size() > 0 && !firstHalf.get(0).isDisabled) { // Check first half
            inBlockStmt.setStatements(SetStatements(freshPass));
            Reduce(inBlockStmt, level, firstHalf);
        }

        inBlockStmt.setStatements(SetStatements(freshPass));
        FileWriterUtil.write(filePath, cu.toString());

        if (level == 1 && inReducedList == null) {
            String results = "Total Statements: " + statementCount + " | Total Steps: " + stepCount +
                    " | DuplicateSteps: " + duplicateStep + "\nFailed Compiles: " + failedCompile +
                    " | RemovedStatements: " + removalCount + " | Timeout: " + timeout;
            FileWriterUtil.write(Execution.xmlPath + executor.testMethod + "_results.txt", results);

            System.out.println(results);
        }
    }

    public NodeList<Statement> SetStatements(List<SimplifiedTreeNode> inList) {
        NodeList<Statement> temp = new NodeList<>();
        for (SimplifiedTreeNode simplifiedTreeNode : inList) {
            if (simplifiedTreeNode.testRemoval) {
                simplifiedTreeNode.testRemoval = false;
            } else if (!simplifiedTreeNode.isDisabled) {
                temp.add((Statement) simplifiedTreeNode.statement);
            }
        }
        return temp;
    }
}

// Created by Brandon Wilber, 06/2022

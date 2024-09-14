import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.util.*;

public class SimplifiedTree {
    List<SimplifiedTreeNode> simplifiedTree = new ArrayList<>();
    Set<String> stepSet = new HashSet<String>();
    int count = 0;

    public SimplifiedTree(Statement inBlockStmt) {
        TreeCrawl(inBlockStmt, 1, -1); // -1 Parent == None/Root
    }

    public void TreeCrawl(Statement inStmt, int inDepth, int inParent) {
        if (inStmt instanceof BlockStmt){
            for (Statement childStatement : ((BlockStmt) inStmt).getStatements()) {
                simplifiedTree.add(new SimplifiedTreeNode(count, (BlockStmt) inStmt, childStatement, inDepth, inParent));
                int tempParent = count;
                count++;

                boolean stopDigging = false; // This whole block is just to check for and manage lambda expressions
                if (childStatement.getChildNodes().size() > 0 && childStatement.getChildNodes().get(0).getChildNodes().size() > 1) {
                    for (Node lambdaCheck : childStatement.getChildNodes().get(0).getChildNodes()) {
                        if (lambdaCheck instanceof LambdaExpr) {
                            stopDigging = true;
                            for (Node innerLambda : lambdaCheck.getChildNodes()) {
                                if (innerLambda instanceof BlockStmt) {
                                    simplifiedTree.get(count - 1).hasChildren = true;
                                    TreeCrawl((Statement) innerLambda, inDepth + 1, count - 1);
                                }
                            }
                        }
                    }
                }

                if (childStatement instanceof IfStmt) {
                    stopDigging = true;
                    simplifiedTree.get(count - 1).hasChildren = true;
                    CheckBlockStmt((IfStmt) childStatement, inDepth, tempParent);
                }

                if (childStatement.getChildNodes().size() > 1 && !stopDigging) {
                    simplifiedTree.get(count - 1).hasChildren = true;

                    for (Node nestedChild : childStatement.getChildNodes()) {
                        if (nestedChild instanceof BlockStmt) {
                            TreeCrawl((Statement) nestedChild, inDepth + 1, tempParent);
                        } else if (nestedChild instanceof SwitchEntry switchEntry) {
                            BlockStmt newSwitchBlock = new BlockStmt();
                            if (nestedChild.getChildNodes().size() == 2 && nestedChild.getChildNodes().get(1) instanceof BlockStmt) {
                                TreeCrawl((Statement) nestedChild.getChildNodes().get(1), inDepth + 1, tempParent);
                            } else {
                                int numStatements = nestedChild.getChildNodes().size();
                                int position = numStatements == 1 ? 0 : 1; // In cases where there is a single statement, this preserves it
                                for (int i = 1; i < numStatements; i++) {
                                    newSwitchBlock.addStatement((Statement) nestedChild.getChildNodes().get(position));
                                }

                                NodeList<Statement> emptyList = new NodeList<>();
                                switchEntry.setStatements(emptyList);
                                switchEntry.addStatement(newSwitchBlock);

                                if (nestedChild.getChildNodes().size() > 1) {
                                    TreeCrawl((Statement) nestedChild.getChildNodes().get(1), inDepth + 1, tempParent);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void CheckBlockStmt (IfStmt inIfStmt, int inDepth, int inParent) {
        if (!(inIfStmt.getThenStmt() instanceof BlockStmt)) {
            if (inIfStmt.getThenStmt() instanceof IfStmt) {
                CheckBlockStmt((IfStmt) inIfStmt.getThenStmt(), inDepth, inParent);
            } else {
                BlockStmt tempThen = new BlockStmt();
                tempThen.addStatement(inIfStmt.getThenStmt());
                inIfStmt.setThenStmt(tempThen);
                TreeCrawl(inIfStmt.getThenStmt(), inDepth + 1, inParent);
            }
        } else {
            TreeCrawl(inIfStmt.getThenStmt(), inDepth + 1, inParent);
        }

        if (inIfStmt.getChildNodes().size() == 3) {
            if (!(inIfStmt.getElseStmt().get() instanceof BlockStmt)) {
                if (inIfStmt.getElseStmt().get() instanceof IfStmt) {
                    CheckBlockStmt((IfStmt) inIfStmt.getElseStmt().get(), inDepth, inParent);
                } else {
                    BlockStmt tempElse = new BlockStmt();
                    tempElse.addStatement(inIfStmt.getElseStmt().get());
                    inIfStmt.setElseStmt(tempElse);
                    TreeCrawl(inIfStmt.getElseStmt().get(), inDepth + 1, inParent);
                }
            } else {
                TreeCrawl(inIfStmt.getElseStmt().get(), inDepth + 1, inParent);
            }
        }
    }

    public List<SimplifiedTreeNode> GetLevelX(BlockStmt inBlock, int levelX) {
        List<SimplifiedTreeNode> temp = new ArrayList<>();

        for (SimplifiedTreeNode node : simplifiedTree) {
            if (node.depth == levelX && node.containerBlock == inBlock && !node.isDisabled) {
                temp.add(node);
            }
        }
        return temp;
    }

    public List<SimplifiedTreeNode> GetNodesByParent(int parentID) {
        List<SimplifiedTreeNode> temp = new ArrayList<>();

        for (SimplifiedTreeNode node : simplifiedTree) {
            if (node.parent == parentID) {
                temp.add(node);
            }
        }
        return temp;
    }

    /*
    public int MaxHeight() { // I don't actually use this
        int max = 1;

        for (SimplifiedTreeNode node : simplifiedTree) {
            if (node.depth > max) {
                max = node.depth;
            }
        }

        return max;
    }

    public NodeList<Statement> GetLeafNodes() { // I don't actually use this
        NodeList<Statement> temp = new NodeList<>();

        for (SimplifiedTreeNode node : simplifiedTree) {
            if (!node.hasChildren) {
                temp.add((Statement)node.statement);
            }
        }
        return temp;
    }*/
}

// Created by Brandon Wilber, 06/2022

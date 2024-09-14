import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;

public class SimplifiedTreeNode {
    int id;
    int parent;
    int depth;
    boolean isDisabled = false;
    boolean hasChildren = false;
    boolean testRemoval = false;
    Node statement;
    BlockStmt containerBlock;

    public SimplifiedTreeNode(int inID, BlockStmt inContainer, Node inStatement, int inDepth, int inParent) {
        id = inID;
        containerBlock = inContainer;
        statement = inStatement;
        depth = inDepth;
        parent = inParent;
    }
}

// Created by Brandon Wilber, 06/2022

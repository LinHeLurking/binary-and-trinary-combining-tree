package combining_tree;

import java.util.Stack;

public class CombiningTree {
    Node[] leaf;

    CombiningTree(int threadNum, int ary) {
        int totalWidth = 1;
        int threadLeafNum = (threadNum + ary - 1) / ary;
        leaf = new Node[threadLeafNum];
        while (totalWidth < threadLeafNum) {
            totalWidth *= ary;
        }
        Node[] nodes = new Node[(ary * totalWidth - 1) / (ary - 1)];
        nodes[0] = new Node();
        nodes[0].cStatus = Node.CStatus.ROOT;
        for (int i = 1; i < nodes.length; ++i) {
            nodes[i] = new Node(nodes[(i - 1) / ary]);
        }
        System.arraycopy(nodes, (totalWidth - 1) / (ary - 1), leaf, 0, threadLeafNum);
    }

    public int get() throws Exception {
        return getAndAdd(0);
    }

    public int getAndIncrement() throws Exception {
        return getAndAdd(1);
    }

    public int getAndAdd(int val) throws Exception {
        Stack<Node> stack = new Stack<>();
        Node myLeaf = leaf[((int) Thread.currentThread().getId()) % leaf.length];
        Node node = myLeaf;
        // pre-combining phase
        while (node.preCombined()) {
            node = node.parent;
        }
        Node stop = node;
        // combining phase
        node = myLeaf;
        int combined = val;
        while (node != stop) {
            combined = node.combine(combined);
            stack.push(node);
            node = node.parent;
        }
        // operation phase
        int prior = stop.op(combined);
        // distribution phase
        while (!stack.empty()) {
            node = stack.pop();
            node.distribute(prior);
        }
        return prior;
    }
}

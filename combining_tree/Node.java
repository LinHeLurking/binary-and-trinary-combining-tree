package combining_tree;

public class Node {
    enum CStatus {IDLE, FIRST, SECOND, THIRD1, THIRD2, RESULT, ROOT}

    int cnt;
    CStatus cStatus;
    int firstValue, secondValue, thirdValue;
    int result;
    Node parent;


    public Node() {
        cStatus = CStatus.IDLE;
        cnt = 0;
    }

    public Node(Node myParent) {
        this();
        parent = myParent;
    }

    synchronized boolean preCombined() throws Exception {
        while (cnt == 2 || cnt == -1) wait();
        switch (cStatus) {
            case IDLE:
                cStatus = CStatus.FIRST;
                return true;
            case FIRST:
                cnt = 1;
                cStatus = CStatus.SECOND;
                return false;
            case SECOND:
                cnt = 2;
                cStatus = CStatus.THIRD1;
                return false;
            case ROOT:
                return false;
            default:
                throw new Exception("Unexpected Node state:" + cStatus);
        }
    }

    synchronized int combine(int combined) throws Exception {
        while (cnt == 2 || cnt == 1) wait();
        cnt = 2;
        firstValue = combined;
        switch (cStatus) {
            case FIRST:
                return firstValue;
            case SECOND:
                return firstValue + secondValue;
            case THIRD2:
                return firstValue + secondValue + thirdValue;
            default:
                throw new Exception("Unexpected Node state:" + cStatus);
        }
    }


    synchronized int op(int combined) throws Exception {
        switch (cStatus) {
            case ROOT:
                int prior = result;
                result += combined;
                return prior;
            case SECOND:
                secondValue = combined;
                cnt = -1;
                notifyAll();
                while (cStatus != CStatus.RESULT) wait();
                cnt = 0;
                cStatus = CStatus.IDLE;
                notifyAll();
                return result;
            case THIRD1:
                secondValue = combined;
                cStatus = CStatus.THIRD2;
                while (cStatus != CStatus.RESULT) wait();
                return result;
            case THIRD2:
                thirdValue = combined;
                cnt = -1;
                notifyAll();
                while (cStatus != CStatus.RESULT) wait();
                cnt = 0;
                cStatus = CStatus.IDLE;
                notifyAll();
                return result;
            default:
                throw new Exception("Unexpected Node state:" + cStatus);
        }
    }

    synchronized void distribute(int prior) {
        switch (cStatus) {
            case FIRST:
                cStatus = CStatus.IDLE;
                cnt = 0;
                break;
            case SECOND:
                result = prior + firstValue;
                cStatus = CStatus.RESULT;
                break;
            case THIRD2:
                result = prior + firstValue + secondValue;
                cStatus = CStatus.RESULT;
                break;
        }
        notifyAll();
    }
}

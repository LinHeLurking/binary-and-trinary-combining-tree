package combining_tree;

public class NodeOriginal {
    enum CStatus {IDLE, FIRST, SECOND, RESULT, ROOT}

    boolean locked;
    NodeOriginal.CStatus cStatus;
    int firstValue, secondValue, thirdValue;
    int result;
    NodeOriginal parent;

    NodeOriginal() {
        cStatus = NodeOriginal.CStatus.IDLE;
        locked = false;
    }

    public NodeOriginal(NodeOriginal myParent) {
        this();
        parent = myParent;
    }

    synchronized boolean preCombine() throws Exception {
        while (locked) wait();
        switch (cStatus) {
            case IDLE:
                cStatus = CStatus.FIRST;
                return true;
            case FIRST:
                locked = true;
                cStatus = CStatus.SECOND;
                return false;
            case ROOT:
                return false;
            default:
                // If correctly implemented, this branch never executes.
                throw new Exception("Unexpected Node state" + cStatus);
        }
    }

    synchronized int combine(int combined) throws Exception {
        while (locked) wait();
        locked = true;
        firstValue = combined;
        switch (cStatus) {
            case FIRST:
                return firstValue;
            case SECOND:
                return firstValue + secondValue;
            default:
                // If correctly implemented, this branch never executes.
                throw new Exception("Unexpected Node state" + cStatus);
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
                locked = false;
                notifyAll();
                while (cStatus != CStatus.RESULT) wait();
                locked = false;
                notifyAll();
                cStatus = CStatus.IDLE;
                return result;
            default:
                // If correctly implemented, this branch never executes.
                throw new Exception("Unexpected Node state" + cStatus);
        }
    }

    synchronized void distribute(int prior) throws Exception {
        switch (cStatus) {
            case FIRST:
                cStatus = CStatus.IDLE;
                locked = false;
                break;
            case SECOND:
                result = prior + firstValue;
                cStatus = CStatus.RESULT;
                break;
            default:
                // If correctly implemented, this branch never executes.
                throw new Exception("Unexpected Node state" + cStatus);
        }
        notifyAll();
    }
}

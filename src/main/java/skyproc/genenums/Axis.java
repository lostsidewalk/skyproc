package skyproc.genenums;

/**
 * @author Justin Swanson
 */
public enum Axis {

    X,

    Y,

    Z;

    /**
     * @param s
     * @return
     */
    public static Axis get(String s) {
        switch (s) {
            case "X":
                return X;
            case "Y":
                return Y;
            default:
                return Z;
        }
    }
}

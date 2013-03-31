package si.mazi.coinstory;

/**
 * @author Matija Mazi <br/>
 * @created 3/31/13 9:56 PM
 */
public class Utils {
    public static String joinToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        while (e != null) {
            if (sb.length() > 0) {
                sb.append("\n    cause: ");
            }
            sb.append(e.toString());
            e = e.getCause();
        }
        return sb.toString();
    }
}

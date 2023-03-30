package hu.bme.mit.swsv.itssos.util;

import eu.arrowhead.common.Utilities;
import org.apache.logging.log4j.util.Supplier;

import java.util.function.Function;

public class Utils {
    public static String toPrettyJson(Object object) {
        return Utilities.toPrettyJson(Utilities.toJson(object));
    }

    /**
     * Log4j-typed parameter Supplier to convert object to JSON
     */
    public static Supplier<String> toPrettyJsonSupplier(Object object) {
        return bind(Utils::toPrettyJson, object);
    }

    /**
     * Bind parameter and convert functional interface to Log4j-typed parameter Supplier
     */
    public static <T, R> Supplier<R> bind(Function<T, R> func, T param) {
        return () -> func.apply(param);
    }

    private Utils() {
        throw new UnsupportedOperationException();
    }
}

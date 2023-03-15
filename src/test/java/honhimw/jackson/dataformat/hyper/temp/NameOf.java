package honhimw.jackson.dataformat.hyper.temp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author hon_him
 * @since 2023-03-15
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NameOf {

    ColumnCode value();
}

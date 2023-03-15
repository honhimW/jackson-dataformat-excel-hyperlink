package honhimw.jackson.dataformat.hyper.temp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author hon_him
 * @since 2023-03-15
 */
@Getter
@RequiredArgsConstructor
public enum ColumnCode {
    A("Code A"), B("Code B");
    final String text;
    // ...
}

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package honhimw.jackson.dataformat.hyper.deser;

import honhimw.jackson.dataformat.hyper.BookContent;
import java.io.File;
import java.io.InputStream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class BookInput<T> implements BookContent<T> {

    private final T raw;
    private final String name;
    private final int index;

    private BookInput(final T raw, final int index) {
        this.raw = raw;
        this.name = null;
        this.index = index;
    }

    private BookInput(final T raw, final String name) {
        this.raw = raw;
        this.name = name;
        this.index = -1;
    }

    public static BookInput<File> source(final File raw) {
        return new BookInput<>(raw, 0);
    }

    public static BookInput<File> source(final File raw, final int sheetIndex) {
        return new BookInput<>(raw, sheetIndex);
    }

    public static BookInput<File> source(final File raw, final String sheetName) {
        return new BookInput<>(raw, sheetName);
    }

    public static BookInput<InputStream> source(final InputStream raw) {
        return new BookInput<>(raw, 0);
    }

    public static BookInput<InputStream> source(final InputStream raw, final int sheetIndex) {
        return new BookInput<>(raw, sheetIndex);
    }

    public static BookInput<InputStream> source(final InputStream raw, final String sheetName) {
        return new BookInput<>(raw, sheetName);
    }
}

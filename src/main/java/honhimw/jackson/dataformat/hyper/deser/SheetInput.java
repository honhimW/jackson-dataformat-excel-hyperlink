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

import honhimw.jackson.dataformat.hyper.SheetContent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.io.InputStream;

@Getter
@ToString
@EqualsAndHashCode
public final class SheetInput<T> implements SheetContent<T> {

    private final T raw;
    private final String name;
    private final int index;

    private SheetInput(final T raw, final int index) {
        this.raw = raw;
        this.name = null;
        this.index = index;
    }

    private SheetInput(final T raw, final String name) {
        this.raw = raw;
        this.name = name;
        this.index = -1;
    }

    public static SheetInput<File> source(final File raw) {
        return new SheetInput<>(raw, 0);
    }

    public static SheetInput<File> source(final File raw, final int sheetIndex) {
        return new SheetInput<>(raw, sheetIndex);
    }

    public static SheetInput<File> source(final File raw, final String sheetName) {
        return new SheetInput<>(raw, sheetName);
    }

    public static SheetInput<InputStream> source(final InputStream raw) {
        return new SheetInput<>(raw, 0);
    }

    public static SheetInput<InputStream> source(final InputStream raw, final int sheetIndex) {
        return new SheetInput<>(raw, sheetIndex);
    }

    public static SheetInput<InputStream> source(final InputStream raw, final String sheetName) {
        return new SheetInput<>(raw, sheetName);
    }
}

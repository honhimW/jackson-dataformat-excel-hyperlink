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

package honhimw.jackson.dataformat.hyper.ser;

import honhimw.jackson.dataformat.hyper.SheetContent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.io.OutputStream;

@Getter
@ToString
@EqualsAndHashCode
public final class SheetOutput<T> implements SheetContent<T> {

    private final T raw;
    private final String name;

    private SheetOutput(final T raw, final String name) {
        this.raw = raw;
        this.name = name;
    }

    public static SheetOutput<File> target(final File raw) {
        return new SheetOutput<>(raw, null);
    }

    public static SheetOutput<File> target(final File raw, final String sheetName) {
        return new SheetOutput<>(raw, sheetName);
    }

    public static SheetOutput<OutputStream> target(final OutputStream raw) {
        return new SheetOutput<>(raw, null);
    }

    public static SheetOutput<OutputStream> target(final OutputStream raw, final String sheetName) {
        return new SheetOutput<>(raw, sheetName);
    }
}

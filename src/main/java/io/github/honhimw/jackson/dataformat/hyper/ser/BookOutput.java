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

package io.github.honhimw.jackson.dataformat.hyper.ser;

import io.github.honhimw.jackson.dataformat.hyper.BookContent;
import java.io.File;
import java.io.OutputStream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class BookOutput<T> implements BookContent<T> {

    private final T raw;
    private final String name;

    private BookOutput(final T raw, final String name) {
        this.raw = raw;
        this.name = name;
    }

    public static BookOutput<File> target(final File raw) {
        return new BookOutput<>(raw, null);
    }

    public static BookOutput<File> target(final File raw, final String sheetName) {
        return new BookOutput<>(raw, sheetName);
    }

    public static BookOutput<OutputStream> target(final OutputStream raw) {
        return new BookOutput<>(raw, null);
    }

    public static BookOutput<OutputStream> target(final OutputStream raw, final String sheetName) {
        return new BookOutput<>(raw, sheetName);
    }
}

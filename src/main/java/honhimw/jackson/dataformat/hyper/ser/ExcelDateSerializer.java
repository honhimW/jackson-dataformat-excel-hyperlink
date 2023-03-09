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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public final class ExcelDateSerializer<T> extends JsonSerializer<T> {

    private final BiFunction<T, Boolean, Double> function;

    @Override
    public void serialize(final T value, JsonGenerator gen, final SerializerProvider serializers) throws IOException {
        final boolean date1904 = ((SheetGenerator) gen).isDate1904();
        gen.writeNumber(function.apply(value, date1904));
    }
}

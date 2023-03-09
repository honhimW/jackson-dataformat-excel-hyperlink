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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public final class ExcelDateDeserializer<T> extends JsonDeserializer<T> {

    private final BiFunction<Double, Boolean, T> function;

    @Override
    public T deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        final boolean date1904 = ((SheetParser) p).isDate1904();
        return function.apply(p.getDoubleValue(), date1904);
    }
}
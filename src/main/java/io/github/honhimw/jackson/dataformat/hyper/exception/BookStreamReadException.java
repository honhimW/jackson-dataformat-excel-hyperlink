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

package io.github.honhimw.jackson.dataformat.hyper.exception;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.util.RequestPayload;
import io.github.honhimw.jackson.dataformat.hyper.deser.BookParser;

@SuppressWarnings("java:S110")
public final class BookStreamReadException extends StreamReadException {

    public BookStreamReadException(final JsonParser p, final String msg) {
        super(p, msg);
    }

    public static BookStreamReadException unexpected(final BookParser p, final Object value) {
        return new BookStreamReadException(p, "Unexpected value: " + value);
    }

    @Override
    public BookStreamReadException withParser(final JsonParser p) {
        _processor = p;
        return this;
    }

    @Override
    public BookStreamReadException withRequestPayload(final RequestPayload payload) {
        _requestPayload = payload;
        return this;
    }
}

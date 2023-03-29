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

package io.github.honhimw.jackson.dataformat.hyper.deser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonToken;
import io.github.honhimw.jackson.dataformat.hyper.HyperMapper;
import io.github.honhimw.jackson.dataformat.hyper.exception.BookStreamReadException;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import io.github.honhimw.jackson.dataformat.hyper.support.FixtureAs;
import io.github.honhimw.jackson.dataformat.hyper.support.fixture.Entry;
import io.github.honhimw.jackson.dataformat.hyper.support.fixture.NestedEntry;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookParserTest implements FixtureAs {

    HyperMapper mapper;
    BookParser parser;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new HyperMapper();
        parser = mapper.createParser(fixtureAsFile("entries.xlsx"));
    }

    @Test
    void noSchema() throws Exception {
        assertThatThrownBy(parser::nextToken)
                .isInstanceOf(BookStreamReadException.class)
                .hasMessageContaining("No schema of type '%s' set, can not parse", HyperSchema.SCHEMA_TYPE);
    }

    @Test
    void flatEntry() throws Exception {
        parser.setSchema(mapper.sheetSchemaFor(Entry.class));
        testEntry(this::Entry);
    }

    @Test
    void nestedEntry() throws Exception {
        parser = mapper.createParser(fixtureAsFile("nested-entries.xlsx"));
        parser.setSchema(mapper.sheetSchemaFor(NestedEntry.class));
        testEntry(this::assertNestedEntry);
    }

    void testEntry(final EntryAssertion assertEntry) throws Exception {
        assertThat(parser.hasCurrentToken()).isFalse();
        assertToken(JsonToken.START_ARRAY);
        assertEntry.run();
        assertEntry.run();
        assertToken(JsonToken.END_ARRAY);
        assertNoEntry();
        parser.close();
        assertThat(parser.isClosed()).isTrue();
    }

    void Entry() throws IOException {
        assertToken(JsonToken.START_OBJECT);
        assertField(JsonToken.VALUE_NUMBER_INT);
        assertField(JsonToken.VALUE_NUMBER_INT);
        assertToken(JsonToken.END_OBJECT);
    }

    void assertNestedEntry() throws IOException {
        assertToken(JsonToken.START_OBJECT);
        assertField(JsonToken.VALUE_NUMBER_INT);
        assertField(JsonToken.START_OBJECT);
        assertField(JsonToken.VALUE_NUMBER_INT);
        assertToken(JsonToken.END_OBJECT);
        assertToken(JsonToken.END_OBJECT);
    }

    void assertField(final JsonToken value) throws IOException {
        assertToken(JsonToken.FIELD_NAME);
        assertToken(value);
    }

    void assertNoEntry() throws IOException {
        assertThat(parser.nextToken()).isNull();
        assertThat(parser.hasCurrentToken()).isFalse();
    }

    void assertToken(final JsonToken token) throws IOException {
        assertThat(parser.nextToken()).isEqualTo(token);
        assertThat(parser.hasCurrentToken()).isTrue();
    }

    interface EntryAssertion {
        void run() throws IOException;
    }
}

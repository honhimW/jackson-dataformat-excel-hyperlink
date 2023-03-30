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

package io.github.honhimw.jackson.dataformat.hyper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.github.honhimw.jackson.dataformat.hyper.ser.BookOutput;
import io.github.honhimw.jackson.dataformat.hyper.support.FixtureAs;
import io.github.honhimw.jackson.dataformat.hyper.temp.Person;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HyperMapperTest implements FixtureAs {

    HyperMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Nested
    class ReadTest {
        @Test
        void readValue() throws Exception {
            final File src = fixtureAsFile("person.xlsx");
            final Person value = mapper.readValue(src, Person.class);
            assertThat(value).isEqualTo(Person.VALUE);
        }

        @Test
        void readValues() throws Exception {
            final File src = fixtureAsFile("person.xlsx");
            final List<Person> value = mapper.readValues(src, Person.class);
            assertThat(value).isEqualTo(Person.VALUES);
        }

    }

    @Nested
    class ReadFailingTest {
    }

    @Nested
    class WriteTest {
        @TempDir
        Path tempDir;
        File out;

        @BeforeEach
        void setUp() throws Exception {
            out = tempDir.resolve("test.xlsx").toFile();
        }

        @Test
        void writeValue() throws Exception {
            mapper.writeValue(out, Person.VALUE);
            final Person actual = mapper.readValue(out, Person.class);
            assertThat(actual).isEqualTo(Person.VALUE).isNotSameAs(Person.VALUE);
        }

        @Test
        void writeValues() throws Exception {
            mapper.writeValue(out, Person.VALUES, Person.class);
            final List<Person> actual = mapper.readValues(out, Person.class);
            assertThat(actual).isEqualTo(Person.VALUES).isNotSameAs(Person.VALUES);
        }

        @Test
        void writeValuesWithSheetName() throws Exception {
            final BookOutput<File> output = BookOutput.target(out, "");
            mapper.writeValue(output, Person.VALUES, Person.class);
            try (XSSFWorkbook workbook = new XSSFWorkbook(out)) {
                final List<Person> actual = mapper.readValues(workbook, Person.class);
                assertThat(actual).isEqualTo(Person.VALUES).isNotSameAs(Person.VALUES);
            }
        }
    }

    @Nested
    class WriteFailingTest {

        @TempDir
        Path tempDir;
        File out;

        @BeforeEach
        void setUp() throws Exception {
            out = tempDir.resolve("test.xlsx").toFile();
        }

        @Test
        void writeValuesWithoutValueType() throws Exception {
            assertThatThrownBy(() -> mapper.writeValue(out, Person.VALUES))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("`valueType` MUST be specified to write a value of a Collection or array type");
        }

        @Test
        @SneakyThrows
        void writeValue() {
            mapper.writeValue(out, List.of(Person.VALUE), Person.class);
        }
    }
}

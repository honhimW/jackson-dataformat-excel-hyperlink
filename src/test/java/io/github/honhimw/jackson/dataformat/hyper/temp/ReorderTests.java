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

package io.github.honhimw.jackson.dataformat.hyper.temp;

import io.github.honhimw.jackson.dataformat.hyper.HyperMapper;
import io.github.honhimw.jackson.dataformat.hyper.deser.BookParser;
import io.github.honhimw.jackson.dataformat.hyper.schema.generator.ColumnNameResolver;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-03-13
 */

public class ReorderTests {

    @Test
    @SneakyThrows
    public void reorderParseByColumnName() {
        File file = new File("E:\\temp\\1234.xlsx");
        HyperMapper mapper = new HyperMapper();
        mapper.setColumnNameResolver(prop -> {
            ColumnName annotation = prop.getAnnotation(ColumnName.class);
            if (Objects.nonNull(annotation)) {
                if (StringUtils.isNotBlank(annotation.value())) {
                    return annotation.value();
                }
            }
            return ColumnNameResolver.DEFAULT.resolve(prop);
        });
        mapper.enable(BookParser.Feature.REORDER_BY_COLUMN_NAME);

        List<Person> people = mapper.readValues(file, Person.class);

        people.forEach(System.out::println);
    }

}

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

package io.github.honhimw.jackson.dataformat.hyper;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.honhimw.jackson.dataformat.hyper.deser.ExcelDateDeserializer;
import io.github.honhimw.jackson.dataformat.hyper.ser.ExcelDateSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.function.BiFunction;
import org.apache.poi.ss.usermodel.DateUtil;

public final class ExcelDateModule extends SimpleModule {

    public ExcelDateModule() {
        addExcelDateDeserializers();
        addExcelDateSerializers();
    }

    private void addExcelDateDeserializers() {
        final BiFunction<Double, Boolean, LocalDateTime> getLocalDateTime = DateUtil::getLocalDateTime;
        final BiFunction<Double, Boolean, LocalDate> getLocalDate = getLocalDateTime.andThen(
            LocalDateTime::toLocalDate);
        addDeserializer(Date.class, new ExcelDateDeserializer<>(DateUtil::getJavaDate));
        addDeserializer(Calendar.class, new ExcelDateDeserializer<>(DateUtil::getJavaCalendar));
        addDeserializer(LocalDate.class, new ExcelDateDeserializer<>(getLocalDate));
        addDeserializer(LocalDateTime.class, new ExcelDateDeserializer<>(getLocalDateTime));
    }

    private void addExcelDateSerializers() {
        addSerializer(Date.class, new ExcelDateSerializer<>(DateUtil::getExcelDate));
        addSerializer(Calendar.class, new ExcelDateSerializer<>(DateUtil::getExcelDate));
        addSerializer(LocalDate.class, new ExcelDateSerializer<>(DateUtil::getExcelDate));
        addSerializer(LocalDateTime.class, new ExcelDateSerializer<>(DateUtil::getExcelDate));
    }
}

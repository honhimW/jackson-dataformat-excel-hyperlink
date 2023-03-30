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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author hon_him
 * @since 2022-12-12
 */

public class MockUtils {

    public static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @SneakyThrows
    public static <T> Collection<T> generate(Class<T> tClass, int num) {
        List<T> list = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            T reflection = reflection(tClass);
            list.add(reflection);
        }
        return list;
    }

    @SneakyThrows
    public static <T> T reflection(Class<T> tClass) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        T entity = tClass.getConstructor().newInstance();
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(tClass);
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (StringUtils.equals(pd.getName(), "class")) {
                continue;
            }
            Class<?> type = pd.getPropertyType();
            Object value = null;
            if (String.class.isAssignableFrom(type)) {
                value = RandomStringUtils.random(random.nextInt(10, 32), CHARS);
            } else if (Short.class.isAssignableFrom(type)) {
                value = random.nextInt(1, 100);
            } else if (Integer.class.isAssignableFrom(type)) {
                value = random.nextInt(1, 10_000);
            } else if (Long.class.isAssignableFrom(type)) {
                value = random.nextLong(1, 10_000);
            } else if (Float.class.isAssignableFrom(type)) {
                value = random.nextFloat(1, 10_000);
            } else if (Double.class.isAssignableFrom(type)) {
                value = random.nextDouble(1, 10_000);
            } else if (Boolean.class.isAssignableFrom(type)) {
                value = random.nextBoolean();
            } else if (Enum.class.isAssignableFrom(type)) {
                Method values = type.getDeclaredMethod("values");
                Object[] invoke = (Object[]) values.invoke(null);
                if (ArrayUtils.isNotEmpty(invoke)) {
                    value = invoke[random.nextInt(0, invoke.length - 1)];
                }
            } else {
                try {
                    value = reflection(type);
                } catch (Exception ignored) {
                }
            }
            Method writeMethod = pd.getWriteMethod();
            writeMethod.invoke(entity, value);
        }
        return entity;
    }

}

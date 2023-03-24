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

package honhimw.jackson.dataformat.hyper.poi;

import java.util.Set;

/**
 * @author hon_him
 * @since 2023-03-12
 */

public interface RetainedSheets {

    String LIST = "List";
    String MAP = "Map";
    String SET = "Set";
    String OBJECT = "Object";

    Set<String> RETAIN_SHEET_NAMES = Set.of(LIST, SET, MAP, OBJECT);

    static boolean isRetain(String name) {
        return RETAIN_SHEET_NAMES.contains(name);
    }

    static boolean isRetain(Class<?> type) {
        return isRetain(type.getSimpleName());
    }

    static void assertUsable(String name) {
        if (isRetain(name)) {
            throw new IllegalArgumentException(String.format("[%s] is retained, please rename the sheet", name));
        }
    }
    static void assertUsable(Class<?> type) {
        if (isRetain(type.getSimpleName())) {
            throw new IllegalArgumentException(String.format("[%s] is retained, please rename the sheet", type.getSimpleName()));
        }
    }

}

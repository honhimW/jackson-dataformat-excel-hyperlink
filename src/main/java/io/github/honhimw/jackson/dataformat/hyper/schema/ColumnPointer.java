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

package io.github.honhimw.jackson.dataformat.hyper.schema;

import java.util.Iterator;
import java.util.stream.Stream;

public interface ColumnPointer extends Iterable<ColumnPointer> {

    static ColumnPointer empty() {
        return PathPointer.EMPTY;
    }

    static ColumnPointer array() {
        return PathPointer.ARRAY;
    }

    ColumnPointer resolve(String other);

    ColumnPointer resolve(ColumnPointer other);

    ColumnPointer resolveArray();

    ColumnPointer relativize(ColumnPointer other);

    int depth();

    boolean isEmpty();

    boolean isParent();

    ColumnPointer getParent();

    ColumnPointer head();

    String name();

    boolean contains(ColumnPointer other);

    Stream<ColumnPointer> stream();

    @Override
    Iterator<ColumnPointer> iterator();

    @Override
    String toString();

    boolean startsWith(ColumnPointer other);

    boolean equals(Object o);

    int hashCode();
}

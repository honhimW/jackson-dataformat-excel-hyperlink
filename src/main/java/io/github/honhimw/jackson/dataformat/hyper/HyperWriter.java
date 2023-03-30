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

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import io.github.honhimw.jackson.dataformat.hyper.ser.BookOutput;
import java.io.IOException;

public final class HyperWriter extends ObjectWriter {

    HyperWriter(final HyperMapper mapper, final SerializationConfig config,
        final JavaType rootType, final PrettyPrinter pp) {
        super(mapper, config, rootType, pp);
    }

    HyperWriter(final HyperMapper mapper, final SerializationConfig config) {
        super(mapper, config);
    }

    HyperWriter(final HyperMapper mapper, final SerializationConfig config, final FormatSchema s) {
        super(mapper, config, s);
    }

    private HyperWriter(final HyperWriter base, final SerializationConfig config,
        final GeneratorSettings genSettings, final Prefetch prefetch) {
        super(base, config, genSettings, prefetch);
    }

    private HyperWriter(final HyperWriter base, final SerializationConfig config) {
        super(base, config);
    }

    private HyperWriter(final HyperWriter base, final HyperFactory f) {
        super(base, f);
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************************
    /* Internal factory methods, for convenience
    /**********************************************************************
     */

    @Override
    protected ObjectWriter _new(final ObjectWriter base, final JsonFactory f) {
        return new HyperWriter((HyperWriter) base, (HyperFactory) f);
    }

    @Override
    protected ObjectWriter _new(final ObjectWriter base, final SerializationConfig config) {
        return new HyperWriter((HyperWriter) base, config);
    }

    @Override
    protected ObjectWriter _new(final GeneratorSettings genSettings, final Prefetch prefetch) {
        return new HyperWriter(this, _config, genSettings, prefetch);
    }

    /*
    /**********************************************************
    /* Factory methods for creating SheetGenerators
    /**********************************************************
     */

    public HyperGenerator createGenerator(final BookOutput<?> out) throws IOException {
        _assertNotNull("out", out);
        return (HyperGenerator) _configureGenerator(generatorFactory().createGenerator(out));
    }

    /*
    /**********************************************************
    /* Factory methods for sequence writers
    /**********************************************************
     */

    public SequenceWriter writeValues(final BookOutput<?> out) throws IOException {
        return _newSequenceWriter(false, createGenerator(out), true);
    }

    public SequenceWriter writeValuesAsArray(final BookOutput<?> out) throws IOException {
        return _newSequenceWriter(true, createGenerator(out), true);
    }

    /*
    /**********************************************************
    /* Serialization methods, others
    /**********************************************************
     */

    public void writeValue(final BookOutput<?> out, final Object value) throws IOException {
        _writeValueAndClose(createGenerator(out), value);
    }

    /*
    /**********************************************************
    /* Other public methods
    /**********************************************************
     */

    public HyperFactory generatorFactory() {
        return (HyperFactory) _generatorFactory;
    }
}

/*
 * Copyright (c) 2016, IIG Telematics, Uni Freiburg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the disclaimer
 * below) provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of IIG Telematics, Uni Freiburg nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY
 * THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BELIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uni.freiburg.iig.telematik.sewol.log;

import de.invation.code.toval.misc.Filterable;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter class for {@link Log} with {@link Filterable} representing a filtered
 * log.
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 * @param <E> LogEntry type
 */
public class LogView<E extends LogEntry> extends Log<E> {

        private final Set<Filterable<LogTrace<E>>> filters = new HashSet<>();

        private boolean uptodate = true;

        /**
         * Adds a new filter to the view.
         *
         * @param filter
         */
        public void addFilter(Filterable<LogTrace<E>> filter) {
                Validate.notNull(filter);
                filters.add(filter);
                uptodate = false;
        }

        /**
         * Removes a filter to the view.
         *
         * @param filter
         */
        public void removeFilter(Filterable<LogTrace<E>> filter) {
                Validate.notNull(filter);
                filters.remove(filter);
                uptodate = false;
        }

        /**
         * Returns an unmodifiable list of filters.
         *
         * @return
         */
        public Set<Filterable<LogTrace<E>>> getFilters() {
                return Collections.unmodifiableSet(filters);
        }

        @Override
        public LogSummary<E> getSummary() {
                update();
                return summary;
        }

        @Override
        public void addTrace(LogTrace<E> trace) throws ParameterException {
                Validate.notNull(trace);
                boolean accept = true;
                for (Filterable<LogTrace<E>> filter : filters) {
                        if (!filter.accept(trace)) {
                                accept = false;
                                break;
                        }
                }
                if (accept) {
                        traces.add(trace);
                        summary.addTrace(trace);
                        if (!distinctTraces.add(trace)) {
                                for (LogTrace<E> storedTrace : traces) {
                                        if (storedTrace.equals(trace)) {
                                                storedTrace.addSimilarInstance(trace.getCaseNumber());
                                                trace.addSimilarInstance(storedTrace.getCaseNumber());
                                        }
                                }
                        }
                }
        }

        @Override
        public List<LogTrace<E>> getTraces() {
                update();
                return Collections.unmodifiableList(traces);
        }

        /**
         * Updates the summary, set of distinct traces and list of traces if the
         * filter set has been changed.
         */
        private void update() {
                if (!uptodate) {
                        List<LogTrace<E>> oldTraces = traces;
                        summary.clear();
                        distinctTraces.clear();
                        traces.clear();
                        addTraces(oldTraces);
                        uptodate = true;
                }
        }
}

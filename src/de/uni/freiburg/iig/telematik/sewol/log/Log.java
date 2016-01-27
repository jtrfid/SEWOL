/*
 * Copyright (c) 2015, IIG Telematics, Uni Freiburg
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

/**
 * @param <E>
 */
public class Log<E extends LogEntry> {

        LogSummary<E> summary = new LogSummary<>();
        Set<LogTrace<E>> distinctTraces = new HashSet<>();
        List<LogTrace<E>> traces = new ArrayList<>();

        /**
         * Returns the {@link LogSummary}.
         *
         * @return
         */
        public LogSummary<E> getSummary() {
                return summary;
        }

        /**
         * Reinitializes the log by reseting the list of traces, the summary and
         * the set of distinct traces.
         */
        void reinitialize() {
                summary = new LogSummary<>();
                distinctTraces = new HashSet<>();
                traces = new ArrayList<>();
        }

        /**
         * Adds a list of {@link LogTrace}s.
         *
         * @param traces Traces to add.
         * @throws ParameterException If the given list of traces is null.
         */
        public void addTraces(List<LogTrace<E>> traces) throws ParameterException {
                Validate.notNull(traces);
                for (LogTrace<E> trace : traces) {
                        addTrace(trace);
                }
        }

        /**
         * Adds a trace to the log.
         *
         * @param trace Trace to add.
         * @throws ParameterException
         */
        public void addTrace(LogTrace<E> trace) throws ParameterException {
                Validate.notNull(trace);
                trace.setCaseNumber(traces.size() + 1);
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

        /**
         * Returns an unmodifiable list of traces.
         *
         * @return
         */
        public List<LogTrace<E>> getTraces() {
                return Collections.unmodifiableList(traces);
        }
}

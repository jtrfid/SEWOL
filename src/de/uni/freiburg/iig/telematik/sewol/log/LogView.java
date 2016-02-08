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
import de.invation.code.toval.misc.NamedComponent;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.log.filter.AbstractLogFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Adapter class for {@link Log} with {@link Filterable} representing a filtered
 * log.
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 * @param <E> LogEntry type
 */
public class LogView<E extends LogEntry> extends Log<E> implements Observer, NamedComponent, Comparator<LogView> {

        private List<LogTrace<E>> allTraces = new ArrayList<>();
        final private Set<AbstractLogFilter<E>> filters = new HashSet<>();

        private boolean uptodate = true;
        private String name;

        private String parentLogName = null;

        private File fileReference;

        public LogView(String name) {
                setName(name);
        }

        /**
         * Adds a new filter to the view.
         *
         * @param filter
         */
        public void addFilter(AbstractLogFilter<E> filter) {
                Validate.notNull(filter);
                filters.add(filter);
                uptodate = false;
                filter.addObserver(this);
        }

        /**
         * Removes a filter to the view.
         *
         * @param filter
         */
        public void removeFilter(AbstractLogFilter<E> filter) {
                Validate.notNull(filter);
                filters.remove(filter);
                uptodate = false;
                filter.deleteObserver(this);
        }

        @Override
        void reinitialize() {
                super.reinitialize();
                allTraces = new ArrayList<>();
        }

        /**
         * Returns an unmodifiable list of filters.
         *
         * @return
         */
        public Set<AbstractLogFilter<E>> getFilters() {
                return Collections.unmodifiableSet(filters);
        }

        @Override
        public LogSummary<E> getSummary() {
                update();
                return summary;
        }

        @Override
        public void addTrace(LogTrace<E> trace) throws ParameterException {
                addTrace(trace, true);
        }

        private void addTrace(LogTrace<E> trace, boolean addToAllTraces) throws ParameterException {
                Validate.notNull(trace);
                boolean accept = true;
                for (AbstractLogFilter<E> filter : filters) {
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
                if (addToAllTraces) {
                        allTraces.add(trace);
                }
        }

        @Override
        public void addTraces(List<LogTrace<E>> traces) throws ParameterException {
                addTraces(traces, true);
        }

        private void addTraces(List<LogTrace<E>> traces, boolean addToAllTraces) throws ParameterException {
                Validate.notNull(traces);
                for (LogTrace<E> trace : traces) {
                        addTrace(trace, addToAllTraces);
                }
        }

        @Override
        public List<LogTrace<E>> getTraces() {
                update();
                return Collections.unmodifiableList(traces);
        }

        @Override
        public void update(Observable observable, Object object) {
                uptodate = false;
        }

        /**
         * Updates the summary, set of distinct traces and list of traces if the
         * filter set has been changed.
         */
        private void update() {
                if (!uptodate) {
                        summary.clear();
                        distinctTraces.clear();
                        traces.clear();
                        addTraces(allTraces, false);
                        uptodate = true;
                }
        }

        @Override
        public final String getName() {
                return name;
        }

        @Override
        public final void setName(String name) {
                Validate.notNull(name);
                Validate.notEmpty(name);

                this.name = name;
        }

        public File getFileReference() {
                return fileReference;
        }

        public final void setFileReference(File fileReference) {
                this.fileReference = fileReference;
        }

        public String getParentLogName() {
                return parentLogName;
        }

        public void setParentLogName(String parentLogName) {
                this.parentLogName = parentLogName;
        }

        @Override
        public int compare(LogView view1, LogView view2) {
                return view1.getName().compareTo(view2.getName());
        }

        @Override
        public int hashCode() {
                int hash = 3;
                hash = 97 * hash + Objects.hashCode(this.filters);
                hash = 97 * hash + Objects.hashCode(this.name);
                hash = 97 * hash + Objects.hashCode(this.parentLogName);
                hash = 97 * hash + Objects.hashCode(this.fileReference);
                return hash;
        }

        @Override
        public boolean equals(Object obj) {
                if (obj == null) {
                        return false;
                }
                if (getClass() != obj.getClass()) {
                        return false;
                }
                final LogView<?> other = (LogView<?>) obj;
                if (!Objects.equals(this.filters, other.filters)) {
                        return false;
                }
                if (!Objects.equals(this.name, other.name)) {
                        return false;
                }
                if (!Objects.equals(this.parentLogName, other.parentLogName)) {
                        return false;
                }
                return Objects.equals(this.fileReference, other.fileReference);
        }
}

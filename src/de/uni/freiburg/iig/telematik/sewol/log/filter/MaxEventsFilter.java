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
package de.uni.freiburg.iig.telematik.sewol.log.filter;

import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;

/**
 * Filter for log traces to filter out traces with more than specified events.
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 * @param <E>
 */
public class MaxEventsFilter<E extends LogEntry> extends AbstractLogFilter<E> {

        public static final String MAX_EVENTS_FILTER_NAME = "max events";

        public static final int MAX_EVENTS_DEFAULT = Integer.MAX_VALUE;

        private int max;

        public MaxEventsFilter(int max) {
                super();
                this.max = max;
        }

        public MaxEventsFilter() {
                this(MAX_EVENTS_DEFAULT);
        }

        public MaxEventsFilter(int max, boolean invert) {
                super(invert);
                this.max = max;
        }

        /**
         * Copy constructor.
         *
         * @param filter
         */
        public MaxEventsFilter(MaxEventsFilter filter) {
                this(filter.getMax(), filter.isInverted());
        }

        public int getMax() {
                return max;
        }

        public void setMax(int max) {
                if (max != this.max) {
                        this.max = max;
                        setChanged();
                        notifyObservers();
                }
        }

        @Override
        public boolean accept(LogTrace<E> trace) {
                return isInverted() ^ (trace.size() <= max);
        }

        @Override
        public String toString() {
                StringBuilder sb = new StringBuilder();

                sb.append(super.toString());

                sb.append("MaxEvents(");
                sb.append(max);
                sb.append(")");

                return sb.toString();
        }

        @Override
        public int hashCode() {
                int hash = 7;
                hash = 19 * hash + this.max;
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
                final MaxEventsFilter<?> other = (MaxEventsFilter<?>) obj;
                return this.max == other.max;
        }

        @Override
        public MaxEventsFilter copy() {
                return new MaxEventsFilter(this);
        }

        @Override
        public String getName() {
                return MAX_EVENTS_FILTER_NAME;
        }
}

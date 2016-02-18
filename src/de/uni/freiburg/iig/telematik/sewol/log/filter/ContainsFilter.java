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

import de.uni.freiburg.iig.telematik.sewol.log.EventType;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import java.util.Objects;

/**
 * Filter for log traces to filter out traces with more than specified events.
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 * @param <E>
 */
public class ContainsFilter<E extends LogEntry> extends AbstractLogFilter<E> {

        private ContainsFilterParameter parameter;
        private String value;

        public ContainsFilter(ContainsFilterParameter parameter, String value) {
                super();
                this.parameter = parameter;
                this.value = value;
        }

        public ContainsFilter(ContainsFilterParameter parameter, String value, boolean invert) {
                super(invert);
                this.parameter = parameter;
                this.value = value;
        }

        public ContainsFilterParameter getParameter() {
                return parameter;
        }

        public String getValue() {
                return value;
        }

        public void setParameter(ContainsFilterParameter parameter) {
                if (!parameter.equals(this.parameter)) {
                        this.parameter = parameter;
                        setChanged();
                        notifyObservers();
                }
        }

        public void setValue(String value) {
                if (!value.equals(this.value)) {
                        this.value = value;
                        setChanged();
                        notifyObservers();
                }
        }

        @Override
        public boolean accept(LogTrace<E> trace) {
                for (E entry : trace.getEntries()) {
                        switch (parameter) {
                                case ACTIVITY:
                                        if (entry.getActivity().equals(value)) {
                                                return isInverted() ^ true;
                                        }
                                        break;
                                case SUBJECT:
                                        if (entry.getOriginator().equals(value)) {
                                                return isInverted() ^ true;
                                        }
                                        break;
                                case ROLE:
                                        if (entry.getRole().equals(value)) {
                                                return isInverted() ^ true;
                                        }
                                        break;
                                case EVENTTYPE:
                                        if (EventType.parse(value) == entry.getEventType()) {
                                                return isInverted() ^ true;
                                        }
                        }
                }

                return isInverted() ^ false;
        }

        @Override
        public String toString() {
                StringBuilder sb = new StringBuilder();

                sb.append(super.toString());

                sb.append("Contains(");
                sb.append(getParameter().name);
                sb.append(",");
                sb.append(getValue());
                sb.append(")");

                return sb.toString();
        }

        @Override
        public int hashCode() {
                int hash = 7;
                hash = 37 * hash + Objects.hashCode(this.parameter);
                hash = 37 * hash + Objects.hashCode(this.value);
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
                final ContainsFilter<?> other = (ContainsFilter<?>) obj;
                if (this.parameter != other.parameter) {
                        return false;
                }
                return Objects.equals(this.value, other.value);
        }

        public enum ContainsFilterParameter {

                ACTIVITY("A"), SUBJECT("S"), ROLE("R"), EVENTTYPE("T");

                public final String name;

                private ContainsFilterParameter(String name) {
                        this.name = name;
                }
        }
}

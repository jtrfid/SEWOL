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

import de.invation.code.toval.misc.Filterable;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import java.util.Observable;

/**
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 * @param <E>
 */
public abstract class AbstractLogFilter<E extends LogEntry> extends Observable implements Filterable<LogTrace<E>> {

        private boolean invert = false;

        /**
         * Creates a new log filter.
         */
        public AbstractLogFilter() {
        }

        /**
         * Creates a new log filter.
         *
         * @param invert Specifies if the filter result should be inverted.
         */
        public AbstractLogFilter(boolean invert) {
                this.invert = invert;
        }

        public abstract String getName();

        /**
         * @return Returns <code>true</code> if the filter result should be
         * inverted.
         */
        public boolean isInverted() {
                return invert;
        }

        /**
         * Sets if the filter result should be inverted.
         *
         * @param invert
         */
        public void setInverted(boolean invert) {
                if (invert != this.invert) {
                        this.invert = invert;
                        setChanged();
                        notifyObservers();
                }
        }

        @Override
        public String toString() {
                if (isInverted()) {
                        return "\u00ac";
                }
                return "";
        }

        @Override
        public int hashCode() {
                int hash = 5;
                hash = 97 * hash + (this.invert ? 1 : 0);
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
                final AbstractLogFilter<?> other = (AbstractLogFilter<?>) obj;
                return this.invert == other.invert;
        }

        public abstract AbstractLogFilter copy();
}

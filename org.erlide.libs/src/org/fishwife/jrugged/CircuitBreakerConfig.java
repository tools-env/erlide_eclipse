/* Copyright 2009-2012 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged;

/**
 * The CircuitBreakerConfig class holds a
 * {@link org.fishwife.jrugged.CircuitBreaker} configuration.
 */
public class CircuitBreakerConfig {

    private final FailureInterpreter failureInterpreter;
    private final long resetMillis;

    public CircuitBreakerConfig(final long resetMillis,
            final FailureInterpreter failureInterpreter) {
        this.resetMillis = resetMillis;
        this.failureInterpreter = failureInterpreter;
    }

    public long getResetMillis() {
        return resetMillis;
    }

    public FailureInterpreter getFailureInterpreter() {
        return failureInterpreter;
    }
}
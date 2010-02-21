// GPars (formerly GParallelizer)
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.samples.forkjoin

/**
 *
 * @author Vaclav Pech
 * Date: Feb 19, 2010
 */

import groovyx.gpars.AbstractForkJoinWorker
import static groovyx.gpars.Parallelizer.doParallel
import static groovyx.gpars.Parallelizer.orchestrate

class ForkJoinFib extends AbstractForkJoinWorker {

    def number

    protected void computeTask() {
        if (number < 0) {
            throw new RuntimeException("No fib below 0!")
        }
        if (number <= 13) {
            result = seqfib(number)
            return
        }
        [new ForkJoinFib(number: (number - 1)), new ForkJoinFib(number: (number - 2))].each { forkOffChild it }
        result = childrenResults.sum()
    }

    static int seqfib(int n) {
        if (n <= 1) return n;
        else return seqfib(n - 1) + seqfib(n - 2);
    }
}

doParallel(2) {

    final long t1 = System.currentTimeMillis()
    try {
        assert orchestrate(new ForkJoinFib(number: 30)) == 832040
        assert orchestrate(new ForkJoinFib(number: 36)) == 14930352

        assert ForkJoinFib.seqfib(37) == 24157817

        assert orchestrate(new ForkJoinFib(number: 37)) == 24157817

        try {
            orchestrate(new ForkJoinFib(number: -1))
        } catch (RuntimeException ignore) {
            println "We've correctly received an exception. That's what we deserve for calculating a negative Fibbonacci number."
        }
    } catch (Throwable e) {
        e.printStackTrace()
    }
    final long t2 = System.currentTimeMillis()
    println t2 - t1
}

// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars

import groovyx.gpars.dataflow.Dataflow
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.DataflowVariable
import groovyx.gpars.dataflow.Dataflows
import groovyx.gpars.group.DefaultPGroup
import groovyx.gpars.group.PGroup

/**
 * @author Vaclav Pech
 */
class ParallelGroupTest extends GroovyTestCase {
    private PGroup group

    protected void setUp() {
        group = new DefaultPGroup()
        super.setUp()
    }

    protected void tearDown() {
        group.shutdown()
        super.tearDown()
    }


    public void testParallelGroup() {
        final Dataflows results = new Dataflows()
        def actor = group.actor {
            results.group1 = parallelGroup
            react {}
        }
        actor.sendAndContinue(10) {
            results.group2 = parallelGroup
        }

        assert results.group1.is(results.group1)
    }

    public void testDataflowContinuations() {
        final DataflowVariable variable = new DataflowVariable()
        final Dataflows results = new Dataflows()
        Dataflow.task {
            results.group1 = Dataflow.activeParallelGroup.get()
            variable.whenBound {
                results.group2 = Dataflow.activeParallelGroup.get()
            }
            variable << 'Foo'
        }
        assert results.group1 == results.group2
        assert results.group1 == Dataflow.DATA_FLOW_GROUP
    }

    public void testDataflowContinuationsWithCustomGroup() {
        final DataflowVariable variable = new DataflowVariable()
        final Dataflows results = new Dataflows()

        group.task {
            results.group1 = Dataflow.activeParallelGroup.get()
            variable.whenBound {
                results.group2 = Dataflow.activeParallelGroup.get()
            }
            variable << 'Foo'
        }
        assert results.group1 == results.group2
        assert results.group1 == group
    }

    public void testDataflowContinuationsOnStreams() {
        final DataflowQueue stream = new DataflowQueue()
        final Dataflows results = new Dataflows()
        Dataflow.task {
            results.group1 = Dataflow.activeParallelGroup.get()
            stream.whenBound {
                results.group2 = Dataflow.activeParallelGroup.get()
            }
            stream << 'Foo'
        }
        assert results.group1 == results.group2
        assert results.group1 == Dataflow.DATA_FLOW_GROUP
    }

    public void testDataflowContinuationsWithCustomGroupOnStreams() {
        final DataflowQueue stream = new DataflowQueue()
        final Dataflows results = new Dataflows()

        group.task {
            results.group1 = Dataflow.activeParallelGroup.get()
            stream.whenBound {
                results.group2 = Dataflow.activeParallelGroup.get()
            }
            stream << 'Foo'
        }
        assert results.group1 == results.group2
        assert results.group1 == group
    }

    public void testDataflowWhenBoundOnStreams() {
        final DataflowQueue stream = new DataflowQueue()
        final Dataflows results = new Dataflows()
        Dataflow.task {
            results.group1 = Dataflow.activeParallelGroup.get()
            stream.whenBound {
                results.group2 = Dataflow.activeParallelGroup.get()
            }
            stream << 'Foo'
        }
        assert results.group1 == results.group2
        assert results.group1 == Dataflow.DATA_FLOW_GROUP
    }

    public void testDataflowWhenBoundWithCustomGroupOnStreams() {
        final DataflowQueue stream = new DataflowQueue()
        final Dataflows results = new Dataflows()

        group.task {
            results.group1 = Dataflow.activeParallelGroup.get()
            stream.whenBound {
                results.group2 = Dataflow.activeParallelGroup.get()
            }
            stream << 'Foo'
        }
        assert results.group1 == results.group2
        assert results.group1 == group
    }

    public void testDataflowContinuationsWithSingleThread() {
        final DataflowVariable variable = new DataflowVariable()
        final Dataflows results = new Dataflows()

        final DefaultPGroup group = new DefaultPGroup(1)
        group.task {
            results.t1 = Thread.currentThread()
            variable.whenBound {
                results.t2 = Thread.currentThread()
            }
            variable.whenBound {
                results.t3 = Thread.currentThread()
            }
            variable.whenBound {
                results.t4 = Thread.currentThread()
            }
            variable << 'Foo'
        }
        assert results.t1 == results.t2
        assert (1..4).collect {results.t1} == [results.t1, results.t2, results.t3, results.t4]
        group.shutdown()
    }

    public void testSendAndContinue() {
        final DefaultPGroup group = new DefaultPGroup(1)
        def results = new Dataflows()

        def actor = group.actor {
            results.t1 = Thread.currentThread()
            react {
                results.t2 = Thread.currentThread()
                reply it
            }
        }
        actor.sendAndContinue(1) {results.t3 = Thread.currentThread();}
        assert results.t1 == results.t2
        assert results.t1 == results.t3
        group.shutdown()
    }
}

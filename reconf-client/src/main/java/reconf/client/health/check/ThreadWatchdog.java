/*
 *    Copyright 1996-2013 UOL Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package reconf.client.health.check;

import java.util.*;
import java.util.concurrent.*;
import reconf.infra.log.*;

public class ThreadWatchdog extends Thread {

    private CopyOnWriteArrayList<DogThread> toWatch = new CopyOnWriteArrayList<DogThread>();

    public ThreadWatchdog() {
        setName("Thread Checker");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(1);
                LoggerHolder.getLog().debug("starting ThreadWatchdog");
                List<DogThread> threads = new ArrayList<DogThread>(toWatch);

                List<DogThread> toRemove = new ArrayList<DogThread>();
                List<DogThread> toAdd = new ArrayList<DogThread>();

                for (DogThread thread : threads) {
                    if (thread.getReloadInterval() > 0 && System.currentTimeMillis() - thread.getLastExecution() > thread.getReloadTimeUnit().toMillis(thread.getReloadInterval())) {
                        LoggerHolder.getLog().error(thread.getName() + " is not running!");
                        toRemove.add(thread);
                        toAdd.add((DogThread) thread.clone());
                    }
                }

                for (DogThread rem : toRemove) {
                    toWatch.remove(rem);
                    try {
                        rem.kill();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

                for (DogThread add : toAdd) {
                    toWatch.add(add);
                    add.start();
                }

            } catch (Throwable t) {
                LoggerHolder.getLog().error("error while executing ThreadWatchdog", t);
            }
        }
    }

    public void add(DogThread thread) {
        if (thread != null) {
            toWatch.add(thread);
        }
    }

}

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
package reconf.client.experimental;

import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.lang.exception.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;

public class ObserverThread extends Thread {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ObserverThread.class);
    private CopyOnWriteArrayList<ObservableThread> toWatch = new CopyOnWriteArrayList<ObservableThread>();

    public ObserverThread() {
        setName("Thread Checker");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(5);
                LoggerHolder.getLog().debug(msg.get("start"));
                List<ObservableThread> threads = new ArrayList<ObservableThread>(toWatch);

                List<ObservableThread> toRemove = new ArrayList<ObservableThread>();
                List<ObservableThread> toAdd = new ArrayList<ObservableThread>();

                for (ObservableThread thread : threads) {
                    if (thread.getReloadInterval() > 0 && System.currentTimeMillis() - thread.getLastExecution() > (1.5F * thread.getReloadTimeUnit().toMillis(thread.getReloadInterval()))) {
                        LoggerHolder.getLog().error(msg.format("not.running", thread.getName()));
                        toRemove.add(thread);
                        toAdd.add((ObservableThread) thread.clone());
                    }
                }

                for (ObservableThread rem : toRemove) {
                    toWatch.remove(rem);
                    rem.stopIt();
                }

                for (ObservableThread add : toAdd) {
                    toWatch.add(add);
                    add.start();
                }

            } catch (Throwable t) {
                LoggerHolder.getLog().error(msg.format("error", ExceptionUtils.getFullStackTrace(t)));
            }
        }
    }

    public void add(ObservableThread thread) {
        if (thread != null) {
            toWatch.add(thread);
        }
    }

}

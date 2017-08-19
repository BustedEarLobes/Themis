package com.github.bustedearlobes.themis.taskmanager;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class ScheduledTask extends ListenerAdapter implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger("Themis");

    private long periodicity;
    private long repeat;
    private long numberOfRuns;
    private long timeOfNextRun;
    private JDA jda;
    
    public ScheduledTask(long delay, long periodicity, long repeat, JDA jda) {
        this.periodicity = periodicity;
        this.repeat = repeat;
        this.timeOfNextRun = System.currentTimeMillis() + delay;
        this.numberOfRuns = 0;
        this.jda = jda;
    }

    protected boolean taskIsReady() {
        return (System.currentTimeMillis() >= timeOfNextRun);
    }

    protected boolean isExpired() {
        return (numberOfRuns > repeat);
    }
    
    protected JDA getJDA() {
        return jda;
    }

    @Override
    public final void run() {
        jda.addEventListener(this);
        try {
            runTask();
        } catch(Exception e) {
            StackTraceElement[] st = e.getStackTrace();
            if(st.length > 0) {
                StackTraceElement lastStackTrace = st[st.length - 1];
                LOG.logp(Level.WARNING, lastStackTrace.getClassName(), lastStackTrace.getMethodName(),
                        "Exception occured in scheduled task.", e);
            } else {
                LOG.log(Level.WARNING, "Exception occured in scheduled task.", e);
            }
        } finally {
            jda.removeEventListener(this);
        }
        timeOfNextRun = System.currentTimeMillis() + periodicity;
        if(repeat != Long.MAX_VALUE) {
            numberOfRuns++;
        }
    }

    protected abstract void runTask();
}
